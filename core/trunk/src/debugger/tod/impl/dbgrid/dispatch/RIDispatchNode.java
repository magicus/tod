/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

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
*/
package tod.impl.dbgrid.dispatch;

import java.rmi.Remote;
import java.rmi.RemoteException;

import zz.utils.net.Server.ServerAdress;

/**
 * Remote interface for dispatch nodes (dispatchers and db nodes)
 * @author gpothier
 */
public interface RIDispatchNode extends Remote
{
	/**
	 * Returns the id of this node
	 */
	public String getNodeId() throws RemoteException;
	

	/**
	 * Tells this node to establish its incoming data connection to
	 * the dispatcher at the specified adress.
	 */
	public void connectToDispatcher(ServerAdress aAdress) throws RemoteException;

	/**
	 * Initializes or reinitializes the database.
	 */
	public void clear() throws RemoteException;
	
	/**
	 * Flushes all buffered data.
	 * @return The number of flushed events.
	 */
	public int flush() throws RemoteException;
	

}
