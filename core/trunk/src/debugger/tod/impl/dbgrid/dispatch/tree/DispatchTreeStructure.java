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
package tod.impl.dbgrid.dispatch.tree;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import tod.core.database.browser.ILocationStore;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.db.NodeRejectedException;
import tod.impl.dbgrid.dispatch.AbstractEventDispatcher;
import tod.impl.dbgrid.dispatch.EventDispatcher;
import tod.impl.dbgrid.dispatch.RIDatabaseNode;
import tod.impl.dbgrid.dispatch.RIDispatchNode;
import tod.impl.dbgrid.dispatch.RIDispatcher;
import tod.impl.dbgrid.dispatch.RIEventDispatcher;


/**
 * Models the structure of the dispatching tree.
 * The {@link GridMaster} delegates all the registering and interconnection
 * of nodes to this class.
 * Instances of this class are not reusable.
 * @author gpothier
 */
public abstract class DispatchTreeStructure
{
	private GridMaster itsMaster;
	
	private int itsExpectedDispatchers;
	private int itsExpectedDatabaseNodes;
	
	
	private List<RIDatabaseNode> itsDatabaseNodes = new ArrayList<RIDatabaseNode>();
	private List<RIEventDispatcher> itsDispatchers = new ArrayList<RIEventDispatcher>();
	
	private AbstractEventDispatcher itsRootDispatcher;
	
	/**
	 * The different possible node roles (database, dispatcher).
	 * @author gpothier
	 */
	public static enum NodeRole
	{
		DATABASE()
		{
			@Override
			public boolean isCompatible(RIDispatchNode aNode)
			{
				return aNode instanceof RIDatabaseNode;
			}
		}, 
		DISPATCHER()
		{
			@Override
			public boolean isCompatible(RIDispatchNode aNode)
			{
				return aNode instanceof RIDispatcher;
			}
		};
		
		/**
		 * Indicates if the given dispatch node is compatible with this role.
		 */
		public abstract boolean isCompatible(RIDispatchNode aNode);
	}
	
	
	
	public DispatchTreeStructure(
			int aExpectedDatabaseNodes,
			int aExpectedDispatchers)
	{
		itsExpectedDispatchers = aExpectedDispatchers;
		itsExpectedDatabaseNodes = aExpectedDatabaseNodes;
	}
	
	/**
	 * Sets the master that owns this DTS. Can be done only once.
	 */
	public void setMaster(GridMaster aMaster)
	{
		assert itsMaster == null;
		itsMaster = aMaster;
	}

	protected GridMaster getMaster()
	{
		return itsMaster;
	}

	/**
	 * Returns the total number of nodes in the tree.
	 */
	public int total()
	{
		return itsExpectedDispatchers+itsExpectedDatabaseNodes;
	}

	
	/**
	 * Assigns a role to a node executing on the given host.
	 * See {@link RIGridMaster#getRoleForNode(String)}
	 */
	public synchronized final NodeRole getRoleForNode(String aHostName)
	{
		return getRoleForNode0(aHostName);
	}
	
	protected abstract NodeRole getRoleForNode0(String aHostName);
	

	public List<RIDatabaseNode> getDatabaseNodes()
	{
		return itsDatabaseNodes;
	}

	public List<RIEventDispatcher> getDispatchers()
	{
		return itsDispatchers;
	}

	public AbstractEventDispatcher getRootDispatcher()
	{
		return itsRootDispatcher;
	}

	protected void setRootDispatcher(AbstractEventDispatcher aRootDispatcher)
	{
		itsRootDispatcher = aRootDispatcher;
	}
	
	protected void incExpectedDatabaseNodes()
	{
		itsExpectedDatabaseNodes++;
	}

	protected void incExpectedDispatchers()
	{
		itsExpectedDispatchers++;
	}
	
	protected int getExpectedDatabaseNodes()
	{
		return itsExpectedDatabaseNodes;
	}

	protected int getExpectedDispatchers()
	{
		return itsExpectedDispatchers;
	}

	/**
	 * Registers a node so that it will be included in the dispatching tree. 
	 */
	public synchronized String registerNode(RIDispatchNode aNode, String aHostname) throws NodeRejectedException
	{
		String theId;
		
		if (aNode instanceof RIDatabaseNode)
		{
			RIDatabaseNode theDatabaseNode = (RIDatabaseNode) aNode;
			theId = registerDatabaseNode(theDatabaseNode, aHostname);
		}
		else if (aNode instanceof RIDispatcher)
		{
			RIDispatcher theInternalDispatcher = (RIDispatcher) aNode;
			theId = registerDispatcher(theInternalDispatcher, aHostname);
		}
		else throw new RuntimeException("Not handled: "+aNode);
				
		return theId;
	}
	
	protected String registerDatabaseNode(RIDatabaseNode aNode, String aHostname) throws NodeRejectedException
	{
		int theId = itsDatabaseNodes.size()+1;
		itsDatabaseNodes.add(aNode);
		System.out.println("Registered node (RMI): "+theId+" from "+aHostname);
		
		return "db-"+theId;
	}
	
	protected String registerDispatcher(
			RIEventDispatcher aDispatcher, 
			String aHostname) throws NodeRejectedException
	{
		int theId = itsDispatchers.size()+1;
		itsDispatchers.add(aDispatcher);
		System.out.println("Registered internal dispatcher (RMI): "+theId+" from "+aHostname);
		
		return "internal-"+theId;
	}
	
	/**
	 * Waits until all expected nodes are connected.
	 */
	protected void waitNodes() throws InterruptedException
	{
		while (getDatabaseNodes().size() < getExpectedDatabaseNodes()
				|| getDispatchers().size() < getExpectedDispatchers())
		{
			Thread.sleep(1000);
			System.out.println(String.format(
					"Found %d/%d nodes, %d/%d internal dispatchers.",
					getDatabaseNodes().size(), getExpectedDatabaseNodes(),
					getDispatchers().size(), getExpectedDispatchers()));
		}
	}
	

	/**
	 * Creates the appropriate root dispatcher according to the
	 * number of expected nodes.
	 */
	protected void createRootDispatcher() throws RemoteException
	{
		setRootDispatcher(new EventDispatcher());
	}
	
	/**
	 * Waits until all nodes and dispatchers are properly connected.
	 */
	public final void waitReady()
	{
		waitReady0();
	}
	
	protected abstract void waitReady0();

	@Override
	public String toString()
	{
		return String.format(
				"Tree structure: %d db nodes, %d dispatchers",
				itsExpectedDatabaseNodes,
				itsExpectedDispatchers);
	}
}
