/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;


import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on the index of an array write event
 * @author gpothier
 */
public class ArrayIndexCondition extends SimpleCondition
{
	private static final long serialVersionUID = -8729400513911498424L;
	
	private int itsIndex;

	public ArrayIndexCondition(int aIndex)
	{
		itsIndex = aIndex;
	}

	@Override
	public BidiIterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.indexIndex.getIndex(itsIndex).getTupleIterator(aTimestamp);
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.matchIndexCondition(itsIndex);
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("Index = %d", itsIndex);
	}

}
