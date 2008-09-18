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

#include <jni.h>
#include <jvmdi.h>
#include <jvmpi.h>

#include "agentimpl.h"
#include "agent.h"

#include "utils.h"

#ifdef __cplusplus
extern "C" {
#endif

// Global JVMPI and JVMDI environments, to be able to use them in pure JNI calls
JVMPI_Interface *gJvmpi;
JVMDI_Interface_1 *gJvmdi;

/* Every JVMTI interface returns an error code, which should be checked
 *   to avoid any cascading errors down the line.
 *   The interface GetErrorName() returns the actual enumeration constant
 *   name, making the error messages much easier to understand.
 */
// void check_jvmti_error(jvmtiEnv *jvmti, jvmtiError errnum, const char *str)
// {
// 	if ( errnum != JVMTI_ERROR_NONE ) 
// 	{
// 		char *errnum_str;
// 		
// 		errnum_str = NULL;
// 		(void)jvmti->GetErrorName(errnum, &errnum_str);
// 		
// 		printf("ERROR: JVMTI: %d(%s): %s\n", errnum, 
// 			(errnum_str==NULL?"Unknown":errnum_str),
// 			(str==NULL?"":str));
// 
// 		fflush(stdout);
// 	}
// }
// 
// void enable_event(jvmtiEnv *jvmti, jvmtiEvent event)
// {
// 	jvmtiError err = jvmti->SetEventNotificationMode(JVMTI_ENABLE, event, NULL);
// 	check_jvmti_error(jvmti, err, "SetEventNotificationMode");
// }
// 
// 
// 

#define CONSTANT_Class 		7
#define CONSTANT_Fieldref 	9
#define CONSTANT_Methodref 	10
#define CONSTANT_InterfaceMethodref 	11
#define CONSTANT_String 	8
#define CONSTANT_Integer 	3
#define CONSTANT_Float 		4
#define CONSTANT_Long 		5
#define CONSTANT_Double 	6
#define CONSTANT_NameAndType 	12
#define CONSTANT_Utf8 		1

uint8_t u1(unsigned char* data)
{
	return data[0];
}

uint16_t u2(unsigned char* data)
{
	return (data[0] << 8) + data[1];
}

/**
Retrieves the address of the ith item in the constant pool.
*/
unsigned char* getConstant(unsigned char* pool, int index)
{
	int len = 0;
	for(int i=0;i<index;i++)
	{
		uint8_t tag = u1(pool);
// 		printf("tag: %d\n", tag);
		pool++;
		
		switch(tag)
		{
			case CONSTANT_Class:
			case CONSTANT_String:
				pool += 2;
				break;
				
			case CONSTANT_Fieldref:
			case CONSTANT_Methodref:
			case CONSTANT_InterfaceMethodref:
			case CONSTANT_Integer:
			case CONSTANT_Float:
			case CONSTANT_NameAndType:
				pool += 4;
				break;
				
			case CONSTANT_Long:
			case CONSTANT_Double:
				pool += 8;
				i++;
				break;
				
			case CONSTANT_Utf8:
				len = u2(pool);
				pool += 2+len;
				break;
				
			default:
				printf("ERROR: unknown entry type: %d\n", tag);
				fflush(stdout);
				break;
		}
	}
	
	return pool;
}

void cbClassFileLoadHook(JVMPI_Event* event) 
{
	unsigned char* data = event->u.class_load_hook.class_data;
	jint len = event->u.class_load_hook.class_data_len;
	
// 	unsigned char* newdata = (unsigned char*) event->u.class_load_hook.malloc_f(len);
// 	memcpy(newdata, data, len);
	
	event->u.class_load_hook.new_class_data = data;
	event->u.class_load_hook.new_class_data_len = len;
	
// 	for(int i=0;i<event->u.class_load_hook.class_data_len;i++)
// 	{
// 		printf("%02x ", data[i]);
// 		if (i % 16 == 15) printf("\n");
// 	}
// 	printf("\n");

	// Find class name
	int pool_size = u2(data+8);
// 	printf("pool_size: %d\n", pool_size);
	unsigned char* pool = data+10;
	unsigned char* after_pool = getConstant(pool, pool_size-1);
	int cls_index = u2(after_pool+2);
	
	unsigned char* cls_const = getConstant(pool, cls_index-1);
	int clsname_index = u2(cls_const+1);
	
	unsigned char* clsname_const = getConstant(pool, clsname_index-1);
	int clsname_size = u2(clsname_const+1);
	char* clsname = (char*) malloc(clsname_size+1);
	memcpy(clsname, clsname_const+3, clsname_size);
	clsname[clsname_size] = 0;
	
	printf("Hook: %s\n", clsname);
	fflush(stdout);
	
	free(clsname);
	
// 	agentClassFileLoadHook(
// 		event->env_id,
// 		name, class_data_len, class_data, new_class_data_len, new_class_data);
}
// 
// 
// void JNICALL cbException(
// 	jvmtiEnv *jvmti,
// 	JNIEnv* jni,
// 	jthread thread,
// 	jmethodID method,
// 	jlocation location,
// 	jobject exception,
// 	jmethodID catch_method,
// 	jlocation catch_location)
// {
// 	if (! agentShouldProcessException(jni, method)) return;
// 
// 	char* methodName;
// 	char* methodSignature;
// 	jclass methodDeclaringClass;
// 	char* methodDeclaring;
//  
// 	jvmtiJlocationFormat locationFormat;
// 	int bytecodeIndex = -1;
// 	
// 	// Obtain method information
// 	jvmti->GetMethodName(method, &methodName, &methodSignature, NULL);
// 	jvmti->GetMethodDeclaringClass(method, &methodDeclaringClass);
// 	jvmti->GetClassSignature(methodDeclaringClass, &methodDeclaringClassSignature, NULL);
// 	
// 	// Obtain location information
// 	jvmti->GetJLocationFormat(&locationFormat);
// 	if (locationFormat == JVMTI_JLOCATION_JVMBCI) bytecodeIndex = (int) location;
// 
// 	agentException(
// 		jni, 
// 		methodName, 
// 		methodSignature, 
// 		methodDeclaringClass, 
// 		methodDeclaringClassSignature, 
// 		exception, 
// 		bytecodeIndex);
// 	
// 	// Free buffers
// 	jvmti->Deallocate((unsigned char*) methodName);
// 	jvmti->Deallocate((unsigned char*) methodSignature);
// 	jvmti->Deallocate((unsigned char*) methodDeclaringClassSignature);
// }
// 
// 
// void JNICALL cbVMStart(
// 	jvmtiEnv *jvmti,
// 	JNIEnv* jni)
// {
// 	agentStart(jni);
// }

void NotifyEvent(JVMPI_Event *event)
{
	switch(event->event_type)
	{
		case JVMPI_EVENT_CLASS_LOAD_HOOK:
			cbClassFileLoadHook(event);
			break;
		default:
			fprintf(stderr, "ERROR: unknown event type: %d\n", event->event_type);
			break;
	}
}


JNIEXPORT jint JNICALL JVM_OnLoad(JavaVM *jvm, char *options, void *reserved)
{
	// Get environments 
	int res = jvm->GetEnv((void **) &gJvmpi, JVMPI_VERSION_1);
	if (res < 0) {
		fprintf(stderr, "ERROR: Unable to get jvmpi, GetEnv failed, error=%d\n", res);
		return JNI_ERR;
	}

	res = jvm->GetEnv((void **) &gJvmdi, JVMDI_VERSION_1);
	if (res < 0) {
		fprintf(stderr, "ERROR: Unable to get jvmdi, GetEnv failed, error=%d\n", res);
		return JNI_ERR;
	}

	gJvmpi->NotifyEvent = NotifyEvent;
	gJvmpi->EnableEvent(JVMPI_EVENT_CLASS_LOAD_HOOK, NULL);
// 	// Retrieve system properties
// 	char* propVerbose = NULL;
// 	char* propHost = NULL;
// 	char* propPort = NULL;
// 	char* propCachePath = NULL;
// 	char* propClientName = NULL;
// 
// 	err = jvmti->GetSystemProperty("agent-verbose", &propVerbose);
// 	if (err != JVMTI_ERROR_NOT_AVAILABLE) check_jvmti_error(jvmti, err, "GetSystemProperty (agent-verbose)");
// 	
// 	err = jvmti->GetSystemProperty("collector-host", &propHost);
// 	check_jvmti_error(jvmti, err, "GetSystemProperty (collector-host)");
// 	
// 	err = jvmti->GetSystemProperty("agent-cache-path", &propCachePath);
// 	if (err != JVMTI_ERROR_NOT_AVAILABLE) check_jvmti_error(jvmti, err, "GetSystemProperty (agent-cache-path)");
// 	
// 	err = jvmti->GetSystemProperty("client-name", &propClientName);
// 	if (err != JVMTI_ERROR_NOT_AVAILABLE) check_jvmti_error(jvmti, err, "GetSystemProperty (client-name)");
// 	
// 	err = jvmti->GetSystemProperty("collector-port", &propPort);
// 	check_jvmti_error(jvmti, err, "GetSystemProperty (collector-port)");
// 	
// 	// Set capabilities
// 	err = jvmti->GetCapabilities(&capabilities);
// 	check_jvmti_error(jvmti, err, "GetCapabilities");
// 	
// 	capabilities.can_generate_all_class_hook_events = 1;
// 	capabilities.can_generate_exception_events = 1;
// 	capabilities.can_tag_objects = 1;
// 	err = jvmti->AddCapabilities(&capabilities);
// 	check_jvmti_error(jvmti, err, "AddCapabilities");
// 
// 	// Set callbacks and enable event notifications 
// 	memset(&callbacks, 0, sizeof(callbacks));
// 	callbacks.ClassFileLoadHook = &cbClassFileLoadHook;
// 	callbacks.Exception = &cbException;
// 	callbacks.VMStart = &cbVMStart;
// 	
// 	err = jvmti->SetEventCallbacks(&callbacks, sizeof(callbacks));
// 	check_jvmti_error(jvmti, err, "SetEventCallbacks");
// 	
// 	// Enable events
//  	enable_event(jvmti, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK);
// 	enable_event(jvmti, JVMTI_EVENT_EXCEPTION);
// 	enable_event(jvmti, JVMTI_EVENT_VM_START);
// 
// 	agentInit(propVerbose, propHost, propPort, propCachePath, propClientName);

	return JNI_OK;
}

// JNIEXPORT void JNICALL 
// Agent_OnUnload(JavaVM *vm)
// {
// 	agentStop();
// }

//************************************************************************************


jlong agentimplGetObjectId(JNIEnv* jni, jobject obj)
{
	return 0;
}

unsigned char* agentimplAlloc(int size)
{
	return NULL;
}


#ifdef __cplusplus
}
#endif
