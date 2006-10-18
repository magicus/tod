/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on bytecode location
 * @author gpothier
 */
public class BytecodeLocationCondition extends SimpleCondition
{
	private static final long serialVersionUID = 7843402757580345452L;
	private int itsBytecodeLocation;

	public BytecodeLocationCondition(int aBytecodeLocation)
	{
		itsBytecodeLocation = aBytecodeLocation;
	}

	@Override
	public Iterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
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
