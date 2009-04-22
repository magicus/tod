#ifndef __tod_agent_skel_h
#define __tod_agent_skel_h

extern "C" {

void jvmtiAllocate(int aLen, unsigned char** mem_ptr);

void VMUtils_callTracedMethods_setTraced(long aJniEnv, int aId);

void VMUtils_callExceptionGeneratedReceiver_exceptionGenerated(
	long aJniEnv, 
	char* aMethodName, 
	char* aMethodSignature, 
	char* aDeclaringClassSignature, 
	int aLocation, 
	long aThrowable); 

void VMUtils_jvmtiGetMethodInfo(
	long aId, 
	char* aNameBuffer, int aNameBufferLen,
	char* aSignatureBuffer, int aSignatureBufferLen,
	char* aDeclaringClassBuffer, int aDeclaringClassBufferLen);

} // extern "C"

#endif
