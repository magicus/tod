/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import tod.impl.dbgrid.dbnode.EventList;
import tod.impl.dbgrid.dbnode.HierarchicalIndex;
import tod.impl.dbgrid.dbnode.PagedFile;
import tod.impl.dbgrid.dbnode.StdIndexSet;
import tod.impl.dbgrid.messages.GridEvent;

public class FixtureIndexes
{

	public static HierarchicalIndex<StdIndexSet.Tuple> createStdIndex() 
	{
		return createStdIndexes(1)[0];
	}

	public static HierarchicalIndex<StdIndexSet.Tuple>[] createStdIndexes(int aCount) 
	{
		try
		{
			File theFile = new File("indexTest.bin");
			theFile.delete();
			PagedFile thePagedFile = new PagedFile(theFile, DebuggerGridConfig.DB_INDEX_PAGE_SIZE);
			HierarchicalIndex<StdIndexSet.Tuple>[] theIndexes = new HierarchicalIndex[aCount];
			for (int i = 0; i < theIndexes.length; i++)
			{
				theIndexes[i] = new HierarchicalIndex<StdIndexSet.Tuple>(thePagedFile, StdIndexSet.TUPLE_CODEC);
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
			PagedFile thePagedFile = new PagedFile(theFile, DebuggerGridConfig.DB_EVENT_PAGE_SIZE);
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

	/**
	 * Fills an index with values.
	 */
	public static void fillIndex(
			HierarchicalIndex<StdIndexSet.Tuple> aIndex, 
			TestHierarchicalIndex.TimestampGenerator aGenerator,
			long aTupleCount)
	{
		for (long i=0;i<aTupleCount;i++)
		{
			long theTimestamp = aGenerator.next();
			long theData = inventData(theTimestamp);
			
			aIndex.add(new StdIndexSet.Tuple(theTimestamp, theData));
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
	}

	/**
	 * Fills an event list
	 */
	public static void fillEventList(
			EventList aEventList, 
			TestEventList.EventGenerator aGenerator,
			long aCount)
	{
		for (long i=0;i<aCount;i++)
		{
			GridEvent theEvent = aGenerator.next();
			aEventList.add(theEvent);
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
	}
	
}
