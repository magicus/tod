/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

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
package tod.impl.dbgrid.queries;

import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.db.IndexMerger;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * A disjunctive condition: any subcondition must match.
 * @author gpothier
 */
public class Disjunction extends CompoundCondition
{
	private static final long serialVersionUID = -259387225693471171L;

	@Override
	public IBidiIterator<StdTuple> createTupleIterator(
			Indexes aIndexes,
			long aTimestamp)
	{
		IBidiIterator<StdTuple>[] theIterators = new IBidiIterator[getConditions().size()];
		int i = 0;
		for (EventCondition theCondition : getConditions())
		{
			theIterators[i++] = theCondition.createTupleIterator(aIndexes, aTimestamp);
		}
		
		return IndexMerger.disjunction(theIterators);
	}
	
	@Override
	public long[] getEventCounts(Indexes aIndexes, long aT1, long aT2, int aSlotsCount, boolean aForceMergeCounts)
	{
		int theSize = getConditions().size();
		
		// Get sub counts
		long[][] theCounts = new long[theSize][];
		int i = 0;
		for (EventCondition theCondition : getConditions())
		{
			theCounts[i++] = theCondition.getEventCounts(aIndexes, aT1, aT2, aSlotsCount, aForceMergeCounts);
		}
		
		// Sum up
		long[] theResult = new long[aSlotsCount];
		for (i=0;i<theSize;i++)
		{
			for (int j=0;j<aSlotsCount;j++) theResult[j] += theCounts[i][j];
		}
		
		return theResult;
	}

	@Override
	public boolean _match(GridEvent aEvent)
	{
		for (EventCondition theCondition : getConditions())
		{
			if (theCondition._match(aEvent)) return true;
		}
		return false;
	}
	
}
