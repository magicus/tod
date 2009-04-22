#ifndef __jniutils_h
#define __jniutils_h

#include <jni.h>

extern "C" 
{

class StaticVoidMethod
{
	protected: jclass itsClass;
	protected: jmethodID itsMethod;
	
	public: StaticVoidMethod();
	
	/* For some reason we cannot do a "new StaticVoidMethod" 
	(the JVM fails to load the library with:
	undefined symbol: _Znwj (fatal)
	(with LD_DEBUG=files)
	*/
	public: void init(
		JNIEnv* jni, 
		char* aClassName, 
		char* aMethodName, 
		char* aMethodSignature);
	
	public: void invoke(JNIEnv* jni, ...);
};

}
#endif
