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
	
	public void logAfterMethodCall(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aMethodLocationId, Object aTarget, Object aResult)
	{
        for (ILogCollector theCollector : getCollectors())
		{
			theCollector.logAfterMethodCall(aTimestamp, aThreadId, aOperationBytecodeIndex, aMethodLocationId, aTarget, aResult);
		}
	}

	public void logBeforeMethodCall(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aMethodLocationId, Object aTarget, Object[] aArguments)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logBeforeMethodCall(aTimestamp, aThreadId, aOperationBytecodeIndex, aMethodLocationId, aTarget, aArguments);
        }
	}

	public void logBehaviorEnter(long aTimestamp, long aThreadId, int aBehaviorLocationId)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logBehaviorEnter(aTimestamp, aThreadId, aBehaviorLocationId);
        }
	}

	public void logBehaviorExit(long aTimestamp, long aThreadId, int aBehaviorLocationId)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logBehaviorExit(aTimestamp, aThreadId, aBehaviorLocationId);
        }
	}

	public void logFieldWrite(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aFieldLocationId, Object aTarget, Object aValue)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logFieldWrite(aTimestamp, aThreadId, aOperationBytecodeIndex, aFieldLocationId, aTarget, aValue);
        }
	}

	public void logInstantiation(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aTypeLocationId, Object aInstance)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logInstantiation(aTimestamp, aThreadId, aOperationBytecodeIndex, aTypeLocationId, aInstance);
        }
	}

	public void logLocalVariableWrite(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aVariableId, Object aTarget, Object aValue)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logLocalVariableWrite(aTimestamp, aThreadId, aOperationBytecodeIndex, aVariableId, aTarget, aValue);
        }
	}

	public void logOutput(long aTimestamp, long aThreadId, Output aOutput, byte[] aData)
	{
        for (ILogCollector theCollector : getCollectors())
        {
            theCollector.logOutput(aTimestamp, aThreadId, aOutput, aData);
        }
	}

}
