/*
 * Created on Jul 25, 2006
 */
package tod.impl.dbgrid;

import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import tod.impl.dbgrid.dbnode.HierarchicalIndex;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import tod.impl.dbgrid.dbnode.StdIndexSet;
import tod.impl.dbgrid.dbnode.RoleIndexSet.RoleTuple;

public class TestHierarchicalIndex
{
	@Test 
	public void testIndex()
	{
		fillCheck(new StdIndexTester(), 10000000);
		fillCheck(new RoleIndexTester(), 10000000);
	}
	
	private <T extends StdIndexSet.StdTuple> void fillCheck(IndexTester<T> aTester, long aTupleCount)
	{
		HierarchicalIndex<T> theIndex = aTester.createIndex();
		aTester.fillIndex(theIndex, new TimestampGenerator(0), aTupleCount);
		aTester.checkIndex(theIndex, new TimestampGenerator(0), aTupleCount);
	}
	
	@Test 
	public void testSeek()
	{
		testSeek(new StdIndexTester());
		testSeek(new RoleIndexTester());
	}
	
	private <T extends StdIndexSet.StdTuple> void testSeek(IndexTester<T> aTester)
	{
		HierarchicalIndex<T> theIndex = aTester.createIndex();
		aTester.fillIndex(theIndex, new TimestampGenerator(0), 1000000);
		
		aTester.seekAndCheck(theIndex, new TimestampGenerator(0), 10000, 100);
		aTester.seekAndCheck(theIndex, new TimestampGenerator(0), 10000, 1000);
		aTester.seekAndCheck(theIndex, new TimestampGenerator(0), 10000, 10000);
		aTester.seekAndCheck(theIndex, new TimestampGenerator(0), 10000, 100000);
		aTester.seekAndCheck(theIndex, new TimestampGenerator(0), 10000, 500000);
		aTester.seekAndCheck(theIndex, new TimestampGenerator(0), 10000, 900000);
	}
	
	private abstract static class IndexTester<T extends StdIndexSet.StdTuple>
	{
		public abstract HierarchicalIndex<T> createIndex();
		
		public abstract void fillIndex(
				HierarchicalIndex<T> aIndex,
				TimestampGenerator aGenerator,
				long aTupleCount);
		
		/**
		 * Checks the values of an index filled with {@link Fixtures#fillIndex(HierarchicalIndex, tod.impl.dbgrid.TestHierarchicalIndex.TimestampGenerator, long)}
		 */
		public void checkIndex(
				HierarchicalIndex<T> aIndex, 
				TimestampGenerator aGenerator,
				long aTupleCount)
		{
			checkTuples(aIndex.getTupleIterator(0), aGenerator, aTupleCount, true);
		}
		
		public void seekAndCheck(
				HierarchicalIndex<T> aIndex, 
				TimestampGenerator aGenerator,
				long aTupleCount,
				long aSkippedTuples)
		{
			long theTimestamp = 0;
			for (long i=0;i<aSkippedTuples-1;i++) theTimestamp = aGenerator.next();
			
			// Check when timestamp is equal
			Iterator<T> theIterator = aIndex.getTupleIterator(theTimestamp);
			T theTuple = theIterator.next();
			checkTuple(theTuple, theTimestamp);
			
			// Check when timestamp is a bit more
			theIterator = aIndex.getTupleIterator(theTimestamp+5);
			theTimestamp = aGenerator.next();
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
				Iterator<T> aIterator, 
				TimestampGenerator aGenerator,
				long aTupleCount,
				boolean aExhaust)
		{
			for (long i=0;i<aTupleCount;i++)
			{
				long theTimestamp = aGenerator.next();
				
				if (! aIterator.hasNext()) fail("No more tuples");
				T theTuple = aIterator.next();
				checkTuple(theTuple, theTimestamp);
				
				if (i % 1000000 == 0) System.out.println("v: "+i);
			}
			
			if (aExhaust && aIterator.hasNext()) fail("Too many tuple");
		}
		
		protected void checkTuple(T aTuple, long aTimestamp)
		{
			long theData = Fixtures.inventData(aTimestamp);
			
			if (aTuple.getTimestamp() != aTimestamp) fail("Timestamp mismatch");
			if (aTuple.getEventPointer() != theData) fail("Data mismatch");
		}
		
	}
	
	private static class StdIndexTester extends IndexTester<StdIndexSet.StdTuple>
	{
		@Override
		public HierarchicalIndex<tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple> createIndex()
		{
			return Fixtures.createStdIndex();
		}

		@Override
		public void fillIndex(HierarchicalIndex<tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple> aIndex, TimestampGenerator aGenerator, long aTupleCount)
		{
			Fixtures.fillStdIndex(aIndex, aGenerator, aTupleCount);
		}
	}
	
	private static class RoleIndexTester extends IndexTester<RoleIndexSet.RoleTuple>
	{
		@Override
		public HierarchicalIndex<RoleTuple> createIndex()
		{
			return Fixtures.createRoleIndex();
		}

		@Override
		public void fillIndex(HierarchicalIndex<RoleTuple> aIndex, TimestampGenerator aGenerator, long aTupleCount)
		{
			Fixtures.fillRoleIndex(aIndex, aGenerator, aTupleCount);
		}

		@Override
		protected void checkTuple(RoleTuple aTuple, long aTimestamp)
		{
			super.checkTuple(aTuple, aTimestamp);
			if (aTuple.getRole() != Fixtures.inventRole(aTimestamp)) fail("Role mismatch");
		}
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
	
	public static void main(String[] args)
	{
		JUnitCore.runClasses(TestHierarchicalIndex.class);
	}
}
