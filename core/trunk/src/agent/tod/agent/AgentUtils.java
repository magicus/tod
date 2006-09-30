/*
 * Created on Sep 8, 2006
 */
package tod.agent;

public class AgentUtils
{
	/**
	 * Adjusts a timestamp value so as no two events of the same
	 * thread can have the same timestamp (see paper for details).
	 */
	public static long transformTimestamp(long aTimestamp, byte aSerial)
	{
		if (aSerial > AgentConfig.TIMESTAMP_ADJUST_MASK) throw new RuntimeException("Timestamp adjust overflow");
		
		return ((aTimestamp << AgentConfig.TIMESTAMP_ADJUST_SHIFT)
				& ~AgentConfig.TIMESTAMP_ADJUST_MASK)
				| aSerial & AgentConfig.TIMESTAMP_ADJUST_MASK;
	}
	
	private static final boolean FORCE_FAST_TS = true;
	private static final int MAX_DTS = 10;
	private static int dts;
	private static long ts;
	
	/**
	 * Returns the current timestamp.
	 */
	public static long timestamp()
	{
		if (FORCE_FAST_TS) return timestamp_fast();
		else
		{
			dts = 0;
			ts = System.nanoTime();
			return ts;
		}
	}
	
	public static long timestamp_fast()
	{
		if (dts++ >= MAX_DTS)
		{
			dts = 0;
			ts = System.nanoTime();
		}
		return ts++;
	}
}
