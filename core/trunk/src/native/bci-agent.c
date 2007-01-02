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
#include <string.h>
#include <unistd.h>

#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <netdb.h>
#include <pthread.h>

#include <jni.h>
#include <jvmti.h>

#include "utils.h"
#include "md5.h"

// Build: g++ -shared -o ../../libbci-agent.so -I $JAVA_HOME/include/ -I $JAVA_HOME/include/linux/ bci-agent.c


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
static FILE* SOCKET_IN;
static FILE* SOCKET_OUT;

// Pointer to our JVMTI environment, to be able to use it in pure JNI calls
static jvmtiEnv *globalJvmti;

// Configuration data
static char* cfgCachePath = NULL;
static int cfgSkipCoreClasses = 0;
int cfgVerbose = 1;
int cfgCaptureExceptions = 0;

// System properties configuration data.
static char* cfgHost = NULL;
static char* cfgHostName = NULL;
static int cfgNativePort = 0;

// Class and method references
static jclass class_System;
static jmethodID method_System_nanoTime;

static jclass class_Object;
static jmethodID method_Object_hashCode;

static jclass class_ExceptionGeneratedReceiver;
static jmethodID method_ExceptionGeneratedReceiver_exceptionGenerated;

static jclass class_TracedMethods;
static jmethodID method_TracedMethods_setTraced;

// Method IDs for methods whose exceptions are ignored
static jmethodID ignoredExceptionMethods[3];

// Object Id mutex and current id value
static pthread_mutex_t oidMutex = PTHREAD_MUTEX_INITIALIZER;
static long oidCurrent = 1;

// Mutex for class load callback
static pthread_mutex_t loadMutex = PTHREAD_MUTEX_INITIALIZER;


/*
Connects to the instrumenting host
host: host to connect to
hostname: name of this host, sent to the peer.
*/
static void bciConnect(char* host, int port, char* hostname)
{
	struct sockaddr_in sin;
	struct hostent *hp;
	
	if ((hp=gethostbyname(host)) == NULL) fatal_error("gethostbyname\n");

	memset((char *)&sin, sizeof(sin), 0);
	sin.sin_family=hp->h_addrtype;
	memcpy((char *)&sin.sin_addr, hp->h_addr, hp->h_length);
	sin.sin_port = htons(port);
	
	int s = socket(AF_INET, SOCK_STREAM, 0);
	if (s < 0) fatal_error("socket\n");
	int r = connect(s, (sockaddr*) &sin, sizeof(sin));
	if (r < 0) fatal_ioerror("Cannot connect to instrumentation server");
	
	SOCKET_IN = fdopen(s, "r");
	SOCKET_OUT = fdopen(s, "w");
	
	// Send host name
	if (cfgVerbose>=1) printf("Sending host name: %s\n", hostname);
	writeUTF(SOCKET_OUT, hostname);
	fflush(SOCKET_OUT);
}


/*
* Tries to create all the directories denoted by the given name.
*/
static int mkdirs(char* name)
{
	char* c;
	int exists = 1;
	struct stat stbuf;
	
	for(c = name+1; exists && *c != '\0'; c++)
	{
		if (*c == '/')
		{
			*c = '\0';
			if (stat(name, &stbuf) == -1) exists = mkdir(name, 0777) >= 0;
			*c = '/';
		}
	}
	return exists;
}

/* Every JVMTI interface returns an error code, which should be checked
 *   to avoid any cascading errors down the line.
 *   The interface GetErrorName() returns the actual enumeration constant
 *   name, making the error messages much easier to understand.
 */
static void
check_jvmti_error(jvmtiEnv *jvmti, jvmtiError errnum, const char *str)
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

static void
enable_event(jvmtiEnv *jvmti, jvmtiEvent event)
{
	jvmtiError err = jvmti->SetEventNotificationMode(JVMTI_ENABLE, event, NULL);
	check_jvmti_error(jvmti, err, "SetEventNotificationMode");
}

