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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

import tod.core.database.browser.ILocationStore;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.ThreadInfo;
import tod.core.transport.CollectorLogReceiver;
import tod.core.transport.LogReceiver;
import tod.impl.dbgrid.GridEventCollector;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * A leaf event dispatcher in the dispatching hierarchy.
 * Leaf dispatchers dispatch to database nodes through {@link DBNodeProxy}.
 * @author gpothier
 */
public abstract class LeafEventDispatcher extends AbstractEventDispatcher
implements RILeafDispatcher
{
	/**
	 * A leaf dispatcher maintains a local copy of the location
	 * store for efficiency reasons.
	 * The registrer (actually the repository) is needed for
	 * exceptions processing.
	 * If the root dispatcher is a leaf, the location store
	 * is shared with that of the master. Otherwise each leaf dispatcher
	 * has its own store.
	 * @see EventCollector#exception(int, long, short, long, String, String, String, int, Object).
	 */
	private ILocationStore itsLocationStore;
	
	public LeafEventDispatcher(boolean aConnectToMaster, ILocationStore aLocationStore) throws RemoteException
	{
		super(aConnectToMaster);
		itsLocationStore = aLocationStore;
	}
	
	protected DBNodeProxy getNode(int aIndex)
	{
		return (DBNodeProxy) getChild(aIndex);
	}
	
	@Override
	public LogReceiver createLogReceiver(
			IHostInfo aHostInfo, 
			GridMaster aMaster,
			InputStream aInStream,
			OutputStream aOutStream, boolean aStartImmediately)
	{
		MyCollector theCollector = new MyCollector(
				aMaster,
				aHostInfo,
				itsLocationStore,
				this);
		
		return new CollectorLogReceiver(
				theCollector,
				itsLocationStore,
				aInStream,
				aOutStream,
				aStartImmediately);
	}

	@Override
	protected void connectToDispatcher(Socket aSocket)
	{
		try
		{
			createLogReceiver(
					null, 
					null, 
					aSocket.getInputStream(), 
					aSocket.getOutputStream(), 
					true);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Dispatches a grid event to the children of this dispatcher 
	 * (nodes or other dispatchers). 
	 */
	public final void dispatchEvent(GridEvent aEvent)
	{
		checkNodeException();
		dispatchEvent0(aEvent);
	}
	
	/**
	 * Dispatches a grid event to the children of this dispatcher 
	 * (nodes or other dispatchers). 
	 */
	protected abstract void dispatchEvent0(GridEvent aEvent);

	private static class MyCollector extends GridEventCollector
	{
		private GridMaster itsMaster;
		
		public MyCollector(
				GridMaster aMaster, 
				IHostInfo aHost, 
				ILocationsRepository aLocationsRepository,
				LeafEventDispatcher aDispatcher)
		{
			super(aHost, aLocationsRepository, aDispatcher);
			itsMaster = aMaster;
		}

		@Override
		public void thread(int aThreadId, long aJVMThreadId, String aName)
		{
			ThreadInfo theThread = createThreadInfo(getHost(), aThreadId, aJVMThreadId, aName);
			itsMaster.registerThread(theThread);
		}
	}
	
}
