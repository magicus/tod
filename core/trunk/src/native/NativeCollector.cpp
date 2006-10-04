#include <vector>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <netdb.h>

#include "tod_core_transport_NativeCollector.h"
#include "tod_core_ObjectIdentity.h"
#include "CThreadData.h"
#include "utils.h"



std::vector<CThreadData*> gThreadData;
FILE* gEventSocket;

extern int cfgVerbose;

CachedClasses gCachedClasses;

#define REG_CLASS(name, cls, mtd, sig) {\
	gCachedClasses.gCls##name = env->FindClass("java/lang/" #cls);\
	if(gCachedClasses.gCls##name == NULL) printf("Could not load " #cls "\n");\
	gCachedClasses.gMtd##name##Value = env->GetMethodID(gCachedClasses.gCls##name, mtd, "()" sig);\
	if(gCachedClasses.gMtd##name##Value == NULL) printf("Could not find value method for " #cls);\
}

/**
 Caches JNI classes and methods
*/
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_init
  (JNIEnv* env, jclass, jstring hostName, jint port)
{
	if (cfgVerbose) printf("NativeColletor.init...");
	// We cache class references of common classes
	REG_CLASS(Boolean, Boolean, "booleanValue", "Z");
	REG_CLASS(Byte, Byte, "byteValue", "B");
	REG_CLASS(Char, Character, "charValue", "C");
	REG_CLASS(Short, Short, "shortValue", "S");
	REG_CLASS(Int, Integer, "intValue", "I");
	REG_CLASS(Long, Long, "longValue", "J");
	REG_CLASS(Float, Float, "floatValue", "F");
	REG_CLASS(Double, Double, "doubleValue", "D");
	REG_CLASS(String, String, "toString", "Ljava/lang/String;");
	if (cfgVerbose) printf(" Classes cached.\n");
	
	// Open socket
	
	const char* hostNameUTF = env->GetStringUTFChars(hostName, NULL);
	
	struct sockaddr_in sin;
	struct hostent *hp;
	
	if ((hp=gethostbyname(hostNameUTF)) == NULL) fatal_error("gethostbyname\n");

	memset((char *)&sin, sizeof(sin), 0);
	sin.sin_family=hp->h_addrtype;
	memcpy((char *)&sin.sin_addr, hp->h_addr, hp->h_length);
	sin.sin_port = htons(port);
	
	int s = socket(AF_INET, SOCK_STREAM, 0);
	if (s < 0) fatal_error("socket\n");
	int r = connect(s, (sockaddr*) &sin, sizeof(sin));
	if (r < 0) fatal_ioerror("Cannot connect to event collector");
	
	gEventSocket = fdopen(s, "w");
	
	if (cfgVerbose) printf("Connected to event collector: %s:%d\n", hostNameUTF, port);
	
	env->ReleaseStringUTFChars(hostName, hostNameUTF);
	
	if (cfgVerbose) printf("NativeColletor.init done.\n");

}

/**
 This method simply ensures that the thread data vector has enough
 memory allocated.
*/
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_allocThreadData
  (JNIEnv * env, jclass cls, jint threadId)
{
	gThreadData.reserve(threadId);
	gThreadData[threadId] = new CThreadData();
}




/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    behaviorExit
 * Signature: (IJSJIIZLjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_behaviorExit
  (JNIEnv * env, jclass cls, jint threadId, 
  jlong p1, jshort p2, jlong p3, jint p4, jint p5, jboolean p6, jobject p7)
{
	CThreadData* threadData = gThreadData[threadId];
	CBuffer* buffer = threadData->packetStart();
	buffer->behaviorExit(env, threadId, p1, p2, p3, p4, p5, p6, p7);
	threadData->packetEnd();
}

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    exception
 * Signature: (IJSJLjava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_exception
  (JNIEnv * env, jclass cls, jint threadId, 
  jlong p1, jshort p2, jlong p3, jstring p4, jstring p5, jstring p6, jint p7, jobject p8)
{
	CThreadData* threadData = gThreadData[threadId];
	CBuffer* buffer = threadData->packetStart();
	buffer->exception(env, threadId, p1, p2, p3, p4, p5, p6, p7, p8);
	threadData->packetEnd();
}

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    fieldWrite
 * Signature: (IJSJIILjava/lang/Object;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_fieldWrite
  (JNIEnv * env, jclass cls, jint threadId, 
  jlong p1, jshort p2, jlong p3, jint p4, jint p5, jobject p6, jobject p7)
{
	CThreadData* threadData = gThreadData[threadId];
	CBuffer* buffer = threadData->packetStart();
	buffer->fieldWrite(env, threadId, p1, p2, p3, p4, p5, p6, p7);
	threadData->packetEnd();
}

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    instantiation
 * Signature: (IJSJIZIILjava/lang/Object;[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_instantiation
  (JNIEnv * env, jclass cls, jint threadId, 
  jlong p1, jshort p2, jlong p3, jint p4, jboolean p5, jint p6, jint p7, jobject p8, jobjectArray p9)
{
	CThreadData* threadData = gThreadData[threadId];
	CBuffer* buffer = threadData->packetStart();
	buffer->instantiation(env, threadId, p1, p2, p3, p4, p5, p6, p7, p8, p9);
	threadData->packetEnd();
}

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    localWrite
 * Signature: (IJSJIILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_localWrite
  (JNIEnv * env, jclass cls, jint threadId, 
  jlong p1, jshort p2, jlong p3, jint p4, jint p5, jobject p6)
{
	CThreadData* threadData = gThreadData[threadId];
	CBuffer* buffer = threadData->packetStart();
	buffer->localWrite(env, threadId, p1, p2, p3, p4, p5, p6);
	threadData->packetEnd();
}

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    methodCall
 * Signature: (IJSJIZIILjava/lang/Object;[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_methodCall
  (JNIEnv * env, jclass cls, jint threadId, 
  jlong p1, jshort p2, jlong p3, jint p4, jboolean p5, jint p6, jint p7, jobject p8, jobjectArray p9)
{
	CThreadData* threadData = gThreadData[threadId];
	CBuffer* buffer = threadData->packetStart();
	buffer->methodCall(env, threadId, p1, p2, p3, p4, p5, p6, p7, p8, p9);
	threadData->packetEnd();
}

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    output
 * Signature: (IJSJLtod/core/Output;[B)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_output
  (JNIEnv * env, jclass cls, jint threadId, 
  jlong p1, jshort p2, jlong p3, jobject p4, jbyteArray p5)
{
	CThreadData* threadData = gThreadData[threadId];
	CBuffer* buffer = threadData->packetStart();
	buffer->output(env, threadId, p1, p2, p3, p4, p5);
	threadData->packetEnd();
}

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    superCall
 * Signature: (IJSJIZIILjava/lang/Object;[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_superCall
  (JNIEnv * env, jclass cls, jint threadId, 
  jlong p1, jshort p2, jlong p3, jint p4, jboolean p5, jint p6, jint p7, jobject p8, jobjectArray p9)
{
	CThreadData* threadData = gThreadData[threadId];
	CBuffer* buffer = threadData->packetStart();
	buffer->superCall(env, threadId, p1, p2, p3, p4, p5, p6, p7, p8, p9);
	threadData->packetEnd();
}

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    thread
 * Signature: (IJLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_thread
  (JNIEnv * env, jclass cls, jint threadId, 
  jlong p1, jstring p2)
{
	CThreadData* threadData = gThreadData[threadId];
	CBuffer* buffer = threadData->packetStart();
	buffer->thread(env, threadId, p1, p2);
	threadData->packetEnd();
}


