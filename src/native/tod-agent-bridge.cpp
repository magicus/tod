#include <gcj/cni.h>
#include <java/lang/System.h>
#include <java/io/PrintStream.h>
#include <java/lang/Throwable.h>

#include <stdio.h>


#include "tod-agent-bridge.h"
#include "tod/agent/TodAgent.h"

#include "tod-agent-skel.h"

using namespace java::lang;
using namespace tod::agent;
using namespace std;

extern int propVerbose;


void agVMStart(long aJniEnv)
{
	try
	{
		String *message = JvNewStringLatin1("GCJ - VMStart");
		System::out->println(message);
		
		TodAgent::agVMStart(aJniEnv);
	}
	catch (Throwable *t)
	{
		System::err->println(JvNewStringLatin1("Unhandled Java exception in agVMStart:"));
		t->printStackTrace();
		System::exit(1);
	}
}

void agOnLoad(
	char* aPropHost,
	char* aPropHostName,
	char* aPropNativePort,
	char* aPropCachePath,
	int aPropVerbose)
{
	try
	{
		JvCreateJavaVM(NULL);
		JvAttachCurrentThread(NULL, NULL);
		JvInitClass(&System::class$);
		JvInitClass(&TodAgent::class$);
		
		String *message = JvNewStringLatin1("GCJ - OnLoad");
		System::out->println(message);
		
		TodAgent::agOnLoad(
			aPropHost ? JvNewStringLatin1(aPropHost) : NULL,
			aPropHostName ? JvNewStringLatin1(aPropHostName) : NULL,
			aPropNativePort ? JvNewStringLatin1(aPropNativePort) : NULL,
			aPropCachePath ? JvNewStringLatin1(aPropCachePath) : NULL,
			aPropVerbose);
	}
	catch (Throwable *t)
	{
		System::err->println(JvNewStringLatin1("Unhandled Java exception in agOnLoad:"));
		t->printStackTrace();
		System::exit(1);
	}
}

void agOnUnload()
{
	try
	{
		JvAttachCurrentThread(NULL, NULL);
		TodAgent::agOnUnload();
	}
	catch (Throwable *t)
	{
		System::err->println(JvNewStringLatin1("Unhandled Java exception in agOnUnload:"));
		t->printStackTrace();
		System::exit(1);
	}
}

void agClassLoadHook(
	long aJniEnv, 
	const char* name, 
	int class_data_len, const unsigned char* class_data,
	int* new_class_data_len, unsigned char** new_class_data) 
{
	try
	{
		JvAttachCurrentThread(NULL, NULL);
		
		jbyteArray original = JvNewByteArray(class_data_len);
		memcpy(elements(original), class_data, class_data_len);
		
		jbyteArray instrumented = TodAgent::agClassLoadHook(
			aJniEnv,
			JvNewStringLatin1(name),
			original);
			
		if (instrumented)
		{
			int len = instrumented->length;
			if (propVerbose >= 3) printf("Redefining %s (%d bytes)...\n", name, len);
			jvmtiAllocate(len, new_class_data);
			*new_class_data_len = len;
			memcpy(*new_class_data, elements(instrumented), len);
		}
		else
		{
			if (propVerbose >= 3) printf("Not redefining %s\n", name);
		}
	}
	catch (Throwable *t)
	{
		fprintf(stderr, "Unhandled Java exception in agClassLoadHook (for class %s):\n", name);
		t->printStackTrace();
	}
}

void agExceptionGenerated(long aJniEnv, long aMethodId, int aLocation, long aThrowable)
{
	try
	{
		JvAttachCurrentThread(NULL, NULL);
		TodAgent::agExceptionGenerated(aJniEnv, aMethodId, aLocation, aThrowable);
	}
	catch (Throwable *t)
	{
		System::err->println(JvNewStringLatin1("Unhandled Java exception in agExceptionGenerated:"));
		t->printStackTrace();
	}
}


long agGetNextOid()
{
	try
	{
		JvAttachCurrentThread(NULL, NULL);
		return TodAgent::agGetNextOid();
	}
	catch (Throwable *t)
	{
		System::err->println(JvNewStringLatin1("Unhandled Java exception in agGetNextOid:"));
		t->printStackTrace();
	}
}

int agGetHostId()
{
	try
	{
		JvAttachCurrentThread(NULL, NULL);
		return TodAgent::agGetHostId();
	}
	catch (Throwable *t)
	{
		System::err->println(JvNewStringLatin1("Unhandled Java exception in agGetHostId:"));
		t->printStackTrace();
	}
}

