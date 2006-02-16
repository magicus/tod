/*
 * Created on Jan 21, 2006
 */
package tod.core.transport;

import tod.core.BehaviourKind;
import tod.core.ILogCollector;
import tod.core.Output;
import tod.core.ILocationRegistrer.LineNumberInfo;
import tod.core.ILocationRegistrer.LocalVariableInfo;

/**
 * A collector that does nothing
 * @author gpothier
 */
public class DummyCollector implements ILogCollector
{

	public void logAfterBehaviorCall(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aBehaviorLocationId, Object aTarget, Object aResult)
	{
	}

	public void logAfterBehaviorCall(long aThreadId)
	{
	}

	public void logAfterBehaviorCallWithException(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aBehaviorLocationId, Object aTarget, Object aException)
	{
	}

	public void logBeforeBehaviorCall(long aThreadId, int aOperationBytecodeIndex, int aBehaviorLocationId)
	{
	}

	public void logBeforeBehaviorCall(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aBehaviorLocationId, Object aTarget, Object[] aArguments)
	{
	}

	public void logBehaviorEnter(long aTimestamp, long aThreadId, int aBehaviorLocationId, Object aObject, Object[] aArguments)
	{
	}

	public void logBehaviorExit(long aTimestamp, long aThreadId, int aBehaviorLocationId, Object aResult)
	{
	}

	public void logBehaviorExitWithException(long aTimestamp, long aThreadId, int aBehaviorLocationId, Object aException)
	{
	}

	public void logConstructorChaining(long aThreadId)
	{
	}

	public void logExceptionGenerated(long aTimestamp, long aThreadId, int aBehaviorLocationId, int aOperationBytecodeIndex, Object aException)
	{
	}

	public void logExceptionGenerated(long aTimestamp, long aThreadId, String aMethodName, String aMethodSignature, String aMethodDeclaringClassSignature, int aOperationBytecodeIndex, Object aException)
	{
	}

	public void logFieldWrite(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aFieldLocationId, Object aTarget, Object aValue)
	{
	}

	public void logInstantiation(long aThreadId)
	{
	}

	public void logLocalVariableWrite(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aVariableId, Object aValue)
	{
	}

	public void logOutput(long aTimestamp, long aThreadId, Output aOutput, byte[] aData)
	{
	}

	public void registerBehavior(BehaviourKind aBehaviourType, int aBehaviourId, int aTypeId, String aBehaviourName, String aSignature)
	{
	}

	public void registerBehaviorAttributes(int aBehaviourId, LineNumberInfo[] aLineNumberTable, LocalVariableInfo[] aLocalVariableTable)
	{
	}

	public void registerField(int aFieldId, int aTypeId, String aFieldName)
	{
	}

	public void registerFile(int aFileId, String aFileName)
	{
	}

	public void registerThread(long aThreadId, String aName)
	{
	}

	public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
	{
	}

}
