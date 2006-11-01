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
static const char CONFIG_DONE = 90;

static int VM_STARTED = 0;
static FILE* SOCKET_IN;
static FILE* SOCKET_OUT;

// Pointer to our JVMTI environment, to be able to use it in pure JNI calls
static jvmtiEnv *globalJvmti;

// Configuration data
static char* cfgCachePath = NULL;
static int cfgSkipCoreClasses = 0;
int cfgVerbose = 0;

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

// Object Id mutex and current id value
static pthread_mutex_t oidMutex = PTHREAD_MUTEX_INITIALIZER;
static long oidCurrent = 1;

// Mutex for class load callback
static pthread_mutex_t loadMutex = PTHREAD_MUTEX_INITIALIZER;

static void writeByte(int i)
{
	fputc(i & 0xff, SOCKET_OUT);
}

static void writeShort(int v)
{
	fputc(0xff & (v >> 8), SOCKET_OUT);
	fputc(0xff & v, SOCKET_OUT);
}

static void writeInt(int v)
{
	fputc(0xff & (v >> 24), SOCKET_OUT);
	fputc(0xff & (v >> 16), SOCKET_OUT);
	fputc(0xff & (v >> 8), SOCKET_OUT);
	fputc(0xff & v, SOCKET_OUT);
}

static void writeLong(jlong v)
{
	fputc(0xff & (v >> 56), SOCKET_OUT);
	fputc(0xff & (v >> 48), SOCKET_OUT);
	fputc(0xff & (v >> 40), SOCKET_OUT);
	fputc(0xff & (v >> 32), SOCKET_OUT);
	fputc(0xff & (v >> 24), SOCKET_OUT);
	fputc(0xff & (v >> 16), SOCKET_OUT);
	fputc(0xff & (v >> 8), SOCKET_OUT);
	fputc(0xff & v, SOCKET_OUT);
}

static int readByte()
{
	return fgetc(SOCKET_IN);
}

static int readShort()
{
	int a = fgetc(SOCKET_IN);
	int b = fgetc(SOCKET_IN);
	
	return (((a & 0xff) << 8) | (b & 0xff));
}

static int readInt()
{
	int a = fgetc(SOCKET_IN);
	int b = fgetc(SOCKET_IN);
	int c = fgetc(SOCKET_IN);
	int d = fgetc(SOCKET_IN);
	
	return (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
}

static void writeUTF(const char* s)
{
	int len = strlen(s);
	writeShort(len);
	fputs(s, SOCKET_OUT);
}

static char* readUTF()
{
	int len = readShort();
	char* s = (char*) malloc(len+1);
	fread(s, 1, len, SOCKET_IN);
	s[len] = 0;
	
	return s;
}


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
	if (cfgVerbose) printf("Sending host name: %s\n", hostname);
	writeUTF(hostname);
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
	// Obtain class and method ids for System.currentTimeNano, Thread.getId...
	


	while(true)
	{
		int cmd = readByte();
		switch(cmd)
		{
			case SET_CACHE_PATH:
				cfgCachePath = readUTF();
				printf("Setting cache path: %s\n", cfgCachePath);
				break;
				
			case SET_SKIP_CORE_CLASSES:
				cfgSkipCoreClasses = readByte();
				printf(cfgSkipCoreClasses ? "Skipping core classes\n" : "Not skipping core classes\n");
				break;

			case SET_VERBOSE:
				cfgVerbose = readByte();
				printf(cfgVerbose ? "Verbose\n" : "Terse\n");
				break;

			case CONFIG_DONE:
				printf("Config done.\n");
				return;
		}
	}
}

