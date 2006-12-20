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

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.NodeException;
import tod.impl.dbgrid.messages.GridEvent;
import zz.utils.Future;
import zz.utils.Task;

public abstract class EventDispatcher
{
	private GridMaster itsMaster;
	
	private List<DBNodeProxy> itsNodes = new ArrayList<DBNodeProxy>();
	
	private boolean itsFlushed = false;
	
	/**
	 * This field is set when the master detects an exception in a node.
	 */
	private NodeException itsNodeException;
	
	public EventDispatcher(GridMaster aMaster)
	{
		itsMaster = aMaster;
	}
	
	public GridMaster getMaster()
	{
		return itsMaster;
	}

	/**
	 * Sets the node exception. It will be reported at the next client method call 
	 */
	public void nodeException(NodeException aException)
	{
		itsNodeException = aException;
	}
	
	public DBNodeProxy addNode(Socket aSocket, int aId) 
	{
		DBNodeProxy theProxy = createProxy(aSocket, aId);
		itsNodes.add(theProxy);
		return theProxy;
	}
	
	protected abstract DBNodeProxy createProxy(Socket aSocket, int aId);
	
	protected DBNodeProxy getNode(int aIndex)
	{
		return itsNodes.get(aIndex);
	}
	
	protected int getNodesCount()
	{
		return itsNodes.size();
	}
	
	/**
	 * Forks a task to all attached nodes, and returns when all nodes
	 * complete the task.
	 */
	private void fork(final Task<DBNodeProxy> aTask)
	{
		// TODO: maybe use something else than Future...
		List<Future<Boolean>> theFutures = new ArrayList<Future<Boolean>>();
		for (DBNodeProxy theProxy : itsNodes)
		{
			final DBNodeProxy theProxy0 = theProxy;
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
	 * Clears each node.
	 */
	public synchronized void clear()
	{
		System.out.println("Event dispatcher: clearing...");
		
		fork(new Task<DBNodeProxy>() {
			public void run(DBNodeProxy aParameter)
			{
				aParameter.clear();
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

		fork(new Task<DBNodeProxy>() {
			public void run(DBNodeProxy aParameter)
			{
				aParameter.flush();
			}
		});

		itsFlushed = true;
		System.out.println("Event dispatcher: flushed.");
	}

	
	/**
	 * Directly dispatches a grid event
	 */
	public synchronized final void dispatchEvent(GridEvent aEvent)
	{
		if (itsNodeException != null) 
		{
			NodeException theException = itsNodeException;
			itsNodeException = null;
			throw theException;
		}

		dispatchEvent0(aEvent);
	}
	
	protected abstract void dispatchEvent0(GridEvent aEvent);
	
}
