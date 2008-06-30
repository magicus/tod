/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An iterator that buffers data into packets.
 * @param <T> Should be an array type
 * @author gpothier
 */
public interface RIBufferIterator<T> extends Remote
{
	/**
	 * Fetches elements following the cursor position, and updates the cursor.
	 * @param aCount Maximum number of elements to fetch.
	 * @return The fetched elements, or null if there are no more events.
	 */
	public T next(int aCount) throws RemoteException;
	
	/**
	 * Fetches elements preceeding the cursor position, and updates the cursor.
	 * @param aCount Maximum number of elements to fetch.
	 * @return The fetched elements, or null if there are no more events.
	 */
	public T previous(int aCount) throws RemoteException;

}
