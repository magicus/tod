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
package tod.impl.dbgrid.dispatcher;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import tod.core.LocationRegistrer;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.ThreadInfo;
import tod.core.transport.CollectorLogReceiver;
import tod.core.transport.LogReceiver;
import tod.impl.dbgrid.GridEventCollector;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.messages.GridEvent;
import zz.utils.net.Server.ServerAdress;

/**
 * A leaf event dispatcher in the dispatching hierarchy.
 * Leaf dispatchers dispatch to database nodes through {@link DBNodeProxy}.
 * @author gpothier
 */
public abstract class LeafEventDispatcher extends AbstractEventDispatcher
{
	public LeafEventDispatcher(boolean aConnectToMaster) throws RemoteException
	{
		super(aConnectToMaster);
	}
	
	public synchronized void acceptChild(Socket aSocket)
	{
		try
		{
			DataInputStream theStream = new DataInputStream(aSocket.getInputStream());
			int theId = theStream.readInt();
			
			DBNodeProxy theProxy = createProxy(aSocket, theId);
			addChild(theProxy);
			System.out.println("Leaf dispatcher accept node (socket): "+theId);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	protected abstract DBNodeProxy createProxy(Socket aSocket, int aId);
	
	protected DBNodeProxy getNode(int aIndex)
	{
		return (DBNodeProxy) getChild(aIndex);
	}
	
	@Override
	public LogReceiver createLogReceiver(
			IHostInfo aHostInfo, 
			GridMaster aMaster,
			LocationRegistrer aRegistrer,
			InputStream aInStream,
			OutputStream aOutStream,
			boolean aStartImmediately)
	{
		MyCollector theCollector = new MyCollector(
				aMaster,
				aHostInfo,
				aRegistrer,
				this);
		
		return new CollectorLogReceiver(
				theCollector,
				aRegistrer,
				aInStream,
				aOutStream,
				aStartImmediately);
	}
	
	public void connectToDispatcher(ServerAdress aAdress) 
	{
		try
		{
			Socket theSocket = aAdress.connect();
			createLogReceiver(
					null, 
					null, 
					null, 
					theSocket.getInputStream(), 
					theSocket.getOutputStream(), 
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
