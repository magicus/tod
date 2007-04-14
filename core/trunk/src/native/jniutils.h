#ifndef __jniutils_h
#define __jniutils_h

#include <jni.h>

template <class R>
class StaticMethod
{
	protected: jclass itsClass;
	protected: jmethodID itsMethod;
	
	public: StaticMethod(
		JNIEnv* jni, 
		char* aClassName, 
		char* aMethodName, 
		char* aMethodSignature);
	
	public: virtual R invoke(JNIEnv* jni, ...) =0;
};

class StaticVoidMethod : public StaticMethod<void>
{
	public: StaticVoidMethod(
		JNIEnv* jni, 
		char* aClassName, 
		char* aMethodName, 
		char* aMethodSignature);
	
	public: virtual void invoke(JNIEnv* jni, ...);
};

#endif
