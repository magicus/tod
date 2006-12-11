/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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

import java.util.ArrayList;
import java.util.List;

import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.NodeException;
import tod.impl.dbgrid.messages.GridEvent;
import zz.utils.Future;

public class EventDispatcher
{
	private GridMaster itsMaster;
	
	private List<DBNodeProxy> itsNodes = new ArrayList<DBNodeProxy>();
	private int itsCurrentNode = 0;
	
	private boolean itsFlushed = false;
	
	/**
	 * This field is set when the master detects an exception in a node.
	 */
	private NodeException itsNodeException;
	
	public EventDispatcher(GridMaster aMaster)
	{
		itsMaster = aMaster;
	}
	
	/**
	 * Sets the node exception. It will be reported at the next client method call 
	 */
	public void nodeException(NodeException aException)
	{
		itsNodeException = aException;
	}
	
	public void addNode(DBNodeProxy aProxy) 
	{
		itsNodes.add(aProxy);
	}

	/**
	 * Clears each node.
	 */
	public synchronized void clear()
	{
		System.out.println("Event dispatcher: clearing...");
		
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
					theProxy0.clear();
					return true;
				}
			});
		}
		
		for (Future<Boolean> theFuture : theFutures) theFuture.get();

		itsFlushed = false;
		System.out.println("Event dispatcher: cleared.");

	}
	
	/**
	 * Directly dispatches a grid event
	 */
	public synchronized void dispatchEvent(GridEvent aEvent)
	{
		if (itsNodeException != null) 
		{
			NodeException theException = itsNodeException;
			itsNodeException = null;
			throw theException;
		}
		
		DBNodeProxy theProxy = itsNodes.get(itsCurrentNode);
		theProxy.pushEvent(aEvent);
		
		// The following code is 5 times faster than using a modulo.
		// (Pentium M 2ghz)
		itsCurrentNode++;
		if (itsCurrentNode >= itsNodes.size()) itsCurrentNode = 0;
	}
	
	/**
	 * Flushes all buffers so that events are sent to the nodes 
	 * and stored.
	 */
	public synchronized void flush()
	{
		System.out.println("Event dispatcher: flushing...");
		
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
					theProxy0.flush();
					return true;
				}
			});
		}
		
		for (Future<Boolean> theFuture : theFutures) theFuture.get();

		itsFlushed = true;
		System.out.println("Event dispatcher: flushed.");
	}
}
