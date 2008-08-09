/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import tod.core.database.browser.IEventBrowser;
import tod.impl.database.IBidiIterator;
import tod.impl.evdbng.db.file.SimpleTuple;
import tod.impl.evdbng.queries.EventCondition;

/**
 * A helper class that computes event counts.
 * @see IEventBrowser#getEventCounts(long, long, int)
 * @author gpothier
 */
public class EventsCounter
{
	public static long[] mergeCountEvents(
			EventCondition aCondition, 
			EventList aEventList,
			Indexes aIndexes, 
			long aT1, 
			long aT2, 
			int aSlotsCount) 
	{
//		System.out.println("Computing counts...");
//		long t0 = System.currentTimeMillis();
		long[] theCounts = new long[aSlotsCount];
		
		long theTotal = 0;
		
		IBidiIterator<SimpleTuple> theIterator = aCondition.createTupleIterator(aEventList, aIndexes, aT1);
		while (theIterator.hasNext())
		{
			SimpleTuple theTuple = theIterator.next();
			long theTimestamp = theTuple.getKey();
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
