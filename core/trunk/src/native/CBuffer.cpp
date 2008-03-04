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
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <unistd.h>

#include "CBuffer.h"
#include "tod_core_ObjectIdentity.h"

#define WRITE true

extern CachedClasses gCachedClasses;

CBuffer::CBuffer(int capacity)
{
	_capacity = capacity;
	_buffer = (char*) malloc(_capacity);
	_pointer = _buffer;
}

CBuffer::~CBuffer()
{
	free(_buffer);
}

int CBuffer::size()
{
	return _pointer - _buffer;
}

void CBuffer::ensureCapacity(int capacity)
{
	if (capacity > _capacity)
	{
		printf("resizing (old: %d, new: %d)\n", _capacity, capacity);
		int sz = size();
		char* buffer = (char*) malloc(capacity);
		memcpy(buffer, _buffer, sz);
		free(_buffer);
		_capacity = capacity;
		_buffer = buffer;
		_pointer = _buffer + sz;
	}
}

void CBuffer::reset()
{
	_pointer = _buffer;
}

char* CBuffer::getBuffer()
{
	return _buffer;
}

void CBuffer::writeBoolean(jboolean v)
{
	jbyte b = v ? 1 : 0;
	writeByte(b);
}
#define DEF_WRITE_METHOD(name, type, sz) void CBuffer::write##name (type v)\
{\
	memcpy(_pointer, &v, sz);\
	_pointer += sz;\
}

DEF_WRITE_METHOD(Byte, jbyte, 1)
DEF_WRITE_METHOD(Char, jchar, 2)
DEF_WRITE_METHOD(Short, jshort, 2)
DEF_WRITE_METHOD(Int, jint, 4)
DEF_WRITE_METHOD(Long, jlong, 8)
DEF_WRITE_METHOD(Float, jfloat, 4)
DEF_WRITE_METHOD(Double, jdouble, 8)

void CBuffer::writeString(JNIEnv* env, jstring v)
{
	jsize len = env->GetStringLength(v);
	const jchar* chars = env->GetStringChars(v, NULL);
	
	ensureCapacity(size() + len*2 + 4);
	writeInt(len);
	memcpy(_pointer, chars, len*2);
	_pointer += len*2; 
	
	env->ReleaseStringChars(v, chars);
}


