/*
 * Created on Mar 14, 2008
 */
package tod.core.transport;

import tod.agent.BehaviorCallType;
import tod.agent.EventCollector;
import tod.agent.Output;

/**
 * An interface for collecting low-level events.
 * Modeled after {@link EventCollector}, but adds a timestamp & thread id parameter to each method.
 * @author gpothier
 */
public interface ILowLevelCollector
{
	public void logClInitEnter(int aThreadId, long aTimestamp, int aBehaviorId, BehaviorCallType aCallType);
	public void logClInitExit(int aThreadId, long aTimestamp, int aProbeId, int aBehaviorId);

	public void logBehaviorEnter(int aThreadId, long aTimestamp, int aBehaviorId, BehaviorCallType aCallType, Object aObject, Object[] aArguments);
	public void logBehaviorExit(int aThreadId, long aTimestamp, int aProbeId, int aBehaviorId, Object aResult);
	public void logBehaviorExitWithException(int aThreadId, long aTimestamp, int aBehaviorId, Object aException);

	public void logExceptionGenerated(int aThreadId, long aTimestamp, String aMethodName, String aMethodSignature,
			String aMethodDeclaringClassSignature, int aOperationBytecodeIndex, Object aException);

	public void logLocalVariableWrite(int aThreadId, long aTimestamp, int aProbeId, int aVariableId, Object aValue);
	public void logFieldWrite(int aThreadId, long aTimestamp, int aProbeId, int aFieldId, Object aTarget, Object aValue);
	public void logNewArray(int aThreadId, long aTimestamp, int aProbeId, Object aTarget, int aBaseTypeId, int aSize);
	public void logArrayWrite(int aThreadId, long aTimestamp, int aProbeId, Object aTarget, int aIndex, Object aValue);

	public void logBeforeBehaviorCallDry(int aThreadId, long aTimestamp, int aProbeId, int aBehaviorId, BehaviorCallType aCallType);
	public void logAfterBehaviorCallDry(int aThreadId, long aTimestamp);

	public void logBeforeBehaviorCall(int aThreadId, long aTimestamp, int aProbeId, int aBehaviorId, BehaviorCallType aCallType, Object aTarget,
			Object[] aArguments);


	public void logAfterBehaviorCall(int aThreadId, long aTimestamp, int aProbeId, int aBehaviorId, Object aTarget, Object aResult);
	public void logAfterBehaviorCallWithException(int aThreadId, long aTimestamp, int aProbeId, int aBehaviorId, Object aTarget, Object aException);

	public void logOutput(int aThreadId, long aTimestamp, Output aOutput, byte[] aData);

	public void registerThread(int aThreadId, long aJVMThreadId, String aName);

}