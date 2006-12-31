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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import tod.core.database.structure.IHostInfo;
import tod.core.transport.LogReceiver;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.NodeException;
import tod.impl.dbgrid.RIGridMaster;
import zz.utils.Future;
import zz.utils.Task;
import zz.utils.net.Server;
import zz.utils.net.Server.ServerAdress;

/**
 * Base class for internal and leaf dispatchers in the
 * dispatching hierarchy.
 * @author gpothier
 */
public abstract class AbstractEventDispatcher extends AbstractDispatchNode 
implements RIEventDispatcher
{
	private MyServer itsServer;
	
	private List<DispatchNodeProxy> itsChildren = new ArrayList<DispatchNodeProxy>();

	/**
	 * This field is set when the master detects an exception in a node.
	 */
	private NodeException itsNodeException;

	public AbstractEventDispatcher() throws RemoteException
	{
		itsServer = new MyServer();
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
	public synchronized final void acceptChild(Socket aSocket)
	{
		try
		{
			DataInputStream theStream = new DataInputStream(aSocket.getInputStream());
			String theId = theStream.readUTF();
			
			Registry theRegistry = LocateRegistry.getRegistry(DebuggerGridConfig.MASTER_HOST);
			RIDispatchNode theConnectable = (RIDispatchNode) theRegistry.lookup(theId);

			DispatchNodeProxy theProxy = createProxy(theConnectable, aSocket, theId);
			addChild(theProxy);
			System.out.println("Dispatcher accept node (socket): "+theId);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Sets up the {@link DispatchNodeProxy} that handles the connection
	 * with a given node.
	 * @param aConnectable The node handled by this proxy
	 * @param aSocket The socket through which the proxy should
	 * communicate with the node.
	 * @param aId The id of the node.
	 */
	protected abstract DispatchNodeProxy createProxy(
			RIDispatchNode aConnectable,
			Socket aSocket, 
			String aId);


	public ServerAdress getAdress() 
	{
		return itsServer.getAdress();
	}
	
	protected void addChild(DispatchNodeProxy aProxy)
	{
		itsChildren.add(aProxy);
	}
	
	protected DispatchNodeProxy getChild(int aIndex)
	{
		return itsChildren.get(aIndex);
	}
	
	protected int getChildrenCount()
	{
		return itsChildren.size();
	}
	
	protected Iterable<DispatchNodeProxy> getChildren()
	{
		return itsChildren;
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
			InputStream aInStream,
			OutputStream aOutStream, 
			boolean aStartImmediately);
	
	/**
	 * Forks a task to all attached nodes, and returns when all nodes
	 * complete the task.
	 * @return The result returned by each node.
	 */
	private <T> List<T> fork(final Task<DispatchNodeProxy, T> aTask)
	{
		// TODO: maybe use something else than Future...
		List<Future<T>> theFutures = new ArrayList<Future<T>>();
		for (DispatchNodeProxy theProxy : itsChildren)
		{
			final DispatchNodeProxy theProxy0 = theProxy;
			theFutures.add (new Future<T>()
			{
				@Override
				protected T fetch() throws Throwable
				{
					return aTask.run(theProxy0);
				}
			});
		}
		
		List<T> theResult = new ArrayList<T>();
		for (Future<T> theFuture : theFutures) theResult.add(theFuture.get());
		
		return theResult;
	}

	/**
	 * Clears each child (node or dispatcher).
	 */
	public synchronized void clear()
	{
		System.out.println("Event dispatcher: clearing...");
		
		fork(new Task<DispatchNodeProxy, Object>() {
			public Object run(DispatchNodeProxy aProxy)
			{
				aProxy.clear();
				return null;
			}
		});

		System.out.println("Event dispatcher: cleared.");
	}
	
	/**
	 * Flushes all buffers so that events are sent to the nodes 
	 * and stored.
	 */
	public synchronized int flush()
	{
		System.out.println("Event dispatcher: flushing...");

		List<Integer> theResults = fork(new Task<DispatchNodeProxy, Integer>() {
			public Integer run(DispatchNodeProxy aProxy)
			{
				return aProxy.flush();
			}
		});
		
		int theCount = 0;
		for (Integer theResult : theResults) theCount += theResult;

		System.out.println("Event dispatcher: flushed "+theCount+" events.");
		return theCount;
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
			super(DebuggerGridConfig.MASTER_NODE_PORT+(int)(Math.random()*100));
		}

		@Override
		protected void accepted(Socket aSocket)
		{
			acceptChild(aSocket);
		}
	}
	

}
