/*
 * Created on Oct 23, 2005
 */
package tod.core;

import java.util.List;

/**
 * A collector that actually dispathes messages to other collectors.
 * @author gpothier
 */
public class MultiCollector extends MultiRegistrer implements ILogCollector
{
	public MultiCollector(ILogCollector... aCollectors)
	{
		super (aCollectors);
	}

	public MultiCollector(List<ILogCollector> aCollectors)
	{
		super (aCollectors);
	}
	
	public void logAfterBehaviorCall(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aBehaviorLocationId, Object aTarget, Object aResult)
	{
        for (ILogCollector theCollector : getCollectors())
		{
			theCollector.logAfterBehaviorCall(aTimestamp, aThreadId, aOperationBytecodeIndex, aBehaviorLocationId, aTarget, aResult);
		}
	}

	public void logAfterBehaviorCallWithException(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aBehaviorLocationId, Object aTarget, Object aException)
	{
		for (ILogCollector theCollector : getCollectors())
		{
			theCollector.logAfterBehaviorCallWithException(aTimestamp, aThreadId, aOperationBytecodeIndex, aBehaviorLocationId, aTarget, aException);
		}
	}
	
	public void logBeforeBehaviorCall(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aBehaviorLocationId, Object aTarget, Object[] aArguments)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logBeforeBehaviorCall(aTimestamp, aThreadId, aOperationBytecodeIndex, aBehaviorLocationId, aTarget, aArguments);
        }
	}

	public void logBehaviorEnter(long aTimestamp, long aThreadId, int aBehaviorLocationId, Object aObject, Object[] aArguments)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logBehaviorEnter(aTimestamp, aThreadId, aBehaviorLocationId, aObject, aArguments);
        }
	}

	public void logBehaviorExit(long aTimestamp, long aThreadId, int aBehaviorLocationId, Object aResult)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logBehaviorExit(aTimestamp, aThreadId, aBehaviorLocationId, aResult);
        }
	}

	public void logBehaviorExitWithException(long aTimestamp, long aThreadId, int aBehaviorLocationId, Object aException)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logBehaviorExitWithException(aTimestamp, aThreadId, aBehaviorLocationId, aException);
        }
	}
	
	public void logExceptionGenerated(long aTimestamp, long aThreadId, int aBehaviorLocationId, int aOperationBytecodeIndex, Object aException)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logExceptionGenerated(aTimestamp, aThreadId, aBehaviorLocationId, aOperationBytecodeIndex, aException);
        }
	}
	
	public void logExceptionGenerated(long aTimestamp, long aThreadId, String aMethodName, String aMethodSignature, String aMethodDeclaringClassSignature, int aOperationBytecodeIndex, Object aException)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logExceptionGenerated(aTimestamp, aThreadId, aMethodName, aMethodSignature, aMethodDeclaringClassSignature, aOperationBytecodeIndex, aException);
        }
	}
	
	public void logFieldWrite(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aFieldLocationId, Object aTarget, Object aValue)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logFieldWrite(aTimestamp, aThreadId, aOperationBytecodeIndex, aFieldLocationId, aTarget, aValue);
        }
	}

	public void logLocalVariableWrite(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aVariableId, Object aValue)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logLocalVariableWrite(aTimestamp, aThreadId, aOperationBytecodeIndex, aVariableId, aValue);
        }
	}

	public void logOutput(long aTimestamp, long aThreadId, Output aOutput, byte[] aData)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logOutput(aTimestamp, aThreadId, aOutput, aData);
        }
	}

	public void logAfterBehaviorCall(long aThreadId)
	{
        for (ILogCollector theCollector : getCollectors())
        {
        	theCollector.logAfterBehaviorCall(aThreadId);
        }
	}

	public void logBeforeBehaviorCall(long aThreadId, int aOperationBytecodeIndex, int aMethodLocationId)
	{
        for (ILogCollector theCollector : getCollectors())
        {
        	theCollector.logBeforeBehaviorCall(aThreadId, aOperationBytecodeIndex, aMethodLocationId);
        }
	}

	public void logConstructorChaining(long aThreadId)
	{
        for (ILogCollector theCollector : getCollectors())
        {
        	theCollector.logConstructorChaining(aThreadId);
        }
	}

	public void logInstantiation(long aThreadId)
	{
        for (ILogCollector theCollector : getCollectors())
        {
        	theCollector.logInstantiation(aThreadId);
        }
	}

	
}
