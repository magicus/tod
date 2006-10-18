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
				| (aSerial & AgentConfig.TIMESTAMP_ADJUST_MASK);
	}
	
	public static long untransformTimestamp(long aTimestamp)
	{
		return aTimestamp >>> AgentConfig.TIMESTAMP_ADJUST_SHIFT;
	}
	
	private static final boolean FORCE_FAST_TS = true;
	private static final boolean FORCE_FALSE_TS = false;
	private static final int MAX_DTS = 10;
	private static int dts = MAX_DTS;
	private static long ts;
	
	/**
	 * Returns the current timestamp.
	 */
	public static long timestamp()
	{
		if (FORCE_FALSE_TS) return timestamp_false();
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
		if (FORCE_FALSE_TS) return timestamp_false();
		if (dts++ >= MAX_DTS)
		{
			dts = 0;
			ts = System.nanoTime();
		}
		
		return ts++;
	}
	
	private static long timestamp_false()
	{
		ts += 1 << AgentConfig.TIMESTAMP_ADJUST_SHIFT;
		
//		if (ts == 62572800 || ts == 62583552)
//		{
//			System.out.println("AgentUtils.timestamp_real()");
//		}
		
		return ts;
	}
	
	public static String formatTimestampU(long aTimestamp)
	{
		return formatTimestamp(untransformTimestamp(aTimestamp));
	}
	
	public static String formatTimestamp(long aTimestamp)
	{
//		aTimestamp >>>= AgentConfig.TIMESTAMP_ADJUST_SHIFT;
		
		long theMicros = aTimestamp/1000;
		aTimestamp -= theMicros*1000;
		
		long theMillis = theMicros/1000;
		theMicros -= theMillis*1000;
		
		long theSeconds = theMillis/1000;
		theMillis -= theSeconds*1000;
		
		long theMinutes = theSeconds/60;
		theSeconds -= theMinutes*60;
		
		long theHours = theMinutes/60;
		theMinutes -= theHours*60;
		
		long theDays = theHours/24;
		theHours -= theDays*24;
		
		long theYears = theDays/365;
		theDays -= theYears*365;
		
		boolean theStarted = false;
		StringBuilder theBuilder = new StringBuilder();
		if (theYears > 0) 
		{
			theBuilder.append(theYears+"y ");
			theStarted = true;
		}
		
		if (theStarted || theDays > 0)
		{
			theBuilder.append(String.format("%03dd ", theDays));
			theStarted = true;
		}
		
		if (theStarted || theHours > 0)
		{
			theBuilder.append(String.format("%02dh ", theHours));
			theStarted = true;
		}
		
		if (theStarted || theMinutes > 0)
		{
			theBuilder.append(String.format("%02dm ", theMinutes));
			theStarted = true;
		}
		
		if (theStarted || theSeconds > 0)
		{
			theBuilder.append(String.format("%02ds ", theSeconds));
			theStarted = true;
		}
		
		theBuilder.append(String.format("%03d.%03d.%03d", theMillis, theMicros, aTimestamp));
		
		return theBuilder.toString();
	}
}
