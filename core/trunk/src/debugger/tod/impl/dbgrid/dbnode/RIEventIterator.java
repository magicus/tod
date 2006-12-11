/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.impl.dbgrid.dbnode;

import java.rmi.Remote;
import java.rmi.RemoteException;

import tod.core.database.browser.IEventBrowser;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Remote interface for {@link NodeEventIterator}.
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
