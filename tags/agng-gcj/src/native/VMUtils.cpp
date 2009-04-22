// This file is intended to give you a head start on implementing native
// methods using CNI.
// Be aware: running 'gcjh -stubs' once more for this class may
// overwrite any edits you have made to this file.

#include <gcj/cni.h>
#include <java/lang/UnsupportedOperationException.h>
#include <java/lang/System.h>
#include <java/io/PrintStream.h>

#include <stdio.h>

#include <tod/agent/VMUtils.h>
#include <tod/agent/MethodInfo.h>

#include "tod-agent-skel.h"

using namespace java::lang;

void
tod::agent::VMUtils::callTracedMethods_setTraced(jlong aJniEnv, jint aId)
{
	VMUtils_callTracedMethods_setTraced(aJniEnv, aId);
}

void
tod::agent::VMUtils::callExceptionGeneratedReceiver_exceptionGenerated(
	jlong aJniEnv, 
	::java::lang::String * aMethodName, 
	::java::lang::String * aMethodSignature, 
	::java::lang::String * aMethodDeclaringClassSignature, 
	jint aLocation, 
	jlong aThrowable)
{
	int methodNameLen = aMethodName->length();
	char methodName[methodNameLen+1];
	JvGetStringUTFRegion(aMethodName, 0, methodNameLen, methodName);
	methodName[methodNameLen] = 0;
	
	int methodSignatureLen = aMethodSignature->length();
	char methodSignature[methodSignatureLen+1];
	JvGetStringUTFRegion(aMethodSignature, 0, methodSignatureLen, methodSignature);
	methodSignature[methodSignatureLen] = 0;
	
	int methodDeclaringClassSignatureLen = aMethodDeclaringClassSignature->length();
	char methodDeclaringClassSignature[methodDeclaringClassSignatureLen+1];
	JvGetStringUTFRegion(aMethodDeclaringClassSignature, 0, methodDeclaringClassSignatureLen, methodDeclaringClassSignature);
	methodDeclaringClassSignature[methodDeclaringClassSignatureLen] = 0;
	
	VMUtils_callExceptionGeneratedReceiver_exceptionGenerated(
		aJniEnv,
		methodName,
		methodSignature,
		methodDeclaringClassSignature,
		aLocation,
		aThrowable);
}

::tod::agent::MethodInfo *
tod::agent::VMUtils::jvmtiGetMethodInfo(jlong aId)
{
	char methodName[2048];
	char methodSignature[2048];
	char methodDeclaringClassSignature[2048];
	
	VMUtils_jvmtiGetMethodInfo(
		aId, 
		methodName, sizeof(methodName),
		methodSignature, sizeof(methodSignature),
		methodDeclaringClassSignature, sizeof(methodDeclaringClassSignature));
	
	return new ::tod::agent::MethodInfo(
		JvNewStringLatin1(methodName),
		JvNewStringLatin1(methodSignature),
		JvNewStringLatin1(methodDeclaringClassSignature));
}
