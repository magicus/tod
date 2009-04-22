/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid.monitoring;

/**
 * Defines types of aggregation for monitored methods
 * @author gpothier
 */
public enum AggregationType 
{
	/**
	 * Indicates that the values returned by the annotated methods
	 * will be summed for all registered instances.
	 */
	SUM
	{
		@Override
		public Object aggregate(Object[] aValues)
		{
			if (aValues[0] instanceof Long) return longSum(aValues);
			if (aValues[0] instanceof Integer) return longSum(aValues);
			if (aValues[0] instanceof Float) return doubleSum(aValues);
			if (aValues[0] instanceof Double) return doubleSum(aValues);
			
			throw new IllegalArgumentException("Type not supported: "+aValues);
		}
	},

	/**
	 * Indicates that the values returned by the annotated methods
	 * will be averaged for all registered instances.
	 */
	AVG
	{
		@Override
		public Object aggregate(Object[] aValues)
		{
			if (aValues[0] instanceof Long) return longAvg(aValues);
			if (aValues[0] instanceof Integer) return longAvg(aValues);
			if (aValues[0] instanceof Float) return doubleAvg(aValues);
			if (aValues[0] instanceof Double) return doubleAvg(aValues);
			
			throw new IllegalArgumentException("Type not supported: "+aValues);
		}
	},
	
	/**
	 * Indicates that an error should be reported if several instances
	 * are present.
	 */
	NONE
	{
		@Override
		public Object aggregate(Object[] aValues)
		{
			if (aValues.length == 0) throw new RuntimeException("That is weird!!");
			if (aValues.length > 1) throw new UnsupportedOperationException("Aggregate not supported ("+aValues.length+" values)");
			return aValues[0];
		}
	};
	
	/**
	 * Computes the aggregate of the specified set of values.
	 * @param aValues An array of values which must all be of the same (or compatible) types
	 */
	public abstract Object aggregate(Object[] aValues);
	
	private static long longSum(Object[] aValues)
	{
		long theSum = 0;
		for (Object theValue : aValues) 
		{
			Number theNumber = (Number) theValue;
			theSum += theNumber.longValue();
		}
		return theSum;
	}
	
	private static long longAvg(Object[] aValues)
	{
		return longSum(aValues) / aValues.length;
	}

	private static double doubleSum(Object[] aValues)
	{
		double theSum = 0;
		for (Object theValue : aValues) 
		{
			Number theNumber = (Number) theValue;
			theSum += theNumber.doubleValue();
		}
		return theSum;
	}
	
	private static double doubleAvg(Object[] aValues)
	{
		return doubleSum(aValues) / aValues.length;
	}
}
