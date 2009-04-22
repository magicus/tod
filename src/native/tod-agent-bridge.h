#ifndef __tod_agent_bridge_h
#define __tod_agent_bridge_h

/*
Bridge between tod-agent-skel and TodAgent.java
(We cannot have a single translation unit with both gcj and jni inculdes)
*/

/*
Called when the agent is loaded by the VM
*/
void agOnLoad(
	char* aPropHost,
	char* aPropHostName,
	char* aPropNativePort,
	char* aPropCachePath,
	int aPropVerbose);

void agOnUnload();

void agVMStart(long aJniEnv);

void agClassLoadHook(
	long aJniEnv, 
	const char* name, 
	int class_data_len, const unsigned char* class_data,
	int* new_class_data_len, unsigned char** new_class_data);
	
long agGetNextOid();

void agExceptionGenerated(long aJniEnv, long aMethodId, int aLocation, long aThrowable);

int agGetHostId();


#endif
