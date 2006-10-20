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
#define MessageType_LOCAL_VARIABLE_WRITE	6
#define MessageType_OUTPUT			7
	
//Arguments
#define MessageType_OBJECT_ARRAY		8
#define MessageType_SIMPLE_OBJECT		9
#define MessageType_NONE			10
	
// Argument values
#define MessageType_NULL			11
#define MessageType_BOOLEAN			12
#define MessageType_BYTE			13
#define MessageType_CHAR			14
#define MessageType_SHORT			15
#define MessageType_INT				16
#define MessageType_LONG			17
#define MessageType_FLOAT			18
#define MessageType_DOUBLE			19
#define MessageType_STRING			20
#define MessageType_OBJECT_UID			21
#define MessageType_OBJECT_HASH			22

// Registering
#define MessageType_REGISTER_CLASS		23
#define MessageType_REGISTER_BEHAVIOR		24
#define MessageType_REGISTER_FIELD		25
#define MessageType_REGISTER_FILE		26
#define MessageType_REGISTER_THREAD		27
#define MessageType_REGISTER_LOCAL_VARIABLE	28
#define MessageType_REGISTER_BEHAVIOR_ATTRIBUTES	29

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