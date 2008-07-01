/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.queries;


import tod.impl.database.IBidiIterator;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.file.SimpleTuple;
import tod.impl.evdbng.messages.GridEventNG;

/**
 * Represents a condition on a variable write event's variable number
 * @author gpothier
 */
public class VariableCondition extends SimpleCondition<SimpleTuple>
{
	private static final long serialVersionUID = -7171025129792888283L;
	private int itsVariableId;

	public VariableCondition(int aVariableId)
	{
		itsVariableId = aVariableId;
	}

	@Override
	public IBidiIterator<SimpleTuple> createTupleIterator(Indexes aIndexes, long aEventId)
	{
		return aIndexes.getVariableIndex(itsVariableId).getTupleIterator(aEventId);
	}

	@Override
	public boolean _match(GridEventNG aEvent)
	{
		return aEvent.matchVariableCondition(itsVariableId);
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("VariableId = %d", itsVariableId);
	}
	
}