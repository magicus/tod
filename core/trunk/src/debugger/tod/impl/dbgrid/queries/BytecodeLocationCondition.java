/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.Tuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on bytecode location
 * @author gpothier
 */
public class BytecodeLocationCondition extends SimpleCondition
{
	private int itsBytecodeLocation;

	public BytecodeLocationCondition(int aBytecodeLocation)
	{
		itsBytecodeLocation = aBytecodeLocation;
	}

	@Override
	protected Iterator<Tuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.bytecodeLocationIndex.getIndex(itsBytecodeLocation).getTupleIterator(aTimestamp);
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.getOperationBytecodeIndex() == itsBytecodeLocation;
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("Bytecode index = %d", itsBytecodeLocation);
	}

}