static void JNICALL
cbClassFileLoadHook(
	jvmtiEnv *jvmti, JNIEnv* jni,
	jclass class_being_redefined, jobject loader,
	const char* name, jobject protection_domain,
	jint class_data_len, const unsigned char* class_data,
	jint* new_class_data_len, unsigned char** new_class_data) 
{
	if (cfgSkipCoreClasses 
		&& (
			strncmp("java/", name, 5) == 0 
			|| strncmp("sun/", name, 4) == 0
			|| strncmp("javax/", name, 6) == 0 
			|| strncmp("com/sun/", name, 8) == 0 
		)) return;

	if (cfgVerbose) printf("Loading (hook) %s\n", name);
	
	// Compute MD5 sum
	char md5Buffer[16];
	char md5String[33];
	md5_buffer((const char *) class_data, class_data_len, md5Buffer);
	md5_sig_to_string(md5Buffer, md5String, 33);
	if (cfgVerbose) printf("MD5 sum: %s\n", md5String);
	
	// Compute cache file name	
	char cacheFileName[2000];
	cacheFileName[0] = 0;
	if (cfgCachePath != NULL)
	{
		snprintf(cacheFileName, sizeof(cacheFileName), "%s/%s.%s.class", cfgCachePath, name, md5String);
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
				if (cfgVerbose) printf ("Using original\n");
			}
			else
			{
				FILE* f = fopen(cacheFileName, "rb");
				if (f == NULL) fatal_error("Could not open file");
				
				jvmtiError err = jvmti->Allocate(len, new_class_data);
				check_jvmti_error(jvmti, err, "Allocate");
				*new_class_data_len = len;
		
				if (fread(*new_class_data, 1, len, f) != len) fatal_ioerror("fread from file");
				if (cfgVerbose) printf("Class definition uploaded from cache.\n");
				fclose(f);
			}
			return;
		}
	}
	
	pthread_mutex_lock(&loadMutex);

	// Send command
	writeByte(INSTRUMENT_CLASS);
	
	// Send class name
	writeUTF(name);
	
	// Send bytecode
	writeInt(class_data_len);
	fwrite(class_data, 1, class_data_len, SOCKET_OUT);
	fflush(SOCKET_OUT);
	
	int len = readInt();
	
	if (len > 0)
	{
		if (cfgVerbose) printf("Redefining %s...\n", name);
		jvmtiError err = jvmti->Allocate(len, new_class_data);
		check_jvmti_error(jvmti, err, "Allocate");
		*new_class_data_len = len;
		
		if (fread(*new_class_data, 1, len, SOCKET_IN) != len) fatal_ioerror("fread");
		if (cfgVerbose) printf("Class definition uploaded.\n");
		
		// Cache class
		if (cfgCachePath != NULL)
		{
			if (cfgVerbose) printf("Caching %s\n", cacheFileName);
			if (! mkdirs(cacheFileName)) fatal_ioerror("Error in mkdirs");
			FILE* f = fopen(cacheFileName, "wb");
			if (f == NULL) fatal_ioerror("Opening cache class file for output");
			if (fwrite(*new_class_data, 1, len, f) < len) fatal_ioerror("Writing cached class");
			fflush(f);
			fclose(f);
			if (cfgVerbose) printf("Cached.\n");
		}
	}
	else if (cfgCachePath != NULL)
	{
		// Mark class as not instrumented.
		if (cfgVerbose) printf("Caching empty: %s\n", cacheFileName);
		if (! mkdirs(cacheFileName)) fatal_ioerror("Error in mkdirs");
		FILE* f = fopen(cacheFileName, "wb");
		if (f == NULL) fatal_ioerror("Opening cache class file for output");
		fflush(f);
		fclose(f);
		if (cfgVerbose) printf("Cached empty.\n");
	}
	
	pthread_mutex_unlock(&loadMutex);
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
	if (VM_STARTED == 0) return;
	
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
	
	if (cfgVerbose) printf("Exception generated: %s, %s, %s, %d\n", methodName, methodSignature, methodDeclaringClassSignature, bytecodeIndex);
	
	jni->CallStaticVoidMethod(
		class_ExceptionGeneratedReceiver, 
		method_ExceptionGeneratedReceiver_exceptionGenerated,
		jni->NewStringUTF(methodName),
		jni->NewStringUTF(methodSignature),
		jni->NewStringUTF(methodDeclaringClassSignature),
		bytecodeIndex,
		exception);

/*
	// Send data
	writeByte(EXCEPTION_GENERATED);
	writeLong(timestamp);
	writeLong(threadId);
	writeUTF(methodName);
	writeUTF(methodSignature);
	writeUTF(methodDeclaringClassSignature);
	writeInt(bytecodeIndex);
	
	if (jni->IsInstanceOf(exception, class_IIdentifiableObject))
	{
		long exceptionUid = jni->CallLongMethod(exception, method_IIdentifiableObject_log_uid);
		writeByte(OBJECT_UID);
		writeLong(exceptionUid);
	}
	else
	{
		int exceptionHash = jni->CallIntMethod(exception, method_Object_hashCode);
		writeByte(OBJECT_HASH);
		writeInt(exceptionHash);
	}
	
	fflush(SOCKET_OUT);
*/

	// Free buffers
	jvmti->Deallocate((unsigned char*) methodName);
	jvmti->Deallocate((unsigned char*) methodSignature);
	jvmti->Deallocate((unsigned char*) methodDeclaringClassSignature);
}

void JNICALL
cbVMInit(
	jvmtiEnv *jvmti,
	JNIEnv* jni,
	jthread thread)
{
	if (cfgVerbose) printf("VMInit\n");
	
	
	// Initialize the classes and method ids that will be used
	// for exception processing
	if (cfgVerbose) printf("Loading (jni) java.lang.System\n");
	class_System = jni->FindClass("java/lang/System");
	class_System = (jclass) jni->NewGlobalRef(class_System);
	method_System_nanoTime = jni->GetStaticMethodID(class_System, "nanoTime", "()J");
	
	if (cfgVerbose) printf("Loading (jni) java.lang.Object\n");
	class_Object = jni->FindClass("java/lang/Object");
	class_Object = (jclass) jni->NewGlobalRef(class_Object);
	method_Object_hashCode = jni->GetMethodID(class_Object, "hashCode", "()I");

	if (cfgVerbose) printf("Loading (jni) tod.agent.ExceptionGeneratedReceiver\n");
	class_ExceptionGeneratedReceiver = jni->FindClass("tod/agent/ExceptionGeneratedReceiver");
	if (class_ExceptionGeneratedReceiver == NULL) printf("Could not load!\n");
	class_ExceptionGeneratedReceiver = (jclass) jni->NewGlobalRef(class_ExceptionGeneratedReceiver);
	method_ExceptionGeneratedReceiver_exceptionGenerated = 
		jni->GetStaticMethodID(
			class_ExceptionGeneratedReceiver,
			"exceptionGenerated", 
			"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Throwable;)V");
	
	if (cfgVerbose) printf("VMInit - done\n");
	
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

	return JNI_OK;
}

JNIEXPORT void JNICALL 
Agent_OnUnload(JavaVM *vm)
{
	if (SOCKET_OUT)
	{
		writeByte(FLUSH);
		fflush(SOCKET_OUT);
		if (cfgVerbose) printf("Sent flush\n");
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
