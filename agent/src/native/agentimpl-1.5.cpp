/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <jni.h>
#include <jvmti.h>

#include "agentimpl.h"
#include "agent.h"
#include "jniutils.h"

#include "utils.h"

#ifdef __cplusplus
extern "C" {
#endif

// Pointer to our JVMTI environment, to be able to use it in pure JNI calls
jvmtiEnv *gJvmti;

typedef jobject (JNICALL *tCall__jobject) (JNIEnv*, jobject);
typedef jclass (JNICALL *tCall__jclass) (JNIEnv*, jobject);
typedef jint (JNICALL *tCall__jint) (JNIEnv*, jobject);
typedef void (JNICALL *tCall__void) (JNIEnv*, jobject);
typedef void (JNICALL *tCall_jlong_void) (JNIEnv*, jobject, jlong);


jmethodID methodId_Object_clone = 0;
tCall__jobject originalMethod_Object_clone = NULL;

jmethodID methodId_Object_hashCode = 0;
tCall__jint originalMethod_Object_hashCode = NULL;

jmethodID methodId_resetId = 0;

StaticVoidMethod* ThreadData_evOOSEnter = NULL;
StaticVoidMethod* ThreadData_evOOSExit_Normal = NULL;
StaticVoidMethod* ThreadData_evOOSExit_Exception = NULL;
StaticVoidMethod* ThreadData_sendResult_Ref = NULL;
StaticVoidMethod* ThreadData_sendResult_Int = NULL;


/* Every JVMTI interface returns an error code, which should be checked
 *   to avoid any cascading errors down the line.
 *   The interface GetErrorName() returns the actual enumeration constant
 *   name, making the error messages much easier to understand.
 */
void check_jvmti_error(jvmtiEnv *jvmti, jvmtiError errnum, const char *str)
{
	if ( errnum != JVMTI_ERROR_NONE ) 
	{
		char *errnum_str;
		
		errnum_str = NULL;
		(void)jvmti->GetErrorName(errnum, &errnum_str);
		
		printf("ERROR: JVMTI: %d(%s): %s\n", errnum, 
			(errnum_str==NULL?"Unknown":errnum_str),
			(str==NULL?"":str));

		fflush(stdout);
	}
}

void* agentimplAlloc(unsigned int size)
{
	unsigned char* mem_ptr;
	jvmtiError err = gJvmti->Allocate(size, &mem_ptr);
	check_jvmti_error(gJvmti, err, "Allocate");
	return mem_ptr;
}

void enable_event(jvmtiEnv *jvmti, jvmtiEvent event)
{
	jvmtiError err = jvmti->SetEventNotificationMode(JVMTI_ENABLE, event, NULL);
	check_jvmti_error(jvmti, err, "SetEventNotificationMode");
}



void JNICALL cbClassFileLoadHook(
	jvmtiEnv *jvmti, JNIEnv* jni,
	jclass class_being_redefined, jobject loader,
	const char* name, jobject protection_domain,
	jint class_data_len, const unsigned char* class_data,
	jint* new_class_data_len, unsigned char** new_class_data) 
{
	agentClassFileLoadHook(
		jni, 
		name, 
		class_data_len, 
		class_data, 
		new_class_data_len, 
		new_class_data, 
		agentimplAlloc);
}


void JNICALL cbException(
	jvmtiEnv *jvmti,
	JNIEnv* jni,
	jthread thread,
	jmethodID method,
	jlocation location,
	jobject exception,
	jmethodID catch_method,
	jlocation catch_location)
{
	if (! agentShouldProcessException(jni, method)) return;

	char* methodName;
	char* methodSignature;
	jclass methodDeclaringClass;
	char* methodDeclaringClassSignature;
 
	jvmtiJlocationFormat locationFormat;
	int bytecodeIndex = -1;
	
	// Obtain method information
	jvmti->GetMethodName(method, &methodName, &methodSignature, NULL);
	jvmti->GetMethodDeclaringClass(method, &methodDeclaringClass);
	jvmti->GetClassSignature(methodDeclaringClass, &methodDeclaringClassSignature, NULL);
	
	// Obtain location information
	jvmti->GetJLocationFormat(&locationFormat);
	if (locationFormat == JVMTI_JLOCATION_JVMBCI) bytecodeIndex = (int) location;

	agentException(
		jni, 
		methodName, 
		methodSignature, 
		methodDeclaringClass, 
		methodDeclaringClassSignature, 
		exception, 
		bytecodeIndex);
	
	// Free buffers
	jvmti->Deallocate((unsigned char*) methodName);
	jvmti->Deallocate((unsigned char*) methodSignature);
	jvmti->Deallocate((unsigned char*) methodDeclaringClassSignature);
}


void JNICALL cbVMStart(
	jvmtiEnv *jvmti,
	JNIEnv* jni)
{
	agentStart(jni);

	printf("Resolving ThreadData methods... ");
	StaticVoidMethod* TOD_enable = new StaticVoidMethod(jni, "java/tod/AgentReady", "nativeAgentLoaded", "()V");
	ThreadData_evOOSEnter = new StaticVoidMethod(jni, "java/tod/AgentReady", "evOOSEnter", "()V");
	ThreadData_evOOSExit_Normal = new StaticVoidMethod(jni, "java/tod/AgentReady", "evOOSExit_Normal", "()V");
	ThreadData_evOOSExit_Exception = new StaticVoidMethod(jni, "java/tod/AgentReady", "evOOSExit_Exception", "()V");
	ThreadData_sendResult_Ref = new StaticVoidMethod(jni, "java/tod/AgentReady", "sendResult_Ref", "(Ljava/lang/Object;)V");
	ThreadData_sendResult_Int = new StaticVoidMethod(jni, "java/tod/AgentReady", "sendResult_Int", "(I)V");
	printf("Done\n");
}

void initMethodIds(JNIEnv* jni)
{
	jclass class_Object = jni->FindClass("java/lang/Object");
	
	methodId_Object_clone = jni->GetMethodID(class_Object, "clone", "()Ljava/lang/Object;");
	methodId_resetId = jni->GetMethodID(class_Object, "$tod$resetId", "()V");
	
	methodId_Object_hashCode = jni->GetMethodID(class_Object, "hashCode", "()I");
}

void nativeEnvelopeBegin(JNIEnv *jni)
{
	if (propVerbose >= 1) printf("nativeEnvelopeBegin\n");
	if (ThreadData_evOOSEnter) ThreadData_evOOSEnter->invoke(jni);
}

// Returns true if normal exit, false if exception
bool nativeEnvelopeEnd(JNIEnv *jni)
{
	if (ThreadData_evOOSEnter)
	{
		jthrowable exception = jni->ExceptionOccurred();
		if (exception)
		{
			if (propVerbose >= 1) printf("nativeEnvelopeEnd - exception\n");
			jni->ExceptionClear();
			ThreadData_evOOSExit_Exception->invoke(jni);
			jni->Throw(exception);
			return false;
		}
		else
		{
			if (propVerbose >= 1) printf("nativeEnvelopeEnd - exit\n");
			ThreadData_evOOSExit_Normal->invoke(jni);
			return true;
		}
	}
	else return false;
}

JNIEXPORT jobject JNICALL Object_clone_wrapper(JNIEnv *jni, jobject obj) 
{
	nativeEnvelopeBegin(jni);

	// Call original clone method
	jobject clone = originalMethod_Object_clone(jni, obj);

	if (nativeEnvelopeEnd(jni))
		ThreadData_sendResult_Ref->invoke(jni, clone);

	// Determine if the object has a "real" class, as in certain cases
	// calling the resetId method on array classes crashes the JVM
	// Note that we don't care that resetId is not called on array classes
	// as they don't store the id in a field
	jclass cls = jni->GetObjectClass(obj);
	jint status;
	jvmtiError e = gJvmti->GetClassStatus(cls, &status);
	if (e != JVMTI_ERROR_NONE)
	{
		jclass ex = jni->FindClass("java/lang/Error");
		jni->ThrowNew(ex, "Exception in Object_clone_wrapper (native)");
		return 0;
	}

	// Reset id
	if (clone && (status & (JVMTI_CLASS_STATUS_ARRAY | JVMTI_CLASS_STATUS_PRIMITIVE)) == 0) 
		jni->CallObjectMethod(clone, methodId_resetId);
		
	return clone;
}

JNIEXPORT jint JNICALL Object_hashCode_wrapper(JNIEnv *jni, jobject obj) 
{
	nativeEnvelopeBegin(jni);
	
	// Call original method
	jint result = originalMethod_Object_hashCode(jni, obj);
	
	if (nativeEnvelopeEnd(jni))
		ThreadData_sendResult_Int->invoke(jni, result);
	
	return result;
}

void JNICALL cbNativeMethodBind(
	jvmtiEnv *jvmti,
	JNIEnv* jni,
	jthread thread,
	jmethodID method,
	void* address,
	void** new_address_ptr)
{
	if (jni == NULL) return;
	
	if (methodId_Object_clone == NULL) initMethodIds(jni);

	if (method == methodId_Object_clone)
	{
		originalMethod_Object_clone = (tCall__jobject) address;
		*new_address_ptr = (void*) &Object_clone_wrapper;
	}
	else if (method == methodId_Object_hashCode)
	{
		originalMethod_Object_hashCode = (tCall__jint) address;
		*new_address_ptr = (void*) &Object_hashCode_wrapper;
	} 

	return;
}

/**
 * JVMTI initialization
 */
JNIEXPORT jint JNICALL 
Agent_OnLoad(JavaVM *vm, char *options, void *reserved) 
{
	jint rc;
	jvmtiError err;
	jvmtiEventCallbacks callbacks;
	jvmtiCapabilities capabilities;
	jvmtiEnv *jvmti;
	
	// Get JVMTI environment 
	rc = vm->GetEnv((void **)&jvmti, JVMTI_VERSION);
	if (rc != JNI_OK) {
		fprintf(stderr, "ERROR: Unable to create jvmtiEnv, GetEnv failed, error=%d\n", rc);
		return -1;
	}
	
	gJvmti = jvmti;
	
	// Retrieve system properties
	char* propVerbose = NULL;
	char* propHost = NULL;
	char* propPort = NULL;
	char* propCachePath = NULL;
	char* propClientName = NULL;

	err = jvmti->GetSystemProperty("agent-verbose", &propVerbose);
	if (err != JVMTI_ERROR_NOT_AVAILABLE) check_jvmti_error(jvmti, err, "GetSystemProperty (agent-verbose)");
	
	err = jvmti->GetSystemProperty("collector-host", &propHost);
	check_jvmti_error(jvmti, err, "GetSystemProperty (collector-host)");
	
	err = jvmti->GetSystemProperty("agent-cache-path", &propCachePath);
	if (err != JVMTI_ERROR_NOT_AVAILABLE) check_jvmti_error(jvmti, err, "GetSystemProperty (agent-cache-path)");
	
	err = jvmti->GetSystemProperty("client-name", &propClientName);
	if (err != JVMTI_ERROR_NOT_AVAILABLE) check_jvmti_error(jvmti, err, "GetSystemProperty (client-name)");
	
	err = jvmti->GetSystemProperty("collector-port", &propPort);
	check_jvmti_error(jvmti, err, "GetSystemProperty (collector-port)");
	
	// Set capabilities
	err = jvmti->GetCapabilities(&capabilities);
	check_jvmti_error(jvmti, err, "GetCapabilities");
	
	capabilities.can_generate_all_class_hook_events = 1;
	capabilities.can_generate_exception_events = 1;
	capabilities.can_tag_objects = 1;
	capabilities.can_generate_native_method_bind_events = 1;
	capabilities.can_set_native_method_prefix = 1;
	err = jvmti->AddCapabilities(&capabilities);
	check_jvmti_error(jvmti, err, "AddCapabilities");

	// Set callbacks and enable event notifications 
	memset(&callbacks, 0, sizeof(callbacks));
	callbacks.ClassFileLoadHook = &cbClassFileLoadHook;
	callbacks.Exception = &cbException;
	callbacks.VMStart = &cbVMStart;
	callbacks.NativeMethodBind = &cbNativeMethodBind;
	
	err = jvmti->SetEventCallbacks(&callbacks, sizeof(callbacks));
	check_jvmti_error(jvmti, err, "SetEventCallbacks");
	
	// Enable events
 	enable_event(jvmti, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK);
	enable_event(jvmti, JVMTI_EVENT_EXCEPTION);
	enable_event(jvmti, JVMTI_EVENT_VM_START);
	enable_event(jvmti, JVMTI_EVENT_NATIVE_METHOD_BIND);
	
	// Native method prefix
	jvmti->SetNativeMethodPrefix("$todwrap$");
	
	cfgIsJVM14 = false;

	agentInit(propVerbose, propHost, propPort, propCachePath, propClientName);

	return JNI_OK;
}

JNIEXPORT void JNICALL 
Agent_OnUnload(JavaVM *vm)
{
	agentStop();
}

//************************************************************************************

jlong agentimplGetObjectId(JNIEnv* jni, jobject obj)
{
	jvmtiError err;
	jvmtiEnv *jvmti = gJvmti;
	jlong tag;
	
	err = jvmti->GetTag(obj, &tag);
	check_jvmti_error(jvmti, err, "GetTag");
	
	if (tag != 0) return tag;
	
	// Not tagged yet, assign an oid.
	tag = nextObjectId(jni, obj);
	
	err = jvmti->SetTag(obj, tag);
	check_jvmti_error(jvmti, err, "SetTag");
	
	return -tag;
}



#ifdef __cplusplus
}
#endif
