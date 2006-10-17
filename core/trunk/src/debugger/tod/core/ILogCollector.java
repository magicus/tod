/*
 * Created on Sep 8, 2006
 */
package tod.core;

/**
 * Interface for incoming events. It closely matches {@link HighLevelCollector}.
 * @author gpothier
 */
public interface ILogCollector
{
	public void methodCall(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget,
			Object[] aArguments);
	
	public void instantiation(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget,
			Object[] aArguments);
	
	public void superCall(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget,
			Object[] aArguments);
	
	public void behaviorExit(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			int aBehaviorId,
			boolean aHasThrown,
			Object aResult);
	
	public void fieldWrite(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			int aFieldId,
			Object aTarget,
			Object aValue);
	
	public void localWrite(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aOperationBytecodeIndex,
			int aVariableId,
			Object aValue);
	
	public void exception(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			String aMethodName, 
			String aMethodSignature,
			String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex,
			Object aException);
	
	public void output(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			Output aOutput,
			byte[] aData);
	
	public void thread(
			int aThreadId, 
			long aJVMThreadId,
			String aName);
	
	public void registerString(long aObjectUID, String aString);
}
