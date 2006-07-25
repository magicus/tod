/*
 * Created on Jul 25, 2006
 */
package tod.impl.dbgrid;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Random;

import junit.framework.TestCase;
import tod.impl.dbgrid.dbnode.HierarchicalIndex;
import tod.impl.dbgrid.dbnode.PagedFile;
import tod.impl.dbgrid.dbnode.StdIndexSet;
import tod.impl.dbgrid.dbnode.StdIndexSet.Tuple;

public class HierarchicalIndexTest extends TestCase
{
	public void testIndex() throws FileNotFoundException
	{
//		fillCheck(10000000);
	}
	
	private void fillCheck(long aTupleCount) throws FileNotFoundException
	{
		HierarchicalIndex<Tuple> theIndex = createStdIndex();
		fillIndex(theIndex, new TimestampGenerator(0), aTupleCount);
		checkIndex(theIndex, new TimestampGenerator(0), aTupleCount);
	}
	
	public void testSeek() throws FileNotFoundException
	{
		HierarchicalIndex<Tuple> theIndex = createStdIndex();
		fillIndex(theIndex, new TimestampGenerator(0), 100000);
		
		seekAndCheck(theIndex, new TimestampGenerator(0), 1000, 100);
		seekAndCheck(theIndex, new TimestampGenerator(0), 1000, 1000);
		seekAndCheck(theIndex, new TimestampGenerator(0), 1000, 10000);
	}
	
	private HierarchicalIndex<StdIndexSet.Tuple> createStdIndex() throws FileNotFoundException
	{
		File theFile = new File("indexTest.bin");
		theFile.delete();
		PagedFile thePagedFile = new PagedFile(theFile, DebuggerGridConfig.DB_INDEX_PAGE_SIZE);
		return new HierarchicalIndex<StdIndexSet.Tuple>(thePagedFile, StdIndexSet.TUPLE_CODEC);
	}
	
	private long inventData(long aTimestamp)
	{
		return (long) (Math.sin(aTimestamp) * Long.MAX_VALUE);
	}
	
	/**
	 * Fills an index with values.
	 */
	private void fillIndex(
			HierarchicalIndex<StdIndexSet.Tuple> aIndex, 
			TimestampGenerator aGenerator,
			long aTupleCount)
	{
		for (long i=0;i<aTupleCount;i++)
		{
			long theTimestamp = aGenerator.next();
			long theData = inventData(theTimestamp);
			
			aIndex.add(new StdIndexSet.Tuple(theTimestamp, theData));
			
			if (i % 100000 == 0) System.out.println("w: "+i);
		}
	}

	/**
	 * Checks the values of an index filled with {@link #fillIndex(HierarchicalIndex, tod.impl.dbgrid.HierarchicalIndexTest.TimestampGenerator, long)}
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
			long theData = inventData(theTimestamp);
			
			if (! aIterator.hasNext()) fail("No more tuples");
			Tuple theTuple = aIterator.next();
			checkTuple(theTuple, theTimestamp);
			
			if (i % 100000 == 0) System.out.println("v: "+i);
		}
		
		if (aExhaust && aIterator.hasNext()) fail("Too many tuple");
	}
	
	private void checkTuple(Tuple aTuple, long aTimestamp)
	{
		long theData = inventData(aTimestamp);
		
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
}
