/*
 * Created on Apr 15, 2006
 */
package tod.experiments.bench;

public interface ISimpleLogCollector
{
	public void logFieldWrite(long tid, long seq, int fieldId, long target, long value);
	public void logVarWrite(long tid, long seq, int varId, long value);
	public void logBehaviorEnter(long tid, long seq, int behaviorId, long target, long[] args);
	public void logBehaviorExit(long tid, long seq, long retValue);
	
	public long getStoredSize();
}
