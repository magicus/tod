package tod.core.database;

import java.util.Random;

/**
	 * Generates a sequence of random, increasing timestamp values.
	 * The interval between successive values is at least 10. 
	 * @author gpothier
	 */
	public class TimestampGenerator
	{
		private Random itsRandom;
		private long itsTimestamp;

		public TimestampGenerator(long aSeed)
		{
			itsRandom = new Random(aSeed);
//			itsTimestamp = itsRandom.nextLong() >>> 8;
			itsTimestamp = 0;
		}
		
		public long next()
		{
//			itsTimestamp += itsRandom.nextInt(100000) + 10;
			itsTimestamp += 10;
			return itsTimestamp;
		}
		
	}