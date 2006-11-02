/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.IndexMerger;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * A conjunctive condition: all subconditions must match.
 * @author gpothier
 */
public class Conjunction extends CompoundCondition
{

	private static final long serialVersionUID = 6155046517220795498L;

	@Override
	public BidiIterator<StdTuple> createTupleIterator(
			Indexes aIndexes,
			long aTimestamp)
	{
		BidiIterator<StdTuple>[] theIterators = new BidiIterator[getConditions().size()];
		int i = 0;
		for (EventCondition theCondition : getConditions())
		{
			theIterators[i++] = theCondition.createTupleIterator(aIndexes, aTimestamp);
		}
		
		return IndexMerger.conjunction(theIterators);
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		for (EventCondition theCondition : getConditions())
		{
			if (! theCondition.match(aEvent)) return false;
		}
		return true;
	}
	
}
