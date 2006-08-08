/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.Tuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on a Field write event's field.
 * @author gpothier
 */
public class FieldCondition extends SimpleCondition
{
	private int itsFieldId;

	public FieldCondition(int aFieldId)
	{
		itsFieldId = aFieldId;
	}

	@Override
	protected Iterator<Tuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.fieldIndex.getIndex(itsFieldId).getTupleIterator(aTimestamp);
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.matchFieldCondition(itsFieldId);
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("FieldId = %d", itsFieldId);
	}

}
