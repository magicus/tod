/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
*/
package tod.impl.dbgrid.dbnode;

import java.util.Iterator;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.impl.dbgrid.BidiIterator;
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
		
		BidiIterator<StdTuple> theIterator = aCondition.createTupleIterator(aIndexes, aT1);
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
