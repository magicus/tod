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
#ifndef _Included_CBuffer_h
#define _Included_CBuffer_h

#include <jni.h>


// See tod.core.transport.MessageType
// Events
#define MessageType_INSTANTIATION 		0
#define MessageType_SUPER_CALL			1
#define MessageType_METHOD_CALL			2
#define MessageType_BEHAVIOR_EXIT		3
#define MessageType_EXCEPTION			4
#define MessageType_FIELD_WRITE			5
#define MessageType_ARRAY_WRITE			6
#define MessageType_LOCAL_VARIABLE_WRITE	7
#define MessageType_OUTPUT			8
	
//Arguments
#define MessageType_OBJECT_ARRAY		9
#define MessageType_SIMPLE_OBJECT		10
#define MessageType_NONE			11
	
// Argument values
#define MessageType_NULL			12
#define MessageType_BOOLEAN			13
#define MessageType_BYTE			14
#define MessageType_CHAR			15
#define MessageType_SHORT			16
#define MessageType_INT				17
#define MessageType_LONG			18
#define MessageType_FLOAT			19
#define MessageType_DOUBLE			20
#define MessageType_STRING			21
#define MessageType_OBJECT_UID			22
#define MessageType_OBJECT_HASH			23

// Registering
#define MessageType_REGISTER_CLASS		24
#define MessageType_REGISTER_BEHAVIOR		25
#define MessageType_REGISTER_FIELD		26
#define MessageType_REGISTER_FILE		27
#define MessageType_REGISTER_THREAD		28
#define MessageType_REGISTER_LOCAL_VARIABLE	29
#define MessageType_REGISTER_BEHAVIOR_ATTRIBUTES	30

struct CachedClasses
{
	jclass gClsBoolean;
	jclass gClsByte;
	jclass gClsChar;
	jclass gClsShort;
	jclass gClsInt;
	jclass gClsLong;
	jclass gClsFloat;
	jclass gClsDouble;
	jclass gClsString;
	
	jmethodID gMtdBooleanValue;
	jmethodID gMtdByteValue;
	jmethodID gMtdCharValue;
	jmethodID gMtdShortValue;
	jmethodID gMtdIntValue;
	jmethodID gMtdLongValue;
	jmethodID gMtdFloatValue;
	jmethodID gMtdDoubleValue;
	jmethodID gMtdStringValue;
};



class CBuffer
{
	char* _buffer;
	int _capacity;
	char* _pointer;

public:
	CBuffer(int capacity);
	~CBuffer();
	
	// Grows the buffer if necessary
	void ensureCapacity(int capacity);

	// Returns the number of bytes occupied
	int size();
	
	// Sets size to 0
	void reset();
	
	char* getBuffer();
	
	void behaviorExit (JNIEnv *, jint, jlong, jshort, jlong, jint, jint, jboolean, jobject);

	void exception (JNIEnv *, jint, jlong, jshort, jlong, jstring, jstring, jstring, jint, jobject);

	void fieldWrite (JNIEnv *, jint, jlong, jshort, jlong, jint, jint, jobject, jobject);

	void instantiation (JNIEnv *, jint, jlong, jshort, jlong, jint, jboolean, jint, jint, jobject, jobjectArray);

	void localWrite (JNIEnv *, jint, jlong, jshort, jlong, jint, jint, jobject);
	
	void methodCall (JNIEnv *, jint, jlong, jshort, jlong, jint, jboolean, jint, jint, jobject, jobjectArray);
	
	void output (JNIEnv *, jint, jlong, jshort, jlong, jobject, jbyteArray);
	
	void superCall (JNIEnv *, jint, jlong, jshort, jlong, jint, jboolean, jint, jint, jobject, jobjectArray);
	
	void thread (JNIEnv *, jint, jlong, jstring);
	
private:
	void writeBoolean(jboolean v);
	void writeByte(jbyte v);
	void writeChar(jchar v);
	void writeShort(jshort v);
	void writeInt(jint v);
	void writeLong(jlong v);
	void writeFloat(jfloat v);
	void writeDouble(jdouble v);
	
	void writeString(JNIEnv* env, jstring v);
	void writeValue(JNIEnv* env, jobject object);
	void writeArgs(JNIEnv* env, jobjectArray args);

};


#endif
