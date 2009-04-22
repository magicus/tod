package tod.agent;

/**
 * Provides native methods that call some JNI and JVMTI functions
 * on the actual running JVM.
 * <br/>
 * If this class is changed the header file and stub should be regenerated:
 * gcjh -cp bin tod.agent.VMUtils
 * gcjh -cp bin -stubs tod.agent.VMUtils
 * 
 * @author gpothier
 */
public class VMUtils
{
	/**
	 * Calls TracedMethods.setTraced through JNI.
	 * This method should *not* be called before the VMStart
	 * event is received.
	 */
	public static native void callTracedMethods_setTraced(long aJniEnv, int aId);
	
	/**
	 * Calls ExceptionGeneratedReceiver.exceptionGenerated through JNI
	 */
	public static native void callExceptionGeneratedReceiver_exceptionGenerated(
		long aJniEnv, 
		String aMethodName,
		String aMethodSignature,
		String aMethodDeclaringClassSignature,
		int aOperationBytecodeIndex,
		long aThrowableRef);

	/**
	 * Retieves metod information through JVMTI
	 */
	public static native MethodInfo jvmtiGetMethodInfo(long aMethod);

}
