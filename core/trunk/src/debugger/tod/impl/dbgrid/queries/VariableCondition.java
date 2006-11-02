/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;


import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on a variable write event's variable number
 * @author gpothier
 */
public class VariableCondition extends SimpleCondition
{
	private static final long serialVersionUID = -7171025129792888283L;
	private int itsVariableId;

	public VariableCondition(int aVariableId)
	{
		itsVariableId = aVariableId;
	}

	@Override
	public BidiIterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.variableIndex.getIndex(itsVariableId).getTupleIterator(aTimestamp);
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.matchVariableCondition(itsVariableId);
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("VariableId = %d", itsVariableId);
	}
	
}
