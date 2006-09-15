/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on event thread.
 * @author gpothier
 */
public class DepthCondition extends SimpleCondition
{
	private int itsDepth;

	public DepthCondition(int aDepth)
	{
		itsDepth = aDepth;
	}

	@Override
	public Iterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.depthIndex.getIndex(itsDepth).getTupleIterator(aTimestamp);
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.getDepth() == itsDepth;
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("Depth = %d", itsDepth);
	}

}
