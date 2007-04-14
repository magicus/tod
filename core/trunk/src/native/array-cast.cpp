#include <jni.h>
#include <string.h>

// The following is for including htons and cohorts
#include <asio.hpp>

#ifdef __cplusplus
extern "C" {
#endif

void throwEx(JNIEnv * env, const char* name, const char* msg)
{
	jclass cls = env->FindClass(name);
	env->ThrowNew(cls, msg);
}


/*
 * Class:     tod_utils_NativeStream
 * Method:    b2i
 * Signature: ([B[I)V
 */
JNIEXPORT void JNICALL Java_tod_utils_ArrayCast_b2i (
	JNIEnv * env, 
	jclass cls, 
	jbyteArray src, 
	jint srcOffset,
	jintArray dest,
	jint destOffset,
	jint len)
{
	jint src_len = env->GetArrayLength(src); 
	jint dest_len = env->GetArrayLength(dest); 
	
	if (len > src_len-srcOffset)
	{
		throwEx(env, "java.lang.IllegalArgumentException", "Specified length larger than source");
	}
	
	if (len > (dest_len-destOffset)*4)
	{
		throwEx(env, "java.lang.IllegalArgumentException", "Specified length larger than dest");
	}
	
	jbyte * s = (jbyte *) env->GetPrimitiveArrayCritical(src, 0);
	jint * d = (jint *) env->GetPrimitiveArrayCritical(dest, 0);
	
	memcpy(d+destOffset, s+srcOffset, len);
	
	env->ReleasePrimitiveArrayCritical(src, s, 0);
	env->ReleasePrimitiveArrayCritical(dest, d, 0);
}

/*
 * Class:     tod_utils_NativeStream
 * Method:    i2b
 * Signature: ([I[B)V
 */
JNIEXPORT void JNICALL Java_tod_utils_ArrayCast_i2b (
	JNIEnv * env, 
	jclass cls, 
	jintArray src, 
	jint srcOffset,
	jbyteArray dest,
	jint destOffset,
	jint len)
{
	if (src == NULL)
	{
		throwEx(env, "java.lang.IllegalArgumentException", "Source is null");
	}

	if (dest == NULL)
	{
		throwEx(env, "java.lang.IllegalArgumentException", "Destination is null");
	}

	jint src_len = env->GetArrayLength(src); 
	jint dest_len = env->GetArrayLength(dest); 
	
	if (len > (src_len-srcOffset)*4)
	{
		throwEx(env, "java.lang.IllegalArgumentException", "Specified length larger than source");
	}
	
	if (len > dest_len-destOffset)
	{
		throwEx(env, "java.lang.IllegalArgumentException", "Specified length larger than dest");
	}
	
	jint * s = (jint *) env->GetPrimitiveArrayCritical(src, 0);
	jbyte * d = (jbyte *) env->GetPrimitiveArrayCritical(dest, 0);
	
	memcpy(d+destOffset, s+srcOffset, len);
	
	env->ReleasePrimitiveArrayCritical(src, s, 0);
	env->ReleasePrimitiveArrayCritical(dest, d, 0);
}

JNIEXPORT jint JNICALL Java_tod_utils_ArrayCast_ba2i (
	JNIEnv * env, 
	jclass cls, 
	jbyteArray src)
{
	jint val;
	jbyte * s = (jbyte *) env->GetPrimitiveArrayCritical(src, 0);
	
	memcpy(&val, s, 4);
	
	env->ReleasePrimitiveArrayCritical(src, s, 0);

	return ntohl(val);
}

JNIEXPORT void JNICALL Java_tod_utils_ArrayCast_i2ba (
	JNIEnv * env, 
	jclass cls, 
	jint val,
	jbyteArray dest)
{
	val = htonl(val);
	jbyte * d = (jbyte *) env->GetPrimitiveArrayCritical(dest, 0);
	
	memcpy(d, &val, 4);
	
	env->ReleasePrimitiveArrayCritical(dest, d, 0);
}


#ifdef __cplusplus
}
#endif
