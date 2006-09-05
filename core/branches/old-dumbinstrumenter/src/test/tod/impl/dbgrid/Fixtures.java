/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid;


import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import tod.impl.dbgrid.dbnode.CFlowMap;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.dbnode.EventList;
import tod.impl.dbgrid.dbnode.HierarchicalIndex;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import tod.impl.dbgrid.dbnode.StdIndexSet;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;
import tod.impl.dbgrid.test.TestHierarchicalIndex;
import tod.impl.dbgrid.test.TestHierarchicalIndex.TimestampGenerator;
import zz.utils.ListMap;
import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;


public class Fixtures
{

	public static class FakeThread
	{
		private static int itsExpectedLength = 10;
		
		private int itsHostId;
		private int itsThreadId;
		
		private TimestampGenerator itsGenerator;
		private Random itsRandom;
		private LinkedList<byte[]> itsStack = new LinkedList<byte[]>();
		
		public FakeThread(int aHostId, int aThreadId)
		{
			itsHostId = aHostId;
			itsThreadId = aThreadId;
			
			int theSeed = itsHostId*10000 + itsThreadId;
			itsGenerator = new TimestampGenerator(theSeed);
			itsRandom = new Random(theSeed);
			
			itsStack.addLast(genPointer());
		}
		
		private byte[] genPointer()
		{
			return ExternalPointer.create(1, itsHostId, itsThreadId, itsGenerator.next());
		}
		
		public boolean addNextToMap(CFlowMap aMap, ListMap<byte[], byte[]> aMemMap)
		{
			byte[] theParent = itsStack.getLast();
			byte[] theChild = genPointer();
			
			aMap.add(theParent, theChild);
			if (aMemMap != null) aMemMap.add(theParent, theChild);
			
			
			// Check if we recurse
			if (itsRandom.nextFloat() < 0.5f)
			{
				itsStack.addLast(theChild);
			}
			
			// Check if we exit
			if (itsRandom.nextFloat() < 1f / itsExpectedLength)
			{
				itsStack.removeLast();
			}
			
			return ! itsStack.isEmpty();
		}
	}

	public static HierarchicalIndex<StdIndexSet.StdTuple> createStdIndex() 
	{
		return createStdIndexes(1)[0];
	}

