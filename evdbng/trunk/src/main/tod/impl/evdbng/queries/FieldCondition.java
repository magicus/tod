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
 * Represents a condition on a Field write event's field.
 * @author gpothier
 */
public class FieldCondition extends SimpleCondition<SimpleTuple>
{
	private static final long serialVersionUID = -8850366553462947973L;
	private int itsFieldId;

	public FieldCondition(int aFieldId)
	{
		itsFieldId = aFieldId;
	}

	@Override
	public IBidiIterator<SimpleTuple> createTupleIterator(EventList aEventList, Indexes aIndexes, long aEventId)
	{
		return aIndexes.getFieldIndex(itsFieldId).getTupleIterator(aEventId);
	}

	@Override
	public boolean _match(GridEvent aEvent)
	{
		return aEvent.matchFieldCondition(itsFieldId);
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("FieldId = %d", itsFieldId);
	}

}
