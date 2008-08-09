/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.queries;


import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.evdbng.db.EventList;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.file.SimpleTuple;

/**
 * Represents a condition on the index of an array write event
 * @author gpothier
 */
public class ArrayIndexCondition extends SimpleCondition<SimpleTuple>
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
	public IBidiIterator<SimpleTuple> createTupleIterator(EventList aEventList, Indexes aIndexes, long aEventId)
	{
		return aIndexes.getArrayIndexIndex(itsPart, itsIndex).getTupleIterator(aEventId);
	}

	@Override
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
