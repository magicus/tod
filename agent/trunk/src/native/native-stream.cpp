/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
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
#include <arpa/inet.h>

#include <jni.h>

// Build: g++ -shared -o ../../libnative-stream.so -I $JAVA_HOME/include/ -I $JAVA_HOME/include/linux/ native-stream.c



#ifdef __cplusplus
extern "C" {
#endif


/*
 * Class:     tod_utils_NativeStream
 * Method:    fileno
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_fileno
  (JNIEnv * env, jclass cls, jlong fid)
{
	FILE * f = (FILE *) fid;
	return fileno(f);
}


/*
 * Class:     tod_utils_NativeStream
 * Method:    fdopen
 * Signature: (Ljava/io/FileDescriptor;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_tod_utils_NativeStream_fdopen
  (JNIEnv * env, jclass cls, jobject fdobj, jstring mode)
{
	jclass class_fdesc = env->GetObjectClass(fdobj);
	jfieldID field_fd = env->GetFieldID(class_fdesc, "fd", "I");
	int fd = env->GetIntField(fdobj, field_fd);

	const char* m = env->GetStringUTFChars(mode, 0);

	FILE * f = fdopen(fd, m);
	
	env->ReleaseStringUTFChars(mode, m);
	
	return (jlong) f;
}

/*
 * Class:     tod_utils_NativeStream
 * Method:    setFD
 * Signature: (Ljava/io/FileDescriptor;I)V
 */
JNIEXPORT void JNICALL Java_tod_utils_NativeStream_setFD
  (JNIEnv * env, jclass cls, jobject fdobj, jint fd)
{
	jclass class_fdesc = env->GetObjectClass(fdobj);
	jfieldID field_fd = env->GetFieldID(class_fdesc, "fd", "I");
	env->SetIntField(fdobj, field_fd, fd);
}

/*
 * Class:     tod_utils_NativeStream
 * Method:    getFD
 * Signature: (Ljava/io/FileDescriptor;)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_getFD
  (JNIEnv * env, jclass cls, jobject fdobj)
{
	jclass class_fdesc = env->GetObjectClass(fdobj);
	jfieldID field_fd = env->GetFieldID(class_fdesc, "fd", "I");
	return env->GetIntField(fdobj, field_fd);
}


/*
 * Class:     tod_utils_NativeStream
 * Method:    fwrite
 * Signature: (J[III)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_fwrite
  (JNIEnv * env, jclass cls, jlong fid, jintArray buffer, jint offset, jint size)
{
	jint len = env->GetArrayLength(buffer); 
	if (len < offset+size) return -1;
	
	FILE * f = (FILE *) fid;
	jint * b = (jint *) env->GetPrimitiveArrayCritical(buffer, 0);
	
	int n = fwrite((void *) (b + offset), 4, size, f);
	
	env->ReleasePrimitiveArrayCritical(buffer, b, 0);
	
	return n;
}

/*
 * Class:     tod_utils_NativeStream
 * Method:    fread
 * Signature: (J[III)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_fread
  (JNIEnv * env, jclass cls, jlong fid, jintArray buffer, jint offset, jint size)
{
	jint len = env->GetArrayLength(buffer); 
	if (len < offset+size) return -1;
	
	FILE * f = (FILE *) fid;
	jint * b = (jint *) env->GetPrimitiveArrayCritical(buffer, 0);
	
	int n = fread((void *) (b + offset), 4, size, f);
	
	env->ReleasePrimitiveArrayCritical(buffer, b, 0);
	
	return n;
}

/*
 * Class:     tod_utils_NativeStream
 * Method:    fflush
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_fflush
  (JNIEnv * env, jclass cls, jlong fid)
{
	FILE * f = (FILE *) fid;
	return fflush(f);
}

/*
 * Class:     tod_utils_NativeStream
 * Method:    fseek
 * Signature: (JJI)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_fseek
  (JNIEnv * env, jclass cls, jlong fid, jlong offset, jint origin)
{
	FILE * f = (FILE *) fid;
	return fseek(f, offset, origin);
}

/*
 * Class:     tod_utils_NativeStream
 * Method:    feof
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_feof
  (JNIEnv * env, jclass cls, jlong fid)
{
	FILE * f = (FILE *) fid;
	return feof(f);
}


/*
 * Class:     tod_utils_NativeStream
 * Method:    fopen
 * Signature: (Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_tod_utils_NativeStream_fopen
  (JNIEnv * env, jclass cls, jstring filename, jstring mode)
{
	const char* fn = env->GetStringUTFChars(filename, 0);
	const char* m = env->GetStringUTFChars(mode, 0);
	
	FILE * f = fopen(fn, m);
	
	env->ReleaseStringUTFChars(filename, fn);
	env->ReleaseStringUTFChars(mode, m);
	
	return (jlong) f;
}

/*
 * Class:     tod_utils_NativeStream
 * Method:    fclose
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_fclose
  (JNIEnv * env, jclass cls, jlong fid)
{
	FILE * f = (FILE *) fid;
	return fclose(f);
}

/*
 * Class:     tod_utils_NativeStream
 * Method:    recv
 * Signature: (I[II)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_recv
  (JNIEnv * env, jclass cls, jint fd, jintArray buffer, jint size)
{
	jint * b = (jint *) env->GetPrimitiveArrayCritical(buffer, 0);
	int n = recv(fd, (void *) b, 4*size, MSG_WAITALL);
	env->ReleasePrimitiveArrayCritical(buffer, b, 0);

	return n;
}

/*
 * Class:     tod_utils_NativeStream
 * Method:    send
 * Signature: (I[II)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_send
  (JNIEnv * env, jclass cls, jint fd, jintArray buffer, jint size)
{
	jint * b = (jint *) env->GetPrimitiveArrayCritical(buffer, 0);
	int n = send(fd, (void *) b, 4*size, 0);
	env->ReleasePrimitiveArrayCritical(buffer, b, 0);

	return n;
}


static char buffer[10000];


static char* cpyByte(char* dest, jbyte v)
{
	memcpy(dest, &v, 1);
	return dest+1;
}

static char* cpyShort(char* dest, jshort v)
{
	memcpy(dest, &v, 2);
	return dest+2;
}

static char* cpyInt(char* dest, jint v)
{
	memcpy(dest, &v, 4);
	return dest+4;
}

static char* cpyLong(char* dest, jlong v)
{
	memcpy(dest, &v, 8);
	return dest+8;
}


static char* otherbuf = (char*) malloc(200);

/*
 * Class:     tod_experiments_NativeLog
 * Method:    log
 * Signature: (IBSJ)V
 */
JNIEXPORT void JNICALL Java_tod_experiments_NativeLog_log
  (JNIEnv * env, jclass cls, jint a, jbyte b, jshort c, jlong d)
{
	char* buf = otherbuf;
	buf = cpyInt(buf, a);
	buf = cpyByte(buf, b);
	buf = cpyShort(buf, c);
	buf = cpyLong(buf, d);
}


#ifdef __cplusplus
}
#endif
