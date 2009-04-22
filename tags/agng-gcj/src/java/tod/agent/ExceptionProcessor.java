/*
 * Created on Dec 14, 2007
 */
package tod.agent;

import java.util.ArrayList;
import java.util.List;

public class ExceptionProcessor
{
	private final NativeAgentConfig itsConfig;
	
	/**
	 * List of methods for which exceptions should be ignored.
	 * (Many exceptions occur during the normal classloading process, which we do
	 * not want to register).
	 */
	private final List<MethodInfo> itsIgnoredMethods = new ArrayList<MethodInfo>();
	
	private ThreadLocal<Boolean> itsInExceptionCb = new ThreadLocal<Boolean>()
	{
		@Override
		protected Boolean initialValue()
		{
			return Boolean.FALSE;
		}
	};
	
	public ExceptionProcessor(NativeAgentConfig aConfig)
	{
		itsConfig = aConfig;
		itsIgnoredMethods.add(new MethodInfo(
				"findBootstrapClass", 
				"(Ljava/lang/String;)Ljava/lang/Class;",
				"java/lang/ClassLoader"));
		
		itsIgnoredMethods.add(new MethodInfo(
				"run", 
				"()Ljava/lang/Object;", 
				"java/net/URLClassLoader$1"));
		
		itsIgnoredMethods.add(new MethodInfo(
				"findClass", 
				"(Ljava/lang/String;)Ljava/lang/Class;",
				"java/net/URLClassLoader"));
	}

	public void agExceptionGenerated(long aJniEnv, MethodInfo aMethodInfo, int aLocation, long aThrowable)
	{
		itsConfig.log(3, "[TOD] TodAgent: Exception detected in "+aMethodInfo.declaringClassSignature+"."+aMethodInfo.methodName);
		
		if (itsInExceptionCb.get())
		{
			System.err.println("Recursive exeception event - probable cause is that the connection to the TOD database has been lost. Exiting.");
			System.exit(1);
		}
		
		itsInExceptionCb.set(Boolean.TRUE);
		
		for (MethodInfo theMethodInfo : itsIgnoredMethods)
		{
			itsConfig.log(2, "[TOD] Ignoring exception in "+theMethodInfo);
			if (theMethodInfo.equals(aMethodInfo)) return;
		}
		
		VMUtils.callExceptionGeneratedReceiver_exceptionGenerated(
				aJniEnv,
				aMethodInfo.methodName, 
				aMethodInfo.methodSignature, 
				aMethodInfo.declaringClassSignature, 
				aLocation, 
				aThrowable);
		
		itsInExceptionCb.set(Boolean.FALSE);
	}
}
