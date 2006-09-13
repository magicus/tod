/*
 * Created on Sep 11, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.rmi.Remote;
import java.rmi.RemoteException;

import tod.impl.dbgrid.messages.GridEvent;

/**
 * Remote interface for {@link EventIterator}.
 * @author gpothier
 */
public interface RIEventIterator extends Remote
{
	/**
	 * Semantics matches {@link IEventBrowser#setNextTimestamp(long)}
	 */
	public void setNextTimestamp (long aTimestamp) throws RemoteException;
	
	/**
	 * Semantics matches {@link IEventBrowser#setPreviousTimestamp(long)}
	 */
	public void setPreviousTimestamp (long aTimestamp) throws RemoteException;

	/**
	 * Fetches events following the cursor position, and updates the cursor.
	 * @param aCount Maximum number of events to fetch.
	 * @return The fetched events, or null if there are no more events.
	 */
	public GridEvent[] next(int aCount) throws RemoteException;
	
	/**
	 * Fetches events preceeding the cursor position, and updates the cursor.
	 * The events are returned in reverse order, ie. the first event of the array
	 * is the latest.
	 * @param aCount Maximum number of events to fetch.
	 * @return The fetched events, or null if there are no more events.
	 */
	public GridEvent[] previous(int aCount) throws RemoteException;
}
