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
}
