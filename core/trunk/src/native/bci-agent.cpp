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
#include "md5.h"

#include <vector>

#include <iostream>
#include <fstream>
#include <asio.hpp>
#include <boost/filesystem/convenience.hpp>

// Build: g++ -shared -o ../../libbci-agent.so -I $JAVA_HOME/include/ -I $JAVA_HOME/include/linux/ bci-agent.c

using asio::ip::tcp;
namespace fs = boost::filesystem;

// Outgoing commands
static const char EXCEPTION_GENERATED = 20;
static const char INSTRUMENT_CLASS = 50;
static const char FLUSH = 99;

static const char OBJECT_HASH = 1;
static const char OBJECT_UID = 2;

// Incoming commands
static const char SET_CACHE_PATH = 80;
static const char SET_SKIP_CORE_CLASSES = 81;
static const char SET_VERBOSE = 82;
static const char SET_CAPTURE_EXCEPTIONS = 83;
static const char CONFIG_DONE = 90;

static int VM_STARTED = 0;
static STREAM* gSocket;

// Pointer to our JVMTI environment, to be able to use it in pure JNI calls
static jvmtiEnv *globalJvmti;

// Configuration data
static char* cfgCachePath = NULL;
static int cfgSkipCoreClasses = 0;
static int cfgVerbose = 2;
static int cfgCaptureExceptions = 0;

// System properties configuration data.
static char* cfgHost = NULL;
static char* cfgHostName = NULL;
static char* cfgNativePort = NULL;
static int cfgHostId = 0; // A host id assigned by the TODServer - not official.

// Class and method references
static jclass class_ExceptionGeneratedReceiver;
static jmethodID method_ExceptionGeneratedReceiver_exceptionGenerated;
static int isInExceptionCb = 0;

static jclass class_TracedMethods;
static jmethodID method_TracedMethods_setTraced;

// Method IDs for methods whose exceptions are ignored
static jmethodID ignoredExceptionMethods[3];

// Object Id mutex and current id value
static t_mutex oidMutex;
static jlong oidCurrent = 1;

// Mutex for class load callback
static t_mutex loadMutex;

// This vector holds traced methods ids for methods
// that are registered prior to VM initialization.
static std::vector<int> tmpTracedMethods;

#ifdef __cplusplus
extern "C" {
#endif


/*
Connects to the instrumenting host
host: host to connect to
hostname: name of this host, sent to the peer.
*/
void bciConnect(char* host, char* port, char* hostname)
{
	printf("Connecting to %s:%s\n", host, port);
	fflush(stdout);
	gSocket = new tcp::iostream(host, port);
	if (gSocket->fail()) fatal_error("Could not connect.\n");
	
	// Send host name
	if (cfgVerbose>=1) printf("Sending host name: %s\n", hostname);
	writeUTF(gSocket, hostname);
	flush(gSocket);
	
	cfgHostId = readInt(gSocket);
	if (cfgVerbose>=2) printf("Assigned host id: %ld\n", cfgHostId);
	if ((cfgHostId & 0xff) != cfgHostId) fatal_error("Host id overflow\n");
}


/*
* Tries to create all the directories denoted by the given name.
*/
int mkdirs(char* name)
{
	try
	{
		fs::path p(name);
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
			case SET_CACHE_PATH:
				cfgCachePath = readUTF(gSocket);
				printf("Setting cache path: %s\n", cfgCachePath);
				break;
				
			case SET_SKIP_CORE_CLASSES:
				cfgSkipCoreClasses = readByte(gSocket);
				printf("Skipping core classes: %s\n", cfgSkipCoreClasses ? "Yes" : "No");
				break;

			case SET_VERBOSE:
				cfgVerbose = readByte(gSocket);
				printf("Verbosity: %d\n", cfgVerbose);
				break;
				
			case SET_CAPTURE_EXCEPTIONS:
				cfgCaptureExceptions = readByte(gSocket);
				printf("Capture exceptions: %s\n", cfgCaptureExceptions ? "Yes" : "No");
				break;

			case CONFIG_DONE:
				printf("Config done.\n");
				return;
		}
	}
	fflush(stdout);
}

void registerTracedMethod(JNIEnv* jni, int tracedMethod)
{
	jni->CallStaticVoidMethod(
		class_TracedMethods, 
		method_TracedMethods_setTraced,
		tracedMethod);
		
	if (cfgVerbose>=3) printf("Registering traced method: %d\n", tracedMethod);
}

