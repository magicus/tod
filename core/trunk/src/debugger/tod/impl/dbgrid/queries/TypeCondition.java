/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on event type.
 * @author gpothier
 */
public class TypeCondition extends SimpleCondition
{
	private static final long serialVersionUID = 5860441411500604107L;
	private MessageType itsType;

	public TypeCondition(MessageType aType)
	{
		itsType = aType;
	}

	@Override
	public Iterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.typeIndex.getIndex(itsType.ordinal()).getTupleIterator(aTimestamp);
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.getEventType() == itsType;
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("Event type = %s", itsType);
	}

}
