/*
 * Created on Jan 21, 2006
 */
package tod.core.transport;

import tod.agent.AgentConfig;
import tod.agent.AgentReady;
import tod.core.HighLevelCollector;
import tod.core.Output;
import tod.core.EventInterpreter.ThreadData;

/**
 * A collector that does nothing
 * @author gpothier
 */
public class DummyCollector extends HighLevelCollector<ThreadData>
{

	public DummyCollector()
	{
		AgentReady.READY = true;
	}

	@Override
	protected void behaviorExit(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aBehaviorId, boolean aHasThrown, Object aResult)
	{
	}

	@Override
	public ThreadData createThreadData(int aId)
	{
		return new ThreadData(aId);
	}

	@Override
	protected void exception(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			String aMethodName, String aMethodSignature, String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex, Object aException)
	{
	}

	@Override
	protected void fieldWrite(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aFieldId, Object aTarget, Object aValue)
	{
	}

	@Override
	protected void instantiation(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments)
	{
	}

	@Override
	protected void localWrite(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aVariableId, Object aValue)
	{
	}

	@Override
	protected void methodCall(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments)
	{
	}

	@Override
	protected void output(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp, Output aOutput,
			byte[] aData)
	{
	}

	@Override
	protected void superCall(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorid, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments)
	{
	}

	@Override
	protected void thread(ThreadData aThread, long aJVMThreadId, String aName)
	{
	}
}