/**
Registers the traced methods that were registered in tmpTracedMethods
*/ 
void registerTmpTracedMethods(JNIEnv* jni)
{
	if (cfgVerbose>=1) printf("Registering %d buffered traced methods\n", tmpTracedMethods.size());
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
		if (cfgVerbose>=1 && nTracedMethods>0) printf("Registering %d traced methods\n", nTracedMethods);
		for (int i=0;i<nTracedMethods;i++)
		{
			registerTracedMethod(jni, tracedMethods[i]);
		}
	}
	else
	{
		if (cfgVerbose>=1 && nTracedMethods>0) printf("Buffering %d traced methods, will register later\n", nTracedMethods);
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

	if (cfgVerbose>=1) printf("Loading (hook) %s\n", name);
	
	int* tracedMethods = NULL;
	int nTracedMethods = 0;
	
	// Compute MD5 sum
	char md5Buffer[16];
	char md5String[33];
	md5_buffer((const char *) class_data, class_data_len, md5Buffer);
	md5_sig_to_string(md5Buffer, md5String, 33);
	if (cfgVerbose>=3) printf("MD5 sum: %s\n", md5String);
	
	// Compute cache file name	
	char cacheFileName[2000];
	char tracedCacheFileName[2000];
	cacheFileName[0] = 0;
	tracedCacheFileName[0] = 0;
	if (cfgCachePath != NULL)
	{
		int l = strlen(name);
		char escapedName[l+1];
		strcpy(escapedName, name);
		for(int i=0;i<l;i++) if (escapedName[i] == '$') escapedName[i] = '-';
		
		snprintf(cacheFileName, sizeof(cacheFileName), "%s/%s.%s.class", cfgCachePath, escapedName, md5String);
		snprintf(tracedCacheFileName, sizeof(tracedCacheFileName), "%s/%s.%s.tm", cfgCachePath, escapedName, md5String);
	}

	// Check if we have a cached version
	if (cfgCachePath != NULL)
	{
		if (cfgVerbose>=2) 
		{
			printf ("Looking for %s\n", cacheFileName);
			fflush(stdout);
		}
		
		// Check if length is 0
		if (fs::exists(cacheFileName))
		{
			int len = fs::file_size(cacheFileName);
			
			if (len == 0)
			{
				if (cfgVerbose>=2) printf ("Using original\n");
			}
			else
			{
				std::fstream f;
				
				// Read class definition
				f.open(cacheFileName, std::ios_base::in | std::ios_base::binary);
				if (f.fail()) fatal_error("Could not open class file");
				
				jvmtiError err = jvmti->Allocate(len, new_class_data);
				check_jvmti_error(jvmti, err, "Allocate");
				*new_class_data_len = len;
		
				f.read((char*) *new_class_data, len);
				if (f.eof()) fatal_ioerror("EOF on read from class file");
				if (cfgVerbose>=2) printf("Class definition uploaded from cache.\n");
				f.close();
				
				// Read traced methods array
				f.open(tracedCacheFileName, std::ios_base::in | std::ios_base::binary);
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
			if (cfgVerbose>=2) 
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
			if (cfgVerbose>=2) printf("Redefining %s...\n", name);
			jvmtiError err = jvmti->Allocate(len, new_class_data);
			check_jvmti_error(jvmti, err, "Allocate");
			*new_class_data_len = len;
			
			gSocket->read((char*) *new_class_data, len);
			if (gSocket->eof()) fatal_ioerror("fread");
			if (cfgVerbose>=2) printf("Class definition uploaded.\n");
			
			nTracedMethods = readInt(gSocket);
			tracedMethods = new int[nTracedMethods];
			for (int i=0;i<nTracedMethods;i++) tracedMethods[i] = readInt(gSocket);
			
			// Cache class
			if (cfgCachePath != NULL)
			{
				if (cfgVerbose>=2) printf("Caching %s\n", cacheFileName);
				if (! mkdirs(cacheFileName)) fatal_ioerror("Error in mkdirs");
		
				std::fstream f;
				
				// Cache bytecode
				f.open(cacheFileName, std::ios_base::out | std::ios_base::binary | std::ios_base::trunc);
				if (f.fail()) fatal_ioerror("Opening cache class file for output");
				f.write((char*) *new_class_data, len);
				if (f.bad()) fatal_ioerror("Writing cached class");
				
				f.flush();
				f.close();
				
				// Cache traced methods
				f.open(tracedCacheFileName, std::ios_base::out | std::ios_base::binary | std::ios_base::trunc);
				if (f.fail()) fatal_ioerror("Opening cache traced methods file for output");
				writeInt(&f, nTracedMethods);
				for (int i=0;i<nTracedMethods;i++) writeInt(&f, tracedMethods[i]);
				f.flush();
				f.close();
				
				if (cfgVerbose>=2) printf("Cached.\n");
			}
		}
		else if (cfgCachePath != NULL)
		{
			// Mark class as not instrumented.
			if (cfgVerbose>=2) printf("Caching empty: %s\n", cacheFileName);
			if (! mkdirs(cacheFileName)) fatal_ioerror("Error in mkdirs");
			
			std::fstream f (cacheFileName, std::ios_base::out | std::ios_base::binary | std::ios_base::trunc);
			if (f.fail()) fatal_ioerror("Opening cache class file for output");
			f.flush();
			f.close();
			if (cfgVerbose>=2) printf("Cached empty.\n");
		}
	}
	
	// Register traced methods
	registerTracedMethods(jni, nTracedMethods, tracedMethods);
	fflush(stdout);
}

void ignoreMethod(JNIEnv* jni, int index, char* className, char* methodName, char* signature)
{
	if (cfgVerbose>=2) printf("Loading (jni-ignore) %s\n", className);
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
	if (cfgVerbose>=2) printf("Loading (jni) tod.agent.ExceptionGeneratedReceiver\n");
	class_ExceptionGeneratedReceiver = jni->FindClass("tod/agent/ExceptionGeneratedReceiver");
	if (class_ExceptionGeneratedReceiver == NULL) printf("Could not load ExceptionGeneratedReceiver!\n");
	class_ExceptionGeneratedReceiver = (jclass) jni->NewGlobalRef(class_ExceptionGeneratedReceiver);
	method_ExceptionGeneratedReceiver_exceptionGenerated = 
		jni->GetStaticMethodID(
			class_ExceptionGeneratedReceiver,
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
	if (isInExceptionCb) return;
	
	if (cfgCaptureExceptions == 0) return;
	if (VM_STARTED == 0) return;
	
	if (! class_ExceptionGeneratedReceiver)
	{
		isInExceptionCb = true;
		initExceptionClasses(jni);
		isInExceptionCb = false;
	}
	
	if (cfgVerbose>=3) printf("Exception detected by native agent.\n");
	
	for (int i=0;i<sizeof(ignoredExceptionMethods);i++)
	{
		if (method == ignoredExceptionMethods[i]) return;
	}
	
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
	
	if (cfgVerbose>=1) printf("Exception generated: %s, %s, %s, %d\n", methodName, methodSignature, methodDeclaringClassSignature, bytecodeIndex);
	
	jni->CallStaticVoidMethod(
		class_ExceptionGeneratedReceiver, 
		method_ExceptionGeneratedReceiver_exceptionGenerated,
		jni->NewStringUTF(methodName),
		jni->NewStringUTF(methodSignature),
		jni->NewStringUTF(methodDeclaringClassSignature),
		bytecodeIndex,
 		exception);


	// Free buffers
	jvmti->Deallocate((unsigned char*) methodName);
	jvmti->Deallocate((unsigned char*) methodSignature);
	jvmti->Deallocate((unsigned char*) methodDeclaringClassSignature);
}


void JNICALL cbVMStart(
	jvmtiEnv *jvmti,
	JNIEnv* jni)
{
	if (cfgVerbose>=1) printf("VMStart\n");
	
	// Initialize the classes and method ids that will be used
	// for registering traced methods
	if (cfgVerbose>=2) printf("Loading (jni) tod.agent.TracedMethods\n");
	class_TracedMethods = jni->FindClass("tod/agent/TracedMethods");
	if (class_TracedMethods == NULL) printf("Could not load TracedMethods!\n");
	class_TracedMethods = (jclass) jni->NewGlobalRef(class_TracedMethods);
	method_TracedMethods_setTraced = jni->GetStaticMethodID(class_TracedMethods, "setTraced", "(I)V");
	if (method_TracedMethods_setTraced == NULL) printf("Could not find setTraced!\n");
	
	if (cfgVerbose>=1) printf("VMStart - done\n");
	fflush(stdout);
	
	VM_STARTED = 1;
	
	registerTmpTracedMethods(jni);
}

JNIEXPORT jint JNICALL 
Agent_OnLoad(JavaVM *vm, char *options, void *reserved) 
{
	jint rc;
	jvmtiError err;
	jvmtiEventCallbacks callbacks;
	jvmtiCapabilities capabilities;
	jvmtiEnv *jvmti;
	
	printf("Loading BCI agent - v2\n");
	fflush(stdout);

	/* Get JVMTI environment */
	rc = vm->GetEnv((void **)&jvmti, JVMTI_VERSION);
	if (rc != JNI_OK) {
		fprintf(stderr, "ERROR: Unable to create jvmtiEnv, GetEnv failed, error=%d\n", rc);
		return -1;
	}
	
	globalJvmti = jvmti;
	
	// Retrieve system properties
	err = jvmti->GetSystemProperty("collector-host", &cfgHost);
	check_jvmti_error(jvmti, err, "GetSystemProperty (collector-host)");
	
	err = jvmti->GetSystemProperty("tod-host", &cfgHostName);
	check_jvmti_error(jvmti, err, "GetSystemProperty (tod-host)");
	
	err = jvmti->GetSystemProperty("native-port", &cfgNativePort);
	check_jvmti_error(jvmti, err, "GetSystemProperty (native-port)");
	
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
	
	bciConnect(cfgHost, cfgNativePort, cfgHostName);
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
		if (cfgVerbose>=1) printf("Sent flush\n");
	}
}


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
	val = (val << 8) | cfgHostId; 
	
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

#ifdef __cplusplus
}
#endif
