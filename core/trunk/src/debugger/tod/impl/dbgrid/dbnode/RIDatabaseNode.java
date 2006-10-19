/*
 * Created on Sep 11, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.rmi.Remote;
import java.rmi.RemoteException;

import tod.core.database.browser.IEventBrowser;
import tod.impl.dbgrid.queries.EventCondition;

/**
 * Remote interface for {@link DatabaseNode}, used only
 * for queries, not for event storage.
 * @author gpothier
 */
public interface RIDatabaseNode extends Remote
{
	/**
	 * Returns the id of this node
	 */
	public int getNodeId() throws RemoteException;

	/**
	 * Creates a new event iterator for the given condition.
	 */
	public RINodeEventIterator getIterator(EventCondition aCondition) throws RemoteException;
	
	/**
	 * Semantic matches {@link IEventBrowser#getEventCounts(long, long, int)}
	 */
	public long[] getEventCounts(
			EventCondition aCondition,
			long aT1,
			long aT2,
			int aSlotsCount, 
			boolean aForceMergeCounts) throws RemoteException;
}