	public static HierarchicalIndex<StdIndexSet.StdTuple>[] createStdIndexes(int aCount) 
	{
		try
		{
			File theFile = new File("stdIndexTest.bin");
			theFile.delete();
			HardPagedFile thePagedFile = new HardPagedFile(theFile, DebuggerGridConfig.DB_INDEX_PAGE_SIZE);
			HierarchicalIndex<StdIndexSet.StdTuple>[] theIndexes = new HierarchicalIndex[aCount];
			for (int i = 0; i < theIndexes.length; i++)
			{
				theIndexes[i] = new HierarchicalIndex<StdIndexSet.StdTuple>(
						""+i, 
						thePagedFile, 
						StdIndexSet.TUPLE_CODEC);
			}
			
			return theIndexes;
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static HierarchicalIndex<RoleIndexSet.RoleTuple> createRoleIndex() 
	{
		return createRoleIndexes(1)[0];
	}
	
	public static HierarchicalIndex<RoleIndexSet.RoleTuple>[] createRoleIndexes(int aCount) 
	{
		try
		{
			File theFile = new File("roleIndexTest.bin");
			theFile.delete();
			HardPagedFile thePagedFile = new HardPagedFile(theFile, DebuggerGridConfig.DB_INDEX_PAGE_SIZE);
			HierarchicalIndex<RoleIndexSet.RoleTuple>[] theIndexes = new HierarchicalIndex[aCount];
			for (int i = 0; i < theIndexes.length; i++)
			{
				theIndexes[i] = new HierarchicalIndex<RoleIndexSet.RoleTuple>(
						""+i, 
						thePagedFile, 
						RoleIndexSet.TUPLE_CODEC);
			}
			
			return theIndexes;
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static EventList createEventList() 
	{
		try
		{
			File theFile = new File("eventTest.bin");
			theFile.delete();
			HardPagedFile thePagedFile = new HardPagedFile(theFile, DebuggerGridConfig.DB_EVENT_PAGE_SIZE);
			return new EventList(thePagedFile);
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	
	public static long inventData(long aTimestamp)
	{
		return aTimestamp*7;
	}

	public static byte inventRole(long aTimestamp)
	{
		return (byte) aTimestamp;
	}
	
	/**
	 * Fills an std index with values.
	 */
	public static void fillStdIndex(
			HierarchicalIndex<StdIndexSet.StdTuple> aIndex, 
			TestHierarchicalIndex.TimestampGenerator aGenerator,
			long aTupleCount)
	{
		for (long i=0;i<aTupleCount;i++)
		{
			long theTimestamp = aGenerator.next();
			aIndex.add(new StdIndexSet.StdTuple(theTimestamp, inventData(theTimestamp)));
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
	}

	/**
	 * Fills an std index with values.
	 */
	public static void fillRoleIndex(
			HierarchicalIndex<RoleIndexSet.RoleTuple> aIndex, 
			TestHierarchicalIndex.TimestampGenerator aGenerator,
			long aTupleCount)
	{
		for (long i=0;i<aTupleCount;i++)
		{
			long theTimestamp = aGenerator.next();
			aIndex.add(new RoleIndexSet.RoleTuple(
					theTimestamp, 
					inventData(theTimestamp), 
					inventRole(theTimestamp)));
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
	}
	
	/**
	 * Fills an event list
	 */
	public static void fillEventList(
			EventList aEventList, 
			EventGenerator aGenerator,
			long aCount)
	{
		for (long i=0;i<aCount;i++)
		{
			GridEvent theEvent = aGenerator.next();
			aEventList.add(theEvent);
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
	}
	
	/**
	 * Fills an event list and returns the ids of the events
	 */
	public static long[] fillEventListReport(
			EventList aEventList, 
			EventGenerator aGenerator,
			int aCount)
	{
		long[] theIds = new long[aCount];
		for (int i=0;i<aCount;i++)
		{
			GridEvent theEvent = aGenerator.next();
			theIds[i] = aEventList.add(theEvent);
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
		
		return theIds;
	}
	
	/**
	 * Fills a database node with events generated by the specified generator.
	 */
	public static void fillNode(DatabaseNode aNode, EventGenerator aGenerator, long aCount)
	{
		for(long i=0;i<aCount;i++) 
		{
			aNode.push(aGenerator.next());
			if (i % 1000000 == 0) System.out.println(i);
		}
	}

	/**
	 * Checks that two events are equal.
	 */
	public static void assertEquals(GridEvent aRefEvent, GridEvent aEvent)
	{
		BitStruct theRefStruct = new IntBitStruct(1000);
		BitStruct theStruct = new IntBitStruct(1000);
		
		aRefEvent.writeTo(theRefStruct);
		aEvent.writeTo(theStruct);
		
		if (theRefStruct.getPos() != theStruct.getPos())
		{
			System.out.println("ref:  "+aRefEvent);
			System.out.println("test: "+aEvent);
			fail("Size mismatch");
		}
		
		int theSize = (theRefStruct.getPos()+31)/32;
		
		theRefStruct.setPos(0);
		theStruct.setPos(0);
		
		for (int i=0;i<theSize;i++)
		{
			if (theRefStruct.readInt(32) != theStruct.readInt(32))
			{
				System.out.println("ref:  "+aRefEvent);
				System.out.println("test: "+aEvent);
				fail("Data mismatch");				
			}
		}
	}
	
	public static void checkCondition(
			DatabaseNode aNode, 
			EventCondition aCondition, 
			EventGenerator aReferenceGenerator,
			int aSkip,
			int aCount)
	{
		GridEvent theEvent = null;
		for (int i=0;i<aSkip;i++)
		{
			theEvent = aReferenceGenerator.next();
		}
		
		int theMatched = 0;
		long theTimestamp = theEvent != null ? theEvent.getTimestamp()+1 : 0;
		Iterator<GridEvent> theIterator = aNode.evaluate(aCondition, theTimestamp);
		for (int i=0;i<aCount;i++)
		{
			GridEvent theRefEvent = aReferenceGenerator.next();
			if (aCondition.match(theRefEvent))
			{
				GridEvent theTestedEvent = theIterator.next(); 
				Fixtures.assertEquals(theRefEvent, theTestedEvent);
				theMatched++;
//				System.out.println(i+"m");
			}
//			else System.out.println(i);
		}
		
		System.out.println("Matched: "+theMatched);
	}

}
