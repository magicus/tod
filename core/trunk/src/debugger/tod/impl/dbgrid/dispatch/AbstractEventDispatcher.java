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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import tod.core.config.TODConfig;
import tod.core.database.structure.HostInfo;
import tod.core.transport.LogReceiver;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.NodeException;
import zz.utils.Future;
import zz.utils.ITask;
import zz.utils.Utils;
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
	

	public AbstractEventDispatcher(boolean aStartServer) throws RemoteException
	{
		if (aStartServer) itsServer = new MyServer();
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
	public final void acceptChild(Socket aSocket)
	{
		try
		{
			acceptChild(
					new BufferedInputStream(aSocket.getInputStream()),
					new BufferedOutputStream(aSocket.getOutputStream()));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public synchronized final String acceptChild(
			InputStream aInputStream,
			OutputStream aOutputStream)
	{
		try
		{
			DataInputStream theStream = new DataInputStream(aInputStream);
			String theId = theStream.readUTF();
			
			Registry theRegistry = LocateRegistry.getRegistry(DebuggerGridConfig.MASTER_HOST);
			RIDispatchNode theNode = (RIDispatchNode) theRegistry.lookup(theId);

			acceptChild(theId, theNode, aInputStream, aOutputStream);
			return theId;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public synchronized final void acceptChild(
			String aId,
			RIDispatchNode aNode,
			InputStream aInputStream,
			OutputStream aOutputStream)
	{
		DispatchNodeProxy theProxy = createProxy(
				aNode,
				aInputStream,
				aOutputStream, 
				aId);
		
		addChild(theProxy);
		System.out.println("Dispatcher accept node (socket): "+aId);
	}
	
	/**
	 * Sets up the {@link DispatchNodeProxy} that handles the connection
	 * with a given node.
	 * @param aConnectable The node handled by this proxy
	 * @param aId The id of the node.
	 */
	protected abstract DispatchNodeProxy createProxy(
			RIDispatchNode aConnectable,
			InputStream aInputStream,
			OutputStream aOutputStream,
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
	 * Forks a task to all attached nodes, and returns when all nodes
	 * complete the task.
	 * @return The result returned by each node.
	 */
	private <T> List<T> fork(final ITask<DispatchNodeProxy, T> aTask)
	{
		return Utils.fork(itsChildren, aTask);
	}

	/**
	 * Clears each child (node or dispatcher).
	 */
	public synchronized void clear()
	{
		System.out.println("Event dispatcher: clearing...");
		
		fork(new ITask<DispatchNodeProxy, Object>() {
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
		System.out.println("[AbstractEventDispatcher] Flushing...");

		List<Integer> theResults = fork(new ITask<DispatchNodeProxy, Integer>() {
			public Integer run(DispatchNodeProxy aProxy)
			{
				return aProxy.flush();
			}
		});
		
		int theCount = 0;
		for (Integer theResult : theResults) theCount += theResult;

		System.out.println("[AbstractEventDispatcher] Flushed "+theCount+" events.");
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
