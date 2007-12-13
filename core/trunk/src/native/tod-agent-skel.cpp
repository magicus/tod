/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
#pragma GCC java_exceptions

#include <jni.h>
#include <jvmti.h>

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

void agVMStart();
void agOnLoad();

#ifdef __cplusplus
extern "C" {
#endif

char* propHost = NULL;
char* propHostName = NULL;
char* propNativePort = NULL;
char* propCachePath = NULL;
char* _propVerbose = NULL;
int propVerbose = 2;


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
		
	}
}

void enable_event(jvmtiEnv *jvmti, jvmtiEvent event)
{
	jvmtiError err = jvmti->SetEventNotificationMode(JVMTI_ENABLE, event, NULL);
	check_jvmti_error(jvmti, err, "SetEventNotificationMode");
}


void JNICALL cbVMStart(
	jvmtiEnv *jvmti,
	JNIEnv* jni)
{
	agVMStart();
}

void JNICALL cbClassFileLoadHook(
	jvmtiEnv *jvmti, JNIEnv* jni,
	jclass class_being_redefined, jobject loader,
	const char* name, jobject protection_domain,
	jint class_data_len, const unsigned char* class_data,
	jint* new_class_data_len, unsigned char** new_class_data) 
{
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
	
	printf("Loading BCI agent - v2.2\n");
	fflush(stdout);

	// Get JVMTI environment 
	rc = vm->GetEnv((void **)&jvmti, JVMTI_VERSION);
	if (rc != JNI_OK) {
		fprintf(stderr, "ERROR: Unable to create jvmtiEnv, GetEnv failed, error=%d\n", rc);
		return -1;
	}
	
	// Retrieve system properties
	err = jvmti->GetSystemProperty("agent-verbose", &_propVerbose);
	if (err != JVMTI_ERROR_NOT_AVAILABLE)
	{
		check_jvmti_error(jvmti, err, "GetSystemProperty (agent-verbose)");
		propVerbose = atoi(_propVerbose);
		printf("Property: agent-verbose=%d\n", propVerbose);
	}
	else
	{
		propVerbose = 0;
		printf("agent-verbose property not specified, going silent.\n");
	}
	
	err = jvmti->GetSystemProperty("collector-host", &propHost);
	check_jvmti_error(jvmti, err, "GetSystemProperty (collector-host)");
	if (propVerbose>=1) printf("Property: collector-host=%s\n", propHost);
	
	err = jvmti->GetSystemProperty("agent-cache-path", &propCachePath);
	if (err != JVMTI_ERROR_NOT_AVAILABLE)
	{
		check_jvmti_error(jvmti, err, "GetSystemProperty (agent-cache-path)");
		if (propVerbose>=1) printf("Property: agent-cache-path=%s\n", propCachePath);
	}
	
	err = jvmti->GetSystemProperty("client-hostname", &propHostName);
	if (err != JVMTI_ERROR_NOT_AVAILABLE)
	{
		check_jvmti_error(jvmti, err, "GetSystemProperty (client-hostname)");
		if (propVerbose>=1) printf("Property: client-hostname=%s\n", propHostName);
	}
	else
	{
		propHostName = "no-name";
	}
	
	err = jvmti->GetSystemProperty("native-port", &propNativePort);
	check_jvmti_error(jvmti, err, "GetSystemProperty (native-port)");
	if (propVerbose>=1) printf("Property: native-port=%s\n", propNativePort);
	
	// Set capabilities
	err = jvmti->GetCapabilities(&capabilities);
	check_jvmti_error(jvmti, err, "GetCapabilities");
	
	capabilities.can_generate_all_class_hook_events = 1;
	capabilities.can_generate_exception_events = 1;
	capabilities.can_tag_objects = 1;
	err = jvmti->AddCapabilities(&capabilities);
	check_jvmti_error(jvmti, err, "AddCapabilities");

	// Set callbacks and enable event notifications 
	memset(&callbacks, 0, sizeof(callbacks));
	callbacks.ClassFileLoadHook = &cbClassFileLoadHook;
	callbacks.Exception = &cbException;
	callbacks.VMStart = &cbVMStart;
	
	err = jvmti->SetEventCallbacks(&callbacks, sizeof(callbacks));
	check_jvmti_error(jvmti, err, "SetEventCallbacks");
	
	// Enable events
 	enable_event(jvmti, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK);
	enable_event(jvmti, JVMTI_EVENT_EXCEPTION);
	enable_event(jvmti, JVMTI_EVENT_VM_START);
	
	fflush(stdout);
	
	agOnLoad();
	
	return JNI_OK;
}

JNIEXPORT void JNICALL 
Agent_OnUnload(JavaVM *vm)
{
}


#ifdef __cplusplus
}
#endif
