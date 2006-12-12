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
