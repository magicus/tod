/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>


#include <jni.h>
#include <jvmti.h>

#include "utils.h"
#include "jniutils.h"
#include "md5.h"

#include <vector>

#include <iostream>
#include <fstream>
#include <asio.hpp>
#include <boost/filesystem/convenience.hpp>
#include <boost/thread/tss.hpp>

// Build: g++ -shared -o ../../libbci-agent.so -I $JAVA_HOME/include/ -I $JAVA_HOME/include/linux/ bci-agent.c
#ifdef __cplusplus
extern "C" {
#endif

using asio::ip::tcp;
namespace fs = boost::filesystem;

// Outgoing commands
const char EXCEPTION_GENERATED = 20;
const char INSTRUMENT_CLASS = 50;
const char FLUSH = 99;

const char OBJECT_HASH = 1;
const char OBJECT_UID = 2;

// Incoming commands
const char SET_SKIP_CORE_CLASSES = 81;
const char SET_CAPTURE_EXCEPTIONS = 83;
const char SET_HOST_BITS = 84;
const char SET_WORKING_SET = 85;
const char CONFIG_DONE = 90;

int VM_STARTED = 0;
STREAM* gSocket;

// Pointer to our JVMTI environment, to be able to use it in pure JNI calls
jvmtiEnv *globalJvmti;

// Configuration data
int cfgSkipCoreClasses = 0;
int cfgCaptureExceptions = 0;
int cfgHostBits = 8; // Number of bits used to encode host id.
int cfgHostId = 0; // A host id assigned by the TODServer - not official.
char* cfgWorkingSet = "undefined"; // The current working set of instrumentation


// System properties configuration data.
char* propHost = NULL;
char* propHostName = NULL;
char* propNativePort = NULL;
char* propCachePath = NULL;
char* _propVerbose = NULL;
int propVerbose = 2;

// directory prefix for the class cache. It is the MD5 sum of the working set.
char* classCachePrefix = NULL;

// Class and method references
StaticVoidMethod* ExceptionGeneratedReceiver_exceptionGenerated;
int isInitializingExceptionMethods = 0;
boost::thread_specific_ptr<bool> isInExceptionCb;

StaticVoidMethod* TracedMethods_setTraced;
StaticVoidMethod* TOD_enable;

// Method IDs for methods whose exceptions are ignored
jmethodID ignoredExceptionMethods[3];

// Object Id mutex and current id value
t_mutex oidMutex;
jlong oidCurrent = 1;

// Mutex for class load callback
t_mutex loadMutex;

// This vector holds traced methods ids for methods
// that are registered prior to VM initialization.
std::vector<int> tmpTracedMethods;



/*
Connects to the instrumenting host
host: host to connect to
hostname: name of this host, sent to the peer.
*/
void bciConnect(char* host, char* port, char* hostname)
{
	if (propVerbose >=1) printf("Connecting to %s:%s\n", host, port);
	fflush(stdout);
	gSocket = new tcp::iostream(host, port);
	if (gSocket->fail()) fatal_error("Could not connect.\n");
	
	// Send host name
	if (propVerbose>=1) printf("Sending host name: %s\n", hostname);
	writeUTF(gSocket, hostname);
	flush(gSocket);
	
	cfgHostId = readInt(gSocket);
	if (propVerbose>=2) printf("Assigned host id: %ld\n", cfgHostId);
	fflush(stdout);
}


/*
* Tries to create all the directories denoted by the given name.
*/
int mkdirs(fs::path& p)
{
	try
	{
		fs::create_directories(p.branch_path());
		return 1;
	}
	catch(...)
	{
		return 0;
	}
}

/* Every JVMTI interface returns an error code, which should be checked
 *   to avoid any cascading errors down the line.
 *   The interface GetErrorName() returns the actual enumeration constant
 *   name, making the error messages much easier to understand.
 */
void check_jvmti_error(jvmtiEnv *jvmti, jvmtiError errnum, const char *str)
{
	if ( errnum != JVMTI_ERROR_NONE ) 
	{
		char *errnum_str;
		
		errnum_str = NULL;
		(void)jvmti->GetErrorName(errnum, &errnum_str);
		
		printf("ERROR: JVMTI: %d(%s): %s\n", errnum, 
			(errnum_str==NULL?"Unknown":errnum_str),
			(str==NULL?"":str));
		
	}
}

void enable_event(jvmtiEnv *jvmti, jvmtiEvent event)
{
	jvmtiError err = jvmti->SetEventNotificationMode(JVMTI_ENABLE, event, NULL);
	check_jvmti_error(jvmti, err, "SetEventNotificationMode");
}

void bciConfigure()
{
	while(true)
	{
		int cmd = readByte(gSocket);
		switch(cmd)
		{
			case SET_SKIP_CORE_CLASSES:
				cfgSkipCoreClasses = readByte(gSocket);
				if (propVerbose >= 1) printf("Skipping core classes: %s\n", cfgSkipCoreClasses ? "Yes" : "No");
				break;

			case SET_CAPTURE_EXCEPTIONS:
				cfgCaptureExceptions = readByte(gSocket);
				if (propVerbose >= 1) printf("Capture exceptions: %s\n", cfgCaptureExceptions ? "Yes" : "No");
				break;
				
			case SET_HOST_BITS:
				cfgHostBits = readByte(gSocket);
				if (propVerbose >= 1) printf("Host bits: %d\n", cfgHostBits);
				break;

			case SET_WORKING_SET:
				cfgWorkingSet = readUTF(gSocket);
				if (propVerbose >= 1) printf("Working set: %s\n", cfgWorkingSet);
				break;

			case CONFIG_DONE:
				// Check host id vs host bits
				int mask = (1 << cfgHostBits) - 1;
				if ((cfgHostId & mask) != cfgHostId) fatal_error("Host id overflow\n");
				
				// Compute md5 sum of working set.
				char md5Buffer[16];
				char* md5String = (char*) malloc(33);
				md5_buffer(cfgWorkingSet, strlen(cfgWorkingSet), md5Buffer);
				md5_sig_to_string(md5Buffer, md5String, 33);

				classCachePrefix = md5String;
				if (propVerbose >= 1) printf("Class cache prefix: %s\n", classCachePrefix);
				
				if (propVerbose >= 1) printf("Config done.\n");
				return;
		}
	}
	fflush(stdout);
}

void registerTracedMethod(JNIEnv* jni, int tracedMethod)
{
	TracedMethods_setTraced->invoke(jni, tracedMethod);
	if (propVerbose>=3) printf("Registered traced method: %d\n", tracedMethod);
}

/**
Registers the traced methods that were registered in tmpTracedMethods
*/ 
void registerTmpTracedMethods(JNIEnv* jni)
{
	if (propVerbose>=1) printf("Registering %d buffered traced methods\n", tmpTracedMethods.size());
	std::vector<int>::iterator iter = tmpTracedMethods.begin();
	std::vector<int>::iterator end = tmpTracedMethods.end();
	
	while (iter != end)
	{
		registerTracedMethod(jni, *iter++);
	}
	
	tmpTracedMethods.clear();
}

void registerTracedMethods(JNIEnv* jni, int nTracedMethods, int* tracedMethods)
{
	if (VM_STARTED)
	{
		if (propVerbose>=1 && nTracedMethods>0) printf("Registering %d traced methods\n", nTracedMethods);
		for (int i=0;i<nTracedMethods;i++)
		{
			registerTracedMethod(jni, tracedMethods[i]);
		}
	}
	else
	{
		if (propVerbose>=1 && nTracedMethods>0) printf("Buffering %d traced methods, will register later\n", nTracedMethods);
		for (int i=0;i<nTracedMethods;i++)
		{
			tmpTracedMethods.push_back(tracedMethods[i]);
		}
	}
	if (tracedMethods) delete tracedMethods;
}

void JNICALL cbClassFileLoadHook(
	jvmtiEnv *jvmti, JNIEnv* jni,
	jclass class_being_redefined, jobject loader,
	const char* name, jobject protection_domain,
	jint class_data_len, const unsigned char* class_data,
	jint* new_class_data_len, unsigned char** new_class_data) 
{
	if (strncmp("tod/core/", name, 9) == 0 
		|| strncmp("tod/agent/", name, 10) == 0
		) return;


	if (cfgSkipCoreClasses 
		&& (
			strncmp("java/", name, 5) == 0 
			|| strncmp("sun/", name, 4) == 0
			|| strncmp("javax/", name, 6) == 0 
			|| strncmp("com/sun/", name, 8) == 0 
		)) return;

	if (propVerbose>=1) printf("Loading (hook) %s\n", name);
	
	int* tracedMethods = NULL;
	int nTracedMethods = 0;
	
	// Compute MD5 sum
	char md5Buffer[16];
	char md5String[33];
	md5_buffer((const char *) class_data, class_data_len, md5Buffer);
	md5_sig_to_string(md5Buffer, md5String, 33);
	if (propVerbose>=3) printf("MD5 sum: %s\n", md5String);
	
	// Compute cache file paths	
	fs::path cacheFilePath;
	fs::path tracedCacheFilePath;
	
	if (propCachePath != NULL)
	{
		char cacheFileName[2000];
		char tracedCacheFileName[2000];
		cacheFileName[0] = 0;
		tracedCacheFileName[0] = 0;
		
		// Escape the class name, as all characters allowed for class names are
		// not necessarily allowed for files on all platforms.
		int l = strlen(name);
		char escapedName[l+1];
		strcpy(escapedName, name);
		for(int i=0;i<l;i++) if (escapedName[i] == '$') escapedName[i] = '_';
		
		snprintf(cacheFileName, sizeof(cacheFileName), "%s/%s/%s.%s.class", 
			propCachePath, classCachePrefix, escapedName, md5String);
			
		snprintf(tracedCacheFileName, sizeof(tracedCacheFileName), "%s/%s/%s.%s.tm", 
			propCachePath, classCachePrefix, escapedName, md5String);
		
		cacheFilePath = fs::path(cacheFileName);
		tracedCacheFilePath = fs::path(tracedCacheFileName);
		fflush(stdout);
	}

	// Check if we have a cached version
	if (propCachePath != NULL)
	{
		if (propVerbose>=2) 
		{
			printf ("Looking for %s\n", cacheFilePath.native_file_string().c_str());
			fflush(stdout);
		}
		
		// Check if length is 0
		if (fs::exists(cacheFilePath.native_file_string()))
		{
			int len = fs::file_size(cacheFilePath);
			
			if (len == 0)
			{
				if (propVerbose>=2) printf ("Using original\n");
			}
			else
			{
				std::fstream f;
				
				// Read class definition
				f.open(cacheFilePath.native_file_string().c_str(), std::ios_base::in | std::ios_base::binary);
				if (f.fail()) fatal_error("Could not open class file");
				
				jvmtiError err = jvmti->Allocate(len, new_class_data);
				check_jvmti_error(jvmti, err, "Allocate");
				*new_class_data_len = len;
		
				f.read((char*) *new_class_data, len);
				if (f.eof()) fatal_ioerror("EOF on read from class file");
				if (propVerbose>=2) printf("Class definition uploaded from cache.\n");
				f.close();
				
				// Read traced methods array
				f.open(tracedCacheFilePath.native_file_string().c_str(), std::ios_base::in | std::ios_base::binary);
				if (f.fail()) fatal_error("Could not open traced methods file");
				nTracedMethods = readInt(&f);
				tracedMethods = new int[nTracedMethods];
				for (int i=0;i<nTracedMethods;i++) tracedMethods[i] = readInt(&f);
				f.close();
			}
			
			// Register traced methods
			registerTracedMethods(jni, nTracedMethods, tracedMethods);
			
			fflush(stdout);
			return;
		}
		else
		{
			if (propVerbose>=2) 
			{
				printf ("Class not found in cache.\n");
				fflush(stdout);
			}
		}
	}
	
	{
		t_lock lock(loadMutex);
	
		// Send command
		writeByte(gSocket, INSTRUMENT_CLASS);
		
		// Send class name
		writeUTF(gSocket, name);
		
		// Send bytecode
		writeInt(gSocket, class_data_len);
		gSocket->write((char*) class_data, class_data_len);
		flush(gSocket);
		
		int len = readInt(gSocket);
		
		if (len > 0)
		{
			if (propVerbose>=2) printf("Redefining %s...\n", name);
			jvmtiError err = jvmti->Allocate(len, new_class_data);
			check_jvmti_error(jvmti, err, "Allocate");
			*new_class_data_len = len;
			
			gSocket->read((char*) *new_class_data, len);
			if (gSocket->eof()) fatal_ioerror("fread");
			if (propVerbose>=2) printf("Class definition uploaded.\n");
			
			nTracedMethods = readInt(gSocket);
			tracedMethods = new int[nTracedMethods];
			for (int i=0;i<nTracedMethods;i++) tracedMethods[i] = readInt(gSocket);
			
			// Cache class
			if (propCachePath != NULL)
			{
				if (propVerbose>=2) printf("Caching %s\n", cacheFilePath.native_file_string().c_str());
				if (! mkdirs(cacheFilePath)) fatal_ioerror("Error in mkdirs");
		
				std::fstream f;
				
				// Cache bytecode
				f.open(cacheFilePath.native_file_string().c_str(), std::ios_base::out | std::ios_base::binary | std::ios_base::trunc);
				if (f.fail()) fatal_ioerror("Opening cache class file for output");
				f.write((char*) *new_class_data, len);
				if (f.bad()) fatal_ioerror("Writing cached class");
				
				f.flush();
				f.close();
				
				// Cache traced methods
				f.open(tracedCacheFilePath.native_file_string().c_str(), std::ios_base::out | std::ios_base::binary | std::ios_base::trunc);
				if (f.fail()) fatal_ioerror("Opening cache traced methods file for output");
				writeInt(&f, nTracedMethods);
				for (int i=0;i<nTracedMethods;i++) writeInt(&f, tracedMethods[i]);
				f.flush();
				f.close();
				
				if (propVerbose>=2) printf("Cached.\n");
			}
		}
		else if (propCachePath != NULL)
		{
			// Mark class as not instrumented.
			if (propVerbose>=2) printf("Caching empty: %s\n", cacheFilePath.native_file_string().c_str());
			if (! mkdirs(cacheFilePath)) fatal_ioerror("Error in mkdirs");
			
			std::fstream f (cacheFilePath.native_file_string().c_str(), std::ios_base::out | std::ios_base::binary | std::ios_base::trunc);
			if (f.fail()) fatal_ioerror("Opening cache class file for output");
			f.flush();
			f.close();
			if (propVerbose>=2) printf("Cached empty.\n");
		}
	}
	
	// Register traced methods
	registerTracedMethods(jni, nTracedMethods, tracedMethods);
	fflush(stdout);
}

void ignoreMethod(JNIEnv* jni, int index, char* className, char* methodName, char* signature)
{
	if (propVerbose>=2) printf("Loading (jni-ignore) %s\n", className);
	jclass clazz = jni->FindClass(className);
	if (clazz == NULL) printf("Could not load %s\n", className);
	jmethodID method = jni->GetMethodID(clazz, methodName, signature);
	if (method == NULL) printf("Could not find %s.%s%s\n", className, methodName, signature);
	jni->DeleteLocalRef(clazz);

	ignoredExceptionMethods[index] = method;
}


void initExceptionClasses(JNIEnv* jni)
{
	// Initialize the classes and method ids that will be used
	// for exception processing
	ExceptionGeneratedReceiver_exceptionGenerated = new StaticVoidMethod(
		jni, 
		"tod/agent/ExceptionGeneratedReceiver",
		"exceptionGenerated", 
		"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Throwable;)V");
		
	// init ignored methods
	int i=0;
	ignoreMethod(jni, i++, "java/lang/ClassLoader", "findBootstrapClass", "(Ljava/lang/String;)Ljava/lang/Class;");
	ignoreMethod(jni, i++, "java/net/URLClassLoader$1", "run", "()Ljava/lang/Object;");
	ignoreMethod(jni, i++, "java/net/URLClassLoader", "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");
}

void JNICALL cbException(
	jvmtiEnv *jvmti,
	JNIEnv* jni,
	jthread thread,
	jmethodID method,
	jlocation location,
	jobject exception,
	jmethodID catch_method,
	jlocation catch_location)
{
	if (! isInExceptionCb.get()) isInExceptionCb.reset(new bool(false));
	
	if (isInitializingExceptionMethods) return; // Check if we are in the lazy init process
	
	if (cfgCaptureExceptions == 0) return;
	if (VM_STARTED == 0) return;
	
	if (! ExceptionGeneratedReceiver_exceptionGenerated)
	{
		isInitializingExceptionMethods = true;
		initExceptionClasses(jni);
		isInitializingExceptionMethods = false;
	}
	
	if (propVerbose>=3) printf("Exception detected by native agent.\n");
	
	for (int i=0;i<sizeof(ignoredExceptionMethods);i++)
	{
		if (method == ignoredExceptionMethods[i]) return;
	}
	
	if (*isInExceptionCb)
	{
		jni->FatalError("Recursive exeception event - probable cause is that the connection to the TOD database has been lost. Exiting.\n");
	}
	
	*isInExceptionCb = true;

	
	char* methodName;
	char* methodSignature;
	jclass methodDeclaringClass;
	char* methodDeclaringClassSignature;
 
	jvmtiJlocationFormat locationFormat;
	int bytecodeIndex = -1;
	
	// Obtain method information
	jvmti->GetMethodName(method, &methodName, &methodSignature, NULL);
	jvmti->GetMethodDeclaringClass(method, &methodDeclaringClass);
	jvmti->GetClassSignature(methodDeclaringClass, &methodDeclaringClassSignature, NULL);
	
	// Obtain location information
	jvmti->GetJLocationFormat(&locationFormat);
	if (locationFormat == JVMTI_JLOCATION_JVMBCI) bytecodeIndex = (int) location;
	
	if (propVerbose>=1) printf("Exception generated: %s, %s, %s, %d\n", methodName, methodSignature, methodDeclaringClassSignature, bytecodeIndex);
	
	ExceptionGeneratedReceiver_exceptionGenerated->invoke(
		jni,
		jni->NewStringUTF(methodName),
		jni->NewStringUTF(methodSignature),
		jni->NewStringUTF(methodDeclaringClassSignature),
		bytecodeIndex,
 		exception);

	// Free buffers
	jvmti->Deallocate((unsigned char*) methodName);
	jvmti->Deallocate((unsigned char*) methodSignature);
	jvmti->Deallocate((unsigned char*) methodDeclaringClassSignature);
	
	jthrowable ex = jni->ExceptionOccurred();
	if (ex)
	{
		jni->ExceptionDescribe();
		jni->FatalError("Exception detected while processing exeception event. Exiting.\n");
	}
	
	*isInExceptionCb = false;
}


void JNICALL cbVMStart(
	jvmtiEnv *jvmti,
	JNIEnv* jni)
{
	if (propVerbose>=1) printf("VMStart\n");
	fflush(stdout);
	
	// Initialize the classes and method ids that will be used
	// for registering traced methods
	TracedMethods_setTraced = new StaticVoidMethod(jni, "tod/agent/TracedMethods", "setTraced", "(I)V");
	
	TOD_enable = new StaticVoidMethod(jni, "tod/agent/AgentReady", "enable", "()V");
	TOD_enable->invoke(jni);
	
	if (propVerbose>=1) printf("VMStart - done\n");
	fflush(stdout);
	
	VM_STARTED = 1;
	
	registerTmpTracedMethods(jni);
}

/**
 * JVMTI initialization
 */
JNIEXPORT jint JNICALL 
Agent_OnLoad(JavaVM *vm, char *options, void *reserved) 
{
	jint rc;
	jvmtiError err;
	jvmtiEventCallbacks callbacks;
	jvmtiCapabilities capabilities;
	jvmtiEnv *jvmti;
	
	fs::path::default_name_check(fs::no_check);

	printf("Loading BCI agent - v2.2\n");
	fflush(stdout);

	// Get JVMTI environment 
	rc = vm->GetEnv((void **)&jvmti, JVMTI_VERSION);
	if (rc != JNI_OK) {
		fprintf(stderr, "ERROR: Unable to create jvmtiEnv, GetEnv failed, error=%d\n", rc);
		return -1;
	}
	
	globalJvmti = jvmti;
	
	// Retrieve system properties
	err = jvmti->GetSystemProperty("agent-verbose", &_propVerbose);
	if (err != JVMTI_ERROR_NOT_AVAILABLE)
	{
		check_jvmti_error(jvmti, err, "GetSystemProperty (agent-verbose)");
		propVerbose = atoi(_propVerbose);
		printf("Property: agent-verbose=%d\n", propVerbose);
	}
	else
	{
		propVerbose = 0;
		printf("agent-verbose property not specified, going silent.\n");
	}
	
	err = jvmti->GetSystemProperty("collector-host", &propHost);
	check_jvmti_error(jvmti, err, "GetSystemProperty (collector-host)");
	if (propVerbose>=1) printf("Property: collector-host=%s\n", propHost);
	
	err = jvmti->GetSystemProperty("agent-cache-path", &propCachePath);
	if (err != JVMTI_ERROR_NOT_AVAILABLE)
	{
		check_jvmti_error(jvmti, err, "GetSystemProperty (agent-cache-path)");
		if (propVerbose>=1) printf("Property: agent-cache-path=%s\n", propCachePath);
	}
	
	err = jvmti->GetSystemProperty("client-hostname", &propHostName);
	if (err != JVMTI_ERROR_NOT_AVAILABLE)
	{
		check_jvmti_error(jvmti, err, "GetSystemProperty (client-hostname)");
		if (propVerbose>=1) printf("Property: client-hostname=%s\n", propHostName);
	}
	else
	{
		propHostName = "no-name";
	}
	
	err = jvmti->GetSystemProperty("native-port", &propNativePort);
	check_jvmti_error(jvmti, err, "GetSystemProperty (native-port)");
	if (propVerbose>=1) printf("Property: native-port=%s\n", propNativePort);
	
	// Set capabilities
	err = jvmti->GetCapabilities(&capabilities);
	check_jvmti_error(jvmti, err, "GetCapabilities");
	
	capabilities.can_generate_all_class_hook_events = 1;
	capabilities.can_generate_exception_events = 1;
	capabilities.can_tag_objects = 1;
	err = jvmti->AddCapabilities(&capabilities);
	check_jvmti_error(jvmti, err, "AddCapabilities");

	// Set callbacks and enable event notifications 
	memset(&callbacks, 0, sizeof(callbacks));
	callbacks.ClassFileLoadHook = &cbClassFileLoadHook;
	callbacks.Exception = &cbException;
	callbacks.VMStart = &cbVMStart;
	
	err = jvmti->SetEventCallbacks(&callbacks, sizeof(callbacks));
	check_jvmti_error(jvmti, err, "SetEventCallbacks");
	
	// Enable events
 	enable_event(jvmti, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK);
	enable_event(jvmti, JVMTI_EVENT_EXCEPTION);
	enable_event(jvmti, JVMTI_EVENT_VM_START);
	
	bciConnect(propHost, propNativePort, propHostName);
	bciConfigure();

	fflush(stdout);

	return JNI_OK;
}

JNIEXPORT void JNICALL 
Agent_OnUnload(JavaVM *vm)
{
	if (gSocket)
	{
		writeByte(gSocket, FLUSH);
		flush(gSocket);
		if (propVerbose>=1) printf("Sent flush\n");
	}
}

/**
 * JVMPI initialization
 */
// jint JNICALL JVM_OnLoad(JavaVM *jvm, char *options, void *reserved)
// {
// }


//************************************************************************************

/*
Returns the next free oid value.
Thread-safe.
*/
jlong getNextOid()
{
	jlong val;
	{
		t_lock lock(oidMutex);
		val = oidCurrent++;
	}
	
	// Include host id
	val = (val << cfgHostBits) | cfgHostId; 
	
	// We cannot use the 64th bit.
	if (val >> 63 != 0) fatal_error("OID overflow");
	return val;
}

/*
 * Class: tod_core_ObjectIdentity
 * Method: get
 * Signature: (Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL Java_tod_core_ObjectIdentity_get
	(JNIEnv * jni, jclass, jobject obj)
{
	jvmtiError err;
	jvmtiEnv *jvmti = globalJvmti;
	jlong tag;
	
	err = jvmti->GetTag(obj, &tag);
	check_jvmti_error(jvmti, err, "GetTag");
	
	if (tag != 0) return tag;
	
	// Not tagged yet, assign an oid.
	tag = getNextOid();
	
	err = jvmti->SetTag(obj, tag);
	check_jvmti_error(jvmti, err, "SetTag");
	
	return -tag;
}


JNIEXPORT jint JNICALL Java_tod_core_EventInterpreter_getHostId
	(JNIEnv * jni, jclass)
{
	return cfgHostId;
}

#ifdef __cplusplus
}
#endif
