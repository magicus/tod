/*
 * Created on Oct 17, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.util.Iterator;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.dbnode.file.IndexTuple;
import tod.impl.dbgrid.dbnode.file.TupleIterator;
import tod.impl.dbgrid.dbnode.file.PageBank.Page;
import tod.impl.dbgrid.queries.EventCondition;
import zz.utils.ArrayStack;
import zz.utils.Stack;

/**
 * A helper class that computes event counts.
 * @see IEventBrowser#getEventCounts(long, long, int)
 * @author gpothier
 */
public class EventsCounter
{
	/**
	 * If this flag is set to true, counting queries are forced to use the merge
	 * operations.
	 */
	public static boolean FORCE_MERGE_COUNTS = false;
	
	public static long[] mergeCountEvents(
			EventCondition aCondition, 
			Indexes aIndexes, 
			long aT1, 
			long aT2, 
			int aSlotsCount) 
	{
//		System.out.println("Computing counts...");
//		long t0 = System.currentTimeMillis();
		long[] theCounts = new long[aSlotsCount];
		
		long theTotal = 0;
		
		Iterator<StdTuple> theIterator = aCondition.createTupleIterator(aIndexes, aT1);
		while (theIterator.hasNext())
		{
			StdTuple theTuple = theIterator.next();
			long theTimestamp = theTuple.getTimestamp();
			if (theTimestamp < aT1) continue;
			if (theTimestamp >= aT2) break;

			int theSlot = (int)(((theTimestamp - aT1) * aSlotsCount) / (aT2 - aT1));
			theCounts[theSlot]++;
			theTotal++;
		}
		
//		long t1 = System.currentTimeMillis();
//		System.out.println("Counts computed in "+(t1-t0)+"ms (found "+theTotal+" events)");
		return theCounts;
	}
}
