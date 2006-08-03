/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.Merger;
import tod.impl.dbgrid.dbnode.StdIndexSet.Tuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * A conjunctive condition: all subconditions must match.
 * @author gpothier
 */
public class Conjunction extends CompoundCondition
{

	@Override
	protected Iterator<Tuple> createTupleIterator(
			Indexes aIndexes,
			long aTimestamp)
	{
		Iterator<Tuple>[] theIterators = new Iterator[getConditions().size()];
		int i = 0;
		for (EventCondition theCondition : getConditions())
		{
			theIterators[i++] = theCondition.createTupleIterator(aIndexes, aTimestamp);
		}
		
		return Merger.conjunction(theIterators);
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
