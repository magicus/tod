/*
 * Created on Nov 16, 2005
 */
package tod.agent;

/**
 * This class provides a method that is called by the JNI side when
 * an exception is generated.
 * @author gpothier
 */
public class ExceptionGeneratedReceiver
{
	static
	{
		AgentConfig.getCollector();
	}
	
	public static void exceptionGenerated(
			long aTimestamp,
			String aMethodName,
			String aMethodSignature,
			String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex,
			Throwable aThrowable)
	{
		if (AgentReady.READY) AgentConfig.getInterpreter().logExceptionGenerated(
				aTimestamp, 
				aMethodName,
				aMethodSignature, 
				aMethodDeclaringClassSignature,
				aOperationBytecodeIndex, 
				aThrowable);
	}
}
