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
package tod.impl.evdb1.queries;


import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.evdb1.db.EventList;
import tod.impl.evdb1.db.Indexes;
import tod.impl.evdb1.db.StdIndexSet.StdTuple;

/**
 * Represents a condition on the index of an array write event
 * @author gpothier
 */
public class ArrayIndexCondition extends SimpleCondition
{
	private static final long serialVersionUID = -8729400513911498424L;
	
	private int itsPart;
	private int itsIndex;

	public ArrayIndexCondition(int aPart, int aIndex)
	{
		itsPart = aPart;
		itsIndex = aIndex;
	}

	@Override
	public IBidiIterator<StdTuple> createTupleIterator(EventList aEventList, Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.getArrayIndexIndex(itsPart, itsIndex).getTupleIterator(aTimestamp);
	}

	public boolean _match(GridEvent aEvent)
	{
		return aEvent.matchIndexCondition(itsPart, itsIndex);
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("Index = %d", itsIndex);
	}

}