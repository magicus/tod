/*
 * Created on Sep 7, 2006
 */
package tod.core;

import tod.core.EventInterpreter.ThreadData;

/**
 * Defines the various type of possible behavior call
 * @author gpothier
 */
public enum BehaviorCallType 
{
	METHOD_CALL()
	{
		public <T extends ThreadData> void call(
				HighLevelCollector<T> aCollector,
				T aThread, 
				long aParentTimestamp,
				short aDepth,
				long aTimestamp, 
				int aOperationBytecodeIndex,
				boolean aDirectParent,
				int aCalledBehavior,
				int aExecutedBehavior,
				Object aTarget,
				Object[] aArguments)
		{
			aCollector.methodCall(
					aThread,
					aParentTimestamp,
					aDepth, 
					aTimestamp,
					aOperationBytecodeIndex,
					aDirectParent, 
					aCalledBehavior, 
					aExecutedBehavior, 
					aTarget,
					aArguments);
		}
	}, 
	SUPER_CALL()
	{
		public <T extends ThreadData> void call(
				HighLevelCollector<T> aCollector,
				T aThread, 
				long aParentTimestamp,
				short aDepth,
				long aTimestamp, 
				int aOperationBytecodeIndex,
				boolean aDirectParent,
				int aCalledBehavior,
				int aExecutedBehavior,
				Object aTarget,
				Object[] aArguments)
		{
			aCollector.superCall(
					aThread,
					aParentTimestamp,
					aDepth, 
					aTimestamp,
					aOperationBytecodeIndex,
					aDirectParent, 
					aCalledBehavior, 
					aExecutedBehavior, 
					aTarget,
					aArguments);
		}
	}, 
	INSTANTIATION()
	{
		public <T extends ThreadData> void call(
				HighLevelCollector<T> aCollector,
				T aThread, 
				long aParentTimestamp,
				short aDepth,
				long aTimestamp, 
				int aOperationBytecodeIndex,
				boolean aDirectParent,
				int aCalledBehavior,
				int aExecutedBehavior,
				Object aTarget,
				Object[] aArguments)
		{
			aCollector.instantiation(
					aThread,
					aParentTimestamp,
					aDepth, 
					aTimestamp,
					aOperationBytecodeIndex,
					aDirectParent, 
					aCalledBehavior, 
					aExecutedBehavior, 
					aTarget,
					aArguments);
		}
	};
	
	/**
	 * Performs the appropriate call on the specified collector.
	 */
	public abstract <T extends ThreadData> void call(
			HighLevelCollector<T> aCollector,
			T aThread,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments);
	
}
