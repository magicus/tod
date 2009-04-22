/*
 * Created on Apr 15, 2006
 */
package tod.experiments.bench;

public abstract class ISimpleLogCollector
{
	public abstract void logFieldWrite(long tid, long seq, int fieldId, long target, long value);
	public abstract void logVarWrite(long tid, long seq, int varId, long value);
	public abstract void logBehaviorEnter(long tid, long seq, int behaviorId, long target, long[] args);
	public abstract void logBehaviorExit(long tid, long seq, long retValue);
	
	public abstract long getStoredSize();
	
	protected long time()
	{
		return 0;
//		return System.currentTimeMillis();
//		return System.nanoTime();
	}


}
