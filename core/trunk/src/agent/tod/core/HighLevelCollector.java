/*
 * Created on Sep 7, 2006
 */
package tod.core;

import tod.core.EventInterpreter.ThreadData;

/**
 * Defines the interface for high-level event collectors.
 * The instrumentation code emits low-level events which are
 * interpreted by the {@link EventInterpreter}.
 * The interpreter then transforms them and sends them to
 * a {@link HighLevelCollector}.
 * @author gpothier
 */
public abstract class HighLevelCollector<T extends ThreadData>
{
	/**
	 * Creates a new {@link ThreadData} object for the {@link EventInterpreter}.
	 * @param aId Internal id of the thread. 
	 */
	public abstract T createThreadData(int aId);
	
	protected abstract void methodCall(
			T aThread, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget,
			Object[] aArguments);
	
	protected abstract void instantiation(
			T aThread, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget,
			Object[] aArguments);
	
	protected abstract void superCall(
			T aThread, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehaviorid,
			int aExecutedBehaviorId,
			Object aTarget,
			Object[] aArguments);
	
	protected abstract void behaviorExit(
			T aThread, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			int aBehaviorId,
			boolean aHasThrown,
			Object aResult);
	
	protected abstract void fieldWrite(
			T aThread, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			int aFieldId,
			Object aTarget,
			Object aValue);
	
	protected abstract void arrayWrite(
			T aThread, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			Object aTarget,
			int aIndex,
			Object aValue);
	
	protected abstract void localWrite(
			T aThread, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aOperationBytecodeIndex,
			int aVariableId,
			Object aValue);
	
	protected abstract void exception(
			T aThread, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			String aMethodName, 
			String aMethodSignature,
			String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex,
			Object aException);
	
	protected abstract void output(
			T aThread, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			Output aOutput,
			byte[] aData);
	
	protected abstract void thread(
			T aThread, 
			long aJVMThreadId,
			String aName);
}
