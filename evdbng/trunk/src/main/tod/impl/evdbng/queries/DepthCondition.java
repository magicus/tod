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
 * Represents a condition on event thread.
 * @author gpothier
 */
public class DepthCondition extends SimpleCondition<SimpleTuple>
{
	private static final long serialVersionUID = 4667937394229993337L;
	private int itsDepth;

	public DepthCondition(int aDepth)
	{
		itsDepth = aDepth;
	}

	@Override
	public IBidiIterator<SimpleTuple> createTupleIterator(Indexes aIndexes, long aEventId)
	{
		return aIndexes.getDepthIndex(itsDepth).getTupleIterator(aEventId);
	}

	@Override
	public boolean _match(GridEventNG aEvent)
	{
		return aEvent.getDepth() == itsDepth;
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("Depth = %d", itsDepth);
	}

}
