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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import tod.core.LocationRegistrer;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.HostInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.transport.LogReceiver;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.NodeException;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.dbnode.NodeRejectedException;
import tod.impl.dbgrid.messages.GridEvent;
import zz.utils.Future;
import zz.utils.Task;
import zz.utils.net.Server;
import zz.utils.net.Server.ServerAdress;

/**
 * Base class for internal and leaf dispatchers in the
 * dispatching hierarchy.
 * @author gpothier
 */
public abstract class AbstractEventDispatcher extends UnicastRemoteObject 
implements RIEventDispatcher
{
	private MyServer itsServer;
	
	private boolean itsFlushed = false;

	private List<DispatchTreeNodeProxy> itsChildren = new ArrayList<DispatchTreeNodeProxy>();

	/**
	 * This field is set when the master detects an exception in a node.
	 */
	private NodeException itsNodeException;

	public AbstractEventDispatcher(boolean aConnectToMaster) throws RemoteException
	{
		itsServer = new MyServer();
		try
		{
			if (aConnectToMaster) connectToMaster();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void connectToMaster() throws IOException, NotBoundException
	{
		// Setup RMI connection
		Registry theRegistry = LocateRegistry.getRegistry(DebuggerGridConfig.MASTER_HOST);
		RIGridMaster theMaster = (RIGridMaster) theRegistry.lookup(GridMaster.RMI_ID);

		try
		{
			theMaster.registerDispatcher(
					this, 
					InetAddress.getLocalHost().getHostName(), 
					this instanceof LeafEventDispatcher);
		}
		catch (NodeRejectedException e)
		{
			System.out.println("Rejected by master: "+e.getMessage());
			System.exit(1);
		}
		
		System.out.println("Dispatcher connected to master.");
	}
	


	/**
	 * Sets the node exception. It will be reported at the 
	 * next client method call. 
	 */
	public void nodeException(NodeException aException)
	{
		itsNodeException = aException;
	}
	
	/**
	 * If there is a current node exception, throw it, and clear it.
	 */
	protected void checkNodeException()
	{
		NodeException theNodeException = itsNodeException;
		itsNodeException = null;
		if (theNodeException != null) throw theNodeException;
	}

	/**
	 * This method is called when the dispatcher accepts a connection
	 * from a child (dispatcher or node).
	 */
	protected abstract void acceptChild(Socket aSocket);

	public ServerAdress getAdress() 
	{
		return itsServer.getAdress();
	}
	
	protected void addChild(DispatchTreeNodeProxy aProxy)
	{
		itsChildren.add(aProxy);
	}
	
	protected DispatchTreeNodeProxy getChild(int aIndex)
	{
		return itsChildren.get(aIndex);
	}
	
	protected int getChildrenCount()
	{
		return itsChildren.size();
	}
	
	/**
	 * Creates a log receiver that is able to communicate directly
	 * with this dispatcher. The grid master requests a log receiver
	 * to its root dispatcher whenever a new client connects.
	 * @param aStartImmediately Whether the receiver should immediately
	 * start its thread. This is for testing only.
	 */
	public abstract LogReceiver createLogReceiver(
			IHostInfo aHostInfo, 
			GridMaster aMaster, 
			LocationRegistrer aRegistrer,
			InputStream aInStream, 
			OutputStream aOutStream,
			boolean aStartImmediately);
	
	/**
	 * Forks a task to all attached nodes, and returns when all nodes
	 * complete the task.
	 */
	private void fork(final Task<DispatchTreeNodeProxy> aTask)
	{
		// TODO: maybe use something else than Future...
		List<Future<Boolean>> theFutures = new ArrayList<Future<Boolean>>();
		for (DispatchTreeNodeProxy theProxy : itsChildren)
		{
			final DispatchTreeNodeProxy theProxy0 = theProxy;
			theFutures.add (new Future<Boolean>()
			{
				@Override
				protected Boolean fetch() throws Throwable
				{
					aTask.run(theProxy0);
					return true;
				}
			});
		}
		
		for (Future<Boolean> theFuture : theFutures) theFuture.get();
	}

	/**
	 * Clears each child (node or dispatcher).
	 */
	public synchronized void clear()
	{
		System.out.println("Event dispatcher: clearing...");
		
		fork(new Task<DispatchTreeNodeProxy>() {
			public void run(DispatchTreeNodeProxy aProxy)
			{
				aProxy.clear();
			}
		});

		itsFlushed = false;
		System.out.println("Event dispatcher: cleared.");
	}
	
	/**
	 * Flushes all buffers so that events are sent to the nodes 
	 * and stored.
	 */
	public synchronized void flush()
	{
		System.out.println("Event dispatcher: flushing...");

		fork(new Task<DispatchTreeNodeProxy>() {
			public void run(DispatchTreeNodeProxy aProxy)
			{
				aProxy.flush();
			}
		});

		itsFlushed = true;
		System.out.println("Event dispatcher: flushed.");
	}
	

	
	/**
	 * A server that waits for children (dispatchers or database nodes)
	 *  to connect
	 * @author gpothier
	 */
	private class MyServer extends Server
	{
		public MyServer()
		{
			super(DebuggerGridConfig.MASTER_NODE_PORT);
		}

		@Override
		protected void accepted(Socket aSocket)
		{
			acceptChild(aSocket);
		}
	}
	

}
