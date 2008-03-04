/*
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
#include <jni.h>
/* Header for class tod_utils_NativeStream */

#ifndef _Included_tod_utils_NativeStream
#define _Included_tod_utils_NativeStream
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     tod_utils_NativeStream
 * Method:    fileno
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_fileno
  (JNIEnv *, jclass, jlong);

/*
 * Class:     tod_utils_NativeStream
 * Method:    fdopen
 * Signature: (Ljava/io/FileDescriptor;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_tod_utils_NativeStream_fdopen
  (JNIEnv *, jclass, jobject, jstring);

/*
 * Class:     tod_utils_NativeStream
 * Method:    setFD
 * Signature: (Ljava/io/FileDescriptor;I)V
 */
JNIEXPORT void JNICALL Java_tod_utils_NativeStream_setFD
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     tod_utils_NativeStream
 * Method:    getFD
 * Signature: (Ljava/io/FileDescriptor;)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_getFD
  (JNIEnv *, jclass, jobject);

/*
 * Class:     tod_utils_NativeStream
 * Method:    fwrite
 * Signature: (J[III)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_fwrite
  (JNIEnv *, jclass, jlong, jintArray, jint, jint);

/*
 * Class:     tod_utils_NativeStream
 * Method:    fread
 * Signature: (J[III)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_fread
  (JNIEnv *, jclass, jlong, jintArray, jint, jint);

/*
 * Class:     tod_utils_NativeStream
 * Method:    fflush
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_fflush
  (JNIEnv *, jclass, jlong);

/*
 * Class:     tod_utils_NativeStream
 * Method:    fseek
 * Signature: (JJI)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_fseek
  (JNIEnv *, jclass, jlong, jlong, jint);

/*
 * Class:     tod_utils_NativeStream
 * Method:    feof
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_feof
  (JNIEnv *, jclass, jlong);

/*
 * Class:     tod_utils_NativeStream
 * Method:    fopen
 * Signature: (Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_tod_utils_NativeStream_fopen
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     tod_utils_NativeStream
 * Method:    fclose
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_fclose
  (JNIEnv *, jclass, jlong);

/*
 * Class:     tod_utils_NativeStream
 * Method:    recv
 * Signature: (I[II)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_recv
  (JNIEnv *, jclass, jint, jintArray, jint);

/*
 * Class:     tod_utils_NativeStream
 * Method:    send
 * Signature: (I[II)I
 */
JNIEXPORT jint JNICALL Java_tod_utils_NativeStream_send
  (JNIEnv *, jclass, jint, jintArray, jint);

/*
 * Class:     tod_utils_NativeStream
 * Method:    b2i
 * Signature: ([B[I)V
 */
JNIEXPORT void JNICALL Java_tod_utils_NativeStream_b2i
  (JNIEnv *, jclass, jbyteArray, jintArray);

/*
 * Class:     tod_utils_NativeStream
 * Method:    i2b
 * Signature: ([I[B)V
 */
JNIEXPORT void JNICALL Java_tod_utils_NativeStream_i2b
  (JNIEnv *, jclass, jintArray, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
