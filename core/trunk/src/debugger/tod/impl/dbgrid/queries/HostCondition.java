/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on event host.
 * @author gpothier
 */
public class HostCondition extends SimpleCondition
{
	private static final long serialVersionUID = -6783805983938219464L;
	private int itsHost;

	public HostCondition(int aHost)
	{
		itsHost = aHost;
	}

	@Override
	public Iterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.hostIndex.getIndex(itsHost).getTupleIterator(aTimestamp);
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.getHost() == itsHost;
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("HostId = %d", itsHost);
	}

}
