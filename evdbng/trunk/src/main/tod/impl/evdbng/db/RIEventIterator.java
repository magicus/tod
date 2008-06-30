/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import java.rmi.Remote;
import java.rmi.RemoteException;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;
import tod.impl.evdbng.messages.GridEventNG;

/**
 * Remote interface for {@link NodeEventIterator}.
 * @author gpothier
 */
public interface RIEventIterator extends RIBufferIterator<GridEventNG[]>
{
	/**
	 * Semantics matches {@link IEventBrowser#setNextTimestamp(long)}
	 */
	public void setNextTimestamp (long aTimestamp) throws RemoteException;
	
	/**
	 * Semantics matches {@link IEventBrowser#setPreviousTimestamp(long)}
	 */
	public void setPreviousTimestamp (long aTimestamp) throws RemoteException;

	
}