#define HANDLE_VALUE_CLASS(Name, NAME, type) \
else if (env->IsInstanceOf(object, gCachedClasses.gCls##Name))\
{\
	write##Name(MessageType_##NAME);\
	type value = env->Call##Name##Method(object, gCachedClasses.gMtd##Name##Value);\
	write##Name(value);\
}

void CBuffer::writeValue(JNIEnv* env, jobject object)
{
// 	printf("cls: %d, obj: %d\n", gCachedClasses.gClsBoolean, object);
	if (object == NULL)
	{
		writeByte(MessageType_NULL);
		jclass c = gCachedClasses.gClsBoolean;
	}
// 	HANDLE_VALUE_CLASS(Boolean, BOOLEAN, jboolean)
// 	HANDLE_VALUE_CLASS(Byte, BYTE, jbyte)
// 	HANDLE_VALUE_CLASS(Char, CHAR, jchar)
// 	HANDLE_VALUE_CLASS(Short, SHORT, jshort)
// 	HANDLE_VALUE_CLASS(Int, INT, jint)
// 	HANDLE_VALUE_CLASS(Long, LONG, jlong)
// 	HANDLE_VALUE_CLASS(Float, FLOAT, jfloat)
// 	HANDLE_VALUE_CLASS(Double, DOUBLE, jdouble)
	
// 	else if (object instanceof String)
// 	{
// 		String theString = (String) object;
// 		sendMessageType(aStream, MessageType_STRING);
// 		MyObjectOutputStream theStream = new MyObjectOutputStream(aStream);
// 		theStream.writeObject(theString);
// 		theStream.drain();
// 	}
	else
	{
		writeByte(MessageType_OBJECT_UID);
		jlong objectId = 0;//Java_tod_core_ObjectIdentity_get(env, NULL, object);
		writeLong(objectId);
	}
}

void CBuffer::writeArgs(JNIEnv* env, jobjectArray args)
{
	jsize len = args ? env->GetArrayLength(args) : 0;
	ensureCapacity(size() + len*9); // Worst case: all args are 8 bytes long
	for(int i=0;i<len;i++)
	{
		jobject arg = env->GetObjectArrayElement(args, i);
		writeValue(env, arg);
	}
}

void CBuffer::behaviorExit
  (JNIEnv * env, jint threadId, 
  jlong p1, jshort p2, jlong p3, jint p4, jint p5, jboolean p6, jobject p7)
{
	if (! WRITE) return;

	writeByte(MessageType_BEHAVIOR_EXIT);
	writeInt(threadId);
	writeLong(p1);
	writeShort(p2);
	writeLong(p3);
	writeInt(p4);
	writeInt(p5);
	writeBoolean(p6);
	writeValue(env, p7);
}

void CBuffer::exception
  (JNIEnv * env, jint threadId, 
  jlong p1, jshort p2, jlong p3, jstring p4, jstring p5, jstring p6, jint p7, jobject p8)
{
	if (! WRITE) return;

	writeByte(MessageType_EXCEPTION);
	writeInt(threadId);
	writeLong(p1);
	writeShort(p2);
	writeLong(p3);
	writeString(env, p4);
	writeString(env, p5);
	writeString(env, p6);
	writeInt(p7);
	writeValue(env, p8);
}

void CBuffer::fieldWrite
  (JNIEnv * env, jint threadId, 
  jlong p1, jshort p2, jlong p3, jint p4, jint p5, jobject p6, jobject p7)
{
	if (! WRITE) return;

	writeByte(MessageType_FIELD_WRITE);
	writeInt(threadId);
	writeLong(p1);
	writeShort(p2);
	writeLong(p3);
	writeInt(p4);
	writeInt(p5);
	writeValue(env, p6);
	writeValue(env, p7);
}

void CBuffer::instantiation
  (JNIEnv * env, jint threadId, 
  jlong p1, jshort p2, jlong p3, jint p4, jboolean p5, jint p6, jint p7, jobject p8, jobjectArray p9)
{
	if (! WRITE) return;

	writeByte(MessageType_INSTANTIATION);
	writeInt(threadId);
	writeLong(p1);
	writeShort(p2);
	writeLong(p3);
	writeInt(p4);
	writeBoolean(p5);
	writeInt(p6);
	writeInt(p7);
	writeValue(env, p8);
	writeArgs(env, p9);
}

void CBuffer::localWrite
  (JNIEnv * env, jint threadId, 
  jlong p1, jshort p2, jlong p3, jint p4, jint p5, jobject p6)
{
	if (! WRITE) return;

	writeByte(MessageType_LOCAL_VARIABLE_WRITE);
	writeInt(threadId);
	writeLong(p1);
	writeShort(p2);
	writeLong(p3);
	writeInt(p4);
	writeInt(p5);
	writeValue(env, p6);
}

void CBuffer::methodCall
  (JNIEnv * env, jint threadId, 
  jlong p1, jshort p2, jlong p3, jint p4, jboolean p5, jint p6, jint p7, jobject p8, jobjectArray p9)
{
	if (! WRITE) return;

	writeByte(MessageType_METHOD_CALL);
	writeInt(threadId);
	writeLong(p1);
	writeShort(p2);
	writeLong(p3);
	writeInt(p4);
	writeBoolean(p5);
	writeInt(p6);
	writeInt(p7);
	writeValue(env, p8);
	writeArgs(env, p9);
}

void CBuffer::output
  (JNIEnv * env, jint threadId, 
  jlong p1, jshort p2, jlong p3, jobject p4, jbyteArray p5)
{
	if (! WRITE) return;

	writeByte(MessageType_OUTPUT);
	
	// TODO: finish this!
}

void CBuffer::superCall
  (JNIEnv * env, jint threadId, 
  jlong p1, jshort p2, jlong p3, jint p4, jboolean p5, jint p6, jint p7, jobject p8, jobjectArray p9)
{
	if (! WRITE) return;

	writeByte(MessageType_SUPER_CALL);
	writeInt(threadId);
	writeLong(p1);
	writeShort(p2);
	writeLong(p3);
	writeInt(p4);
	writeBoolean(p5);
	writeInt(p6);
	writeInt(p7);
	writeValue(env, p8);
	writeArgs(env, p9);
}

void CBuffer::thread
  (JNIEnv * env, jint threadId, 
  jlong p1, jstring p2)
{
	if (! WRITE) return;

	writeByte(MessageType_REGISTER_THREAD);
	writeInt(threadId);
	writeLong(p1);
	writeString(env, p2);
}

