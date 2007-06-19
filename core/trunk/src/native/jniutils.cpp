#include <string.h>
#include <stdarg.h>

#include "jniutils.h"

extern int propVerbose;

template <class R>
StaticMethod<R>::StaticMethod(
	JNIEnv* jni, 
	char* aClassName,
	char* aMethodName, 
	char* aMethodSignature)
{
	if (propVerbose>=2) printf("Loading (jni) %s\n", aClassName);
	jclass theClass = jni->FindClass(aClassName);
	if (theClass == NULL) printf("Could not load %s!\n", aClassName);
	itsClass = (jclass) jni->NewGlobalRef(theClass);
	
	itsMethod = jni->GetStaticMethodID(itsClass, aMethodName, aMethodSignature);
	if (itsMethod == NULL) printf("Could not find method %s %s!\n", aMethodName, aMethodSignature);	
}

StaticVoidMethod::StaticVoidMethod(
	JNIEnv* jni,
	char* aClassName, 
	char* aMethodName, 
	char* aMethodSignature)
: StaticMethod<void>::StaticMethod(jni, aClassName, aMethodName, aMethodSignature)
{
}

void StaticVoidMethod::invoke(JNIEnv* jni, ...)
{
	va_list args;
	va_start(args, jni);
	jni->CallStaticVoidMethodV(itsClass, itsMethod, args);
}