static void bciConfigure()
{
	while(true)
	{
		int cmd = readByte(SOCKET_IN);
		switch(cmd)
		{
			case SET_CACHE_PATH:
				cfgCachePath = readUTF(SOCKET_IN);
				printf("Setting cache path: %s\n", cfgCachePath);
				break;
				
			case SET_SKIP_CORE_CLASSES:
				cfgSkipCoreClasses = readByte(SOCKET_IN);
				printf("Skipping core classes: %s\n", cfgSkipCoreClasses ? "Yes" : "No");
				break;

			case SET_VERBOSE:
				cfgVerbose = readByte(SOCKET_IN);
				printf("Verbosity: %d\n", cfgVerbose);
				break;
				
			case SET_CAPTURE_EXCEPTIONS:
				cfgCaptureExceptions = readByte(SOCKET_IN);
				printf("Capture exceptions: %s\n", cfgCaptureExceptions ? "Yes" : "No");
				break;

			case CONFIG_DONE:
				printf("Config done.\n");
				return;
		}
	}
	fflush(stdout);
}

void registerTracedMethods(JNIEnv* jni, int nTracedMethods, int* tracedMethods)
{
	if (cfgVerbose>=1 && nTracedMethods>0) printf("Registering %d traced methods\n", nTracedMethods);
	for (int i=0;i<nTracedMethods;i++)
	{
		jni->CallStaticVoidMethod(
			class_TracedMethods, 
			method_TracedMethods_setTraced,
			tracedMethods[i]);
			
		if (cfgVerbose>=3) printf("Registering traced method: %d\n", tracedMethods[i]);
	}
	
	if (tracedMethods) delete tracedMethods;
}

static void JNICALL
cbClassFileLoadHook(
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
		snprintf(cacheFileName, sizeof(cacheFileName), "%s/%s.%s.class", cfgCachePath, name, md5String);
		snprintf(tracedCacheFileName, sizeof(tracedCacheFileName), "%s/%s.%s.tm", cfgCachePath, name, md5String);
	}

	// Check if we have a cached version
	if (cfgCachePath != NULL)
	{
		// Check if length is 0
		struct stat stbuf;
		if (stat(cacheFileName, &stbuf) == 0)
		{
			int len = stbuf.st_size;

			if (len == 0)
			{
				if (cfgVerbose>=2) printf ("Using original\n");
			}
			else
			{
				FILE* f = fopen(cacheFileName, "rb");
				if (f == NULL) fatal_error("Could not open class file");
				
				jvmtiError err = jvmti->Allocate(len, new_class_data);
				check_jvmti_error(jvmti, err, "Allocate");
				*new_class_data_len = len;
		
				if (fread(*new_class_data, 1, len, f) != len) fatal_ioerror("fread from file");
				if (cfgVerbose>=2) printf("Class definition uploaded from cache.\n");
				fclose(f);
				
				f = fopen(tracedCacheFileName, "rb");
				if (f == NULL) fatal_error("Could not open traced methods file");
				nTracedMethods = readInt(f);
				tracedMethods = new int[nTracedMethods];
				for (int i=0;i<nTracedMethods;i++) tracedMethods[i] = readInt(f);
				fclose(f);
			}
			
			// Register traced methods
			registerTracedMethods(jni, nTracedMethods, tracedMethods);
			
			fflush(stdout);
			return;
		}
	}
	
	pthread_mutex_lock(&loadMutex);

	// Send command
	writeByte(SOCKET_OUT, INSTRUMENT_CLASS);
	
	// Send class name
	writeUTF(SOCKET_OUT, name);
	
	// Send bytecode
	writeInt(SOCKET_OUT, class_data_len);
	fwrite(class_data, 1, class_data_len, SOCKET_OUT);
	fflush(SOCKET_OUT);
	
	int len = readInt(SOCKET_IN);
	
	if (len > 0)
	{
		if (cfgVerbose>=2) printf("Redefining %s...\n", name);
		jvmtiError err = jvmti->Allocate(len, new_class_data);
		check_jvmti_error(jvmti, err, "Allocate");
		*new_class_data_len = len;
		
		if (fread(*new_class_data, 1, len, SOCKET_IN) != len) fatal_ioerror("fread");
		if (cfgVerbose>=2) printf("Class definition uploaded.\n");
		
		nTracedMethods = readInt(SOCKET_IN);
		tracedMethods = new int[nTracedMethods];
		for (int i=0;i<nTracedMethods;i++) tracedMethods[i] = readInt(SOCKET_IN);

		
		// Cache class
		if (cfgCachePath != NULL)
		{
			if (cfgVerbose>=2) printf("Caching %s\n", cacheFileName);
			if (! mkdirs(cacheFileName)) fatal_ioerror("Error in mkdirs");
			
			// Cache bytecode
			FILE* f = fopen(cacheFileName, "wb");
			if (f == NULL) fatal_ioerror("Opening cache class file for output");
			if (fwrite(*new_class_data, 1, len, f) < len) fatal_ioerror("Writing cached class");
			fflush(f);
			fclose(f);
			
			// Cache traced methods
			f = fopen(tracedCacheFileName, "wb");
			if (f == NULL) fatal_ioerror("Opening cache traced methods file for output");
			writeInt(f, nTracedMethods);
			for (int i=0;i<nTracedMethods;i++) writeInt(f, tracedMethods[i]);
			fflush(f);
			fclose(f);
			
			if (cfgVerbose>=2) printf("Cached.\n");
		}
	}
	else if (cfgCachePath != NULL)
	{
		// Mark class as not instrumented.
		if (cfgVerbose>=2) printf("Caching empty: %s\n", cacheFileName);
		if (! mkdirs(cacheFileName)) fatal_ioerror("Error in mkdirs");
		FILE* f = fopen(cacheFileName, "wb");
		if (f == NULL) fatal_ioerror("Opening cache class file for output");
		fflush(f);
		fclose(f);
		if (cfgVerbose>=2) printf("Cached empty.\n");
	}
	
	pthread_mutex_unlock(&loadMutex);
	
	// Register traced methods
	registerTracedMethods(jni, nTracedMethods, tracedMethods);
	fflush(stdout);
}

