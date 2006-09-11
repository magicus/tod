/*
 * Created on Sep 11, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.rmi.Remote;
import java.rmi.RemoteException;

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
	public RIEventIterator getIterator(EventCondition aCondition) throws RemoteException;
}
