/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".

Contact: gpothier -at- dcc . uchile . cl
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
	 * Indicates that the maximum value of the values returned by the 
	 * annotated methods will be returned
	 */
	MAX
	{
		@Override
		public Object aggregate(Object[] aValues)
		{
			if (aValues[0] instanceof Long) return longMax(aValues);
			if (aValues[0] instanceof Integer) return longMax(aValues);
			if (aValues[0] instanceof Float) return doubleMax(aValues);
			if (aValues[0] instanceof Double) return doubleMax(aValues);
			
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
	
	private static long longMax(Object[] aValues)
	{
		long theMax = 0;
		for (Object theValue : aValues) 
		{
			Number theNumber = (Number) theValue;
			theMax = Math.max(theMax, theNumber.longValue());
		}
		return theMax;
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
	
	private static double doubleMax(Object[] aValues)
	{
		double theMax = 0;
		for (Object theValue : aValues) 
		{
			Number theNumber = (Number) theValue;
			theMax = Math.max(theMax, theNumber.doubleValue());
		}
		return theMax;
	}
	
	private static double doubleAvg(Object[] aValues)
	{
		return doubleSum(aValues) / aValues.length;
	}
}
