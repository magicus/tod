/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.queries;


import tod.impl.database.IBidiIterator;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.file.SimpleTuple;
import tod.impl.evdbng.messages.GridEventNG;

/**
 * Represents a condition on bytecode location
 * @author gpothier
 */
public class BytecodeLocationCondition extends SimpleCondition<SimpleTuple>
{
	private static final long serialVersionUID = 7843402757580345452L;
	private int itsBytecodeLocation;

	public BytecodeLocationCondition(int aBytecodeLocation)
	{
		itsBytecodeLocation = aBytecodeLocation;
	}

	@Override
	public IBidiIterator<SimpleTuple> createTupleIterator(Indexes aIndexes, long aEventId)
	{
		return aIndexes.getLocationIndex(itsBytecodeLocation).getTupleIterator(aEventId);
	}

	@Override
	public boolean _match(GridEventNG aEvent)
	{
		return aEvent.getProbeInfo().bytecodeIndex == itsBytecodeLocation;
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("Bytecode index = %d", itsBytecodeLocation);
	}

}