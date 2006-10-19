/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid.aggregator;

import java.rmi.RemoteException;

import tod.core.database.browser.IEventBrowser;
import tod.impl.dbgrid.dbnode.RIEventIterator;

/**
 * Remote interface for a {@link QueryAggregator}.
 * @author gpothier
 */
public interface RIQueryAggregator extends RIEventIterator
{
	/**
	 * Semantic matches {@link IEventBrowser#getEventCounts(long, long, int)}
	 */
	public long[] getEventCounts(
			long aT1, 
			long aT2, 
			int aSlotsCount,
			boolean aForceMergeCounts) throws RemoteException;

}
