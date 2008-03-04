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
/* Header for class tod_core_transport_NativeCollector */

#ifndef _Included_tod_core_transport_NativeCollector
#define _Included_tod_core_transport_NativeCollector
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    init
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_init
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    allocThreadData
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_allocThreadData
  (JNIEnv *, jclass, jint);

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    behaviorExit
 * Signature: (IJSJIIZLjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_behaviorExit
  (JNIEnv *, jclass, jint, jlong, jshort, jlong, jint, jint, jboolean, jobject);

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    exception
 * Signature: (IJSJLjava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_exception
  (JNIEnv *, jclass, jint, jlong, jshort, jlong, jstring, jstring, jstring, jint, jobject);

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    fieldWrite
 * Signature: (IJSJIILjava/lang/Object;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_fieldWrite
  (JNIEnv *, jclass, jint, jlong, jshort, jlong, jint, jint, jobject, jobject);

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    instantiation
 * Signature: (IJSJIZIILjava/lang/Object;[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_instantiation
  (JNIEnv *, jclass, jint, jlong, jshort, jlong, jint, jboolean, jint, jint, jobject, jobjectArray);

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    localWrite
 * Signature: (IJSJIILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_localWrite
  (JNIEnv *, jclass, jint, jlong, jshort, jlong, jint, jint, jobject);

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    methodCall
 * Signature: (IJSJIZIILjava/lang/Object;[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_methodCall
  (JNIEnv *, jclass, jint, jlong, jshort, jlong, jint, jboolean, jint, jint, jobject, jobjectArray);

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    output
 * Signature: (IJSJLtod/core/Output;[B)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_output
  (JNIEnv *, jclass, jint, jlong, jshort, jlong, jobject, jbyteArray);

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    superCall
 * Signature: (IJSJIZIILjava/lang/Object;[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_superCall
  (JNIEnv *, jclass, jint, jlong, jshort, jlong, jint, jboolean, jint, jint, jobject, jobjectArray);

/*
 * Class:     tod_core_transport_NativeCollector
 * Method:    thread
 * Signature: (IJLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_tod_core_transport_NativeCollector_thread
  (JNIEnv *, jclass, jint, jlong, jstring);

#ifdef __cplusplus
}
#endif
#endif
