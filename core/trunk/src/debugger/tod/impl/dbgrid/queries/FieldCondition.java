/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;


import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on a Field write event's field.
 * @author gpothier
 */
public class FieldCondition extends SimpleCondition
{
	private static final long serialVersionUID = -8850366553462947973L;
	private int itsFieldId;

	public FieldCondition(int aFieldId)
	{
		itsFieldId = aFieldId;
	}

	@Override
	public BidiIterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
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