void JNICALL
cbException(
	jvmtiEnv *jvmti,
	JNIEnv* jni,
	jthread thread,
	jmethodID method,
	jlocation location,
	jobject exception,
	jmethodID catch_method,
	jlocation catch_location)
{
	if (cfgCaptureExceptions == 0) return;
	if (VM_STARTED == 0) return;
	
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

static void ignoreMethod(JNIEnv* jni, int index, char* className, char* methodName, char* signature)
{
	if (cfgVerbose>=2) printf("Loading (jni-ignore) %s\n", className);
	jclass clazz = jni->FindClass(className);
	if (clazz == NULL) printf("Could not load %s\n", className);
	jmethodID method = jni->GetMethodID(clazz, methodName, signature);
	if (method == NULL) printf("Could not find %s.%s%s\n", className, methodName, signature);
	jni->DeleteLocalRef(clazz);

	ignoredExceptionMethods[index] = method;
}


static void initIgnoredMethods(JNIEnv* jni)
{
	int i=0;
	ignoreMethod(jni, i++, "java/lang/ClassLoader", "findBootstrapClass", "(Ljava/lang/String;)Ljava/lang/Class;");
	ignoreMethod(jni, i++, "java/net/URLClassLoader$1", "run", "()Ljava/lang/Object;");
	ignoreMethod(jni, i++, "java/net/URLClassLoader", "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");
}


void JNICALL
cbVMInit(
	jvmtiEnv *jvmti,
	JNIEnv* jni,
	jthread thread)
{
	if (cfgVerbose>=1) printf("VMInit\n");
	
	
	// Initialize the classes and method ids that will be used
	// for exception processing
	if (cfgVerbose>=2) printf("Loading (jni) java.lang.System\n");
	class_System = jni->FindClass("java/lang/System");
	class_System = (jclass) jni->NewGlobalRef(class_System);
	method_System_nanoTime = jni->GetStaticMethodID(class_System, "nanoTime", "()J");
	
	if (cfgVerbose>=2) printf("Loading (jni) java.lang.Object\n");
	class_Object = jni->FindClass("java/lang/Object");
	class_Object = (jclass) jni->NewGlobalRef(class_Object);
	method_Object_hashCode = jni->GetMethodID(class_Object, "hashCode", "()I");

	if (cfgVerbose>=2) printf("Loading (jni) tod.agent.TracedMethods\n");
	class_TracedMethods = jni->FindClass("tod/agent/TracedMethods");
	if (class_TracedMethods == NULL) printf("Could not load TracedMethods!\n");
	class_TracedMethods = (jclass) jni->NewGlobalRef(class_TracedMethods);
	method_TracedMethods_setTraced = jni->GetStaticMethodID(class_TracedMethods, "setTraced", "(I)V");
	if (method_TracedMethods_setTraced == NULL) printf("Could not find setTraced!\n");
	
	if (cfgVerbose>=2) printf("Loading (jni) tod.agent.ExceptionGeneratedReceiver\n");
	class_ExceptionGeneratedReceiver = jni->FindClass("tod/agent/ExceptionGeneratedReceiver");
	if (class_ExceptionGeneratedReceiver == NULL) printf("Could not load ExceptionGeneratedReceiver!\n");
	class_ExceptionGeneratedReceiver = (jclass) jni->NewGlobalRef(class_ExceptionGeneratedReceiver);
	method_ExceptionGeneratedReceiver_exceptionGenerated = 
		jni->GetStaticMethodID(
			class_ExceptionGeneratedReceiver,
			"exceptionGenerated", 
			"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Throwable;)V");
	
	initIgnoredMethods(jni);
	
	if (cfgVerbose>=1) printf("VMInit - done\n");
	fflush(stdout);
	
	VM_STARTED = 1;
}


JNIEXPORT jint JNICALL 
Agent_OnLoad(JavaVM *vm, char *options, void *reserved) 
{
	jint rc;
	jvmtiError err;
	jvmtiEventCallbacks callbacks;
	jvmtiCapabilities capabilities;
	jvmtiEnv *jvmti;
	
	printf("Loading BCI agent\n");

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
	
	char* s;
	err = jvmti->GetSystemProperty("native-port", &s);
	check_jvmti_error(jvmti, err, "GetSystemProperty (native-port)");
	cfgNativePort = atoi(s);
	err = jvmti->Deallocate((unsigned char*) s);
	check_jvmti_error(jvmti, err, "Deallocate (native-port)");
	
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
	callbacks.VMInit = &cbVMInit;
	
	err = jvmti->SetEventCallbacks(&callbacks, sizeof(callbacks));
	check_jvmti_error(jvmti, err, "SetEventCallbacks");
	
	// Enable events
	enable_event(jvmti, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK);
	enable_event(jvmti, JVMTI_EVENT_EXCEPTION);
	enable_event(jvmti, JVMTI_EVENT_VM_INIT);
	
	bciConnect(cfgHost, cfgNativePort, cfgHostName);
	bciConfigure();

	fflush(stdout);

	return JNI_OK;
}

JNIEXPORT void JNICALL 
Agent_OnUnload(JavaVM *vm)
{
	if (SOCKET_OUT)
	{
		writeByte(SOCKET_OUT, FLUSH);
		fflush(SOCKET_OUT);
		if (cfgVerbose>=1) printf("Sent flush\n");
	}
}


//************************************************************************************

/*
Returns the next free oid value.
Thread-safe.
*/
static long getNextOid()
{
	pthread_mutex_lock(&oidMutex);
	long val = oidCurrent++;
	if (val < 0) val = 0;
	pthread_mutex_unlock(&oidMutex);
	
	if (val == 0) fatal_error("OID overflow");
	return val;
}



#ifdef __cplusplus
extern "C" {
#endif

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
