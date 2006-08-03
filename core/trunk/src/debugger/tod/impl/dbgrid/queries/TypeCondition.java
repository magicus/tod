/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.Tuple;
import tod.impl.dbgrid.messages.EventType;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on event type.
 * @author gpothier
 */
public class TypeCondition extends EventCondition
{
	private EventType itsType;

	public TypeCondition(EventType aType)
	{
		itsType = aType;
	}

	@Override
	protected Iterator<Tuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.typeIndex.getIndex(itsType.ordinal()).getTupleIterator(aTimestamp);
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.getEventType() == itsType;
	}
}
