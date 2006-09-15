/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.IndexMerger;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * A disjunctive condition: any subcondition must match.
 * @author gpothier
 */
public class Disjunction extends CompoundCondition
{

	@Override
	public Iterator<StdTuple> createTupleIterator(
			Indexes aIndexes,
			long aTimestamp)
	{
		Iterator<StdTuple>[] theIterators = new Iterator[getConditions().size()];
		int i = 0;
		for (EventCondition theCondition : getConditions())
		{
			theIterators[i++] = theCondition.createTupleIterator(aIndexes, aTimestamp);
		}
		
		return IndexMerger.disjunction(theIterators);
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		for (EventCondition theCondition : getConditions())
		{
			if (theCondition.match(aEvent)) return true;
		}
		return false;
	}
	
}
