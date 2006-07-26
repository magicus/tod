/*
 * Created on Jul 25, 2006
 */
package tod.impl.dbgrid;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import static org.junit.Assert.*;
import tod.impl.dbgrid.dbnode.HierarchicalIndex;
import tod.impl.dbgrid.dbnode.StdIndexSet;
import tod.impl.dbgrid.dbnode.StdIndexSet.Tuple;

public class TestHierarchicalIndex
{
	@Test 
	public void testIndex() throws FileNotFoundException
	{
		fillCheck(10000000);
	}
	
	private void fillCheck(long aTupleCount)
	{
		HierarchicalIndex<Tuple> theIndex = FixtureIndexes.createStdIndex();
		FixtureIndexes.fillIndex(theIndex, new TimestampGenerator(0), aTupleCount);
		checkIndex(theIndex, new TimestampGenerator(0), aTupleCount);
	}
	
	@Test 
	public void testSeek()
	{
		HierarchicalIndex<Tuple> theIndex = FixtureIndexes.createStdIndex();
		FixtureIndexes.fillIndex(theIndex, new TimestampGenerator(0), 1000000);
		
		seekAndCheck(theIndex, new TimestampGenerator(0), 10000, 100);
		seekAndCheck(theIndex, new TimestampGenerator(0), 10000, 1000);
		seekAndCheck(theIndex, new TimestampGenerator(0), 10000, 10000);
		seekAndCheck(theIndex, new TimestampGenerator(0), 10000, 100000);
		seekAndCheck(theIndex, new TimestampGenerator(0), 10000, 500000);
		seekAndCheck(theIndex, new TimestampGenerator(0), 10000, 900000);
	}
	
	/**
	 * Checks the values of an index filled with {@link FixtureIndexes#fillIndex(HierarchicalIndex, tod.impl.dbgrid.TestHierarchicalIndex.TimestampGenerator, long)}
	 */
	private void checkIndex(
			HierarchicalIndex<StdIndexSet.Tuple> aIndex, 
			TimestampGenerator aGenerator,
			long aTupleCount)
	{
		checkTuples(aIndex.getTupleIterator(0), aGenerator, aTupleCount, true);
	}
	
	private void seekAndCheck(
			HierarchicalIndex<StdIndexSet.Tuple> aIndex, 
			TimestampGenerator aGenerator,
			long aTupleCount,
			long aSkippedTuples)
	{
		long theTimestamp = 0;
		for (long i=0;i<aSkippedTuples-1;i++) theTimestamp = aGenerator.next();
		
		// Check when timestamp is equal
		Iterator<Tuple> theIterator = aIndex.getTupleIterator(theTimestamp);
		Tuple theTuple = theIterator.next();
		checkTuple(theTuple, theTimestamp);
		
		// Check when timestamp is a bit more
		theIterator = aIndex.getTupleIterator(theTimestamp+5);
		theTuple = theIterator.next();
		checkTuple(theTuple, theTimestamp);
		
		checkTuples(theIterator, aGenerator, aTupleCount, false);
	}
	
	/**
	 * Checks that the tuple values obtained from an iterator match thoses of the given 
	 * generator.
	 * @param aExhaust If true, this method ensures that the tuple stream
	 * finishes when the specified tuple count is read.
	 * 
	 */
	private void checkTuples(
			Iterator<Tuple> aIterator, 
			TimestampGenerator aGenerator,
			long aTupleCount,
			boolean aExhaust)
	{
		for (long i=0;i<aTupleCount;i++)
		{
			long theTimestamp = aGenerator.next();
			
			if (! aIterator.hasNext()) fail("No more tuples");
			Tuple theTuple = aIterator.next();
			checkTuple(theTuple, theTimestamp);
			
			if (i % 1000000 == 0) System.out.println("v: "+i);
		}
		
		if (aExhaust && aIterator.hasNext()) fail("Too many tuple");
	}
	
	private void checkTuple(Tuple aTuple, long aTimestamp)
	{
		long theData = FixtureIndexes.inventData(aTimestamp);
		
		if (aTuple.getTimestamp() != aTimestamp) fail("Timestamp mismatch");
		if (aTuple.getEventPointer() != theData) fail("Data mismatch");
	}
	
	/**
	 * Generates a sequence of random, increasing timestamp values.
	 * The interval between successive values is at least 10. 
	 * @author gpothier
	 */
	public static class TimestampGenerator
	{
		private Random itsRandom;
		private long itsTimestamp;

		public TimestampGenerator(long aSeed)
		{
			itsRandom = new Random(aSeed);
			itsTimestamp = itsRandom.nextLong() >>> 8;
		}
		
		public long next()
		{
			itsTimestamp += itsRandom.nextInt(100000) + 10;
			return itsTimestamp;
		}
		
	}
	
	public static void main(String[] args)
	{
		JUnitCore.runClasses(TestHierarchicalIndex.class);
	}
}
