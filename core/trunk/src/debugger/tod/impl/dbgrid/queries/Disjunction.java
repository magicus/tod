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
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.IndexMerger;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * A disjunctive condition: any subcondition must match.
 * @author gpothier
 */
public class Disjunction extends CompoundCondition
{

	private static final long serialVersionUID = -259387225693471171L;

	@Override
	public BidiIterator<StdTuple> createTupleIterator(
			Indexes aIndexes,
			long aTimestamp)
	{
		BidiIterator<StdTuple>[] theIterators = new BidiIterator[getConditions().size()];
		int i = 0;
		for (EventCondition theCondition : getConditions())
		{
			theIterators[i++] = theCondition.createTupleIterator(aIndexes, aTimestamp);
		}
		
		return IndexMerger.disjunction(theIterators);
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		for (EventCondition theCondition : getConditions())
		{
			if (theCondition.match(aEvent)) return true;
		}
		return false;
	}
	
}
