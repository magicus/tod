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
import tod.impl.dbgrid.dispatch.InternalEventDispatcher;
import tod.impl.dbgrid.dispatch.LeafEventDispatcher;
import tod.impl.dbgrid.dispatch.RIDatabaseNode;
import tod.impl.dbgrid.dispatch.RIDispatchNode;
import tod.impl.dbgrid.dispatch.RIEventDispatcher;
import tod.impl.dbgrid.dispatch.RIInternalDispatcher;
import tod.impl.dbgrid.dispatch.RILeafDispatcher;
import tod.impl.dbgrid.gridimpl.GridImpl;
import tod.impl.dbgrid.gridimpl.IGridImplementationFactory;


/**
 * Models the structure of the dispatching tree.
 * The {@link GridMaster} delegates all the registering and interconnection
 * of nodes to this class.
 * Instances of this class are not reusable.
 * @author gpothier
 */
public abstract class DispatchTreeStructure
{
	private int itsExpectedLeafDispatchers;
	private int itsExpectedInternalDispatchers;
	private int itsExpectedDatabaseNodes;
	
	
	private List<RIDatabaseNode> itsDatabaseNodes = new ArrayList<RIDatabaseNode>();
	private List<RILeafDispatcher> itsLeafDispatchers = new ArrayList<RILeafDispatcher>();
	private List<RIEventDispatcher> itsInternalDispatchers = new ArrayList<RIEventDispatcher>();
	
	private AbstractEventDispatcher itsRootDispatcher;
	
	/**
	 * The different possible node roles (database, leaf dispatcher,
	 * internal dispatcher).
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
		LEAF_DISPATCHER()
		{
			@Override
			public boolean isCompatible(RIDispatchNode aNode)
			{
				return aNode instanceof RILeafDispatcher;
			}
		}, 
		INTERNAL_DISPATCHER()
		{
			@Override
			public boolean isCompatible(RIDispatchNode aNode)
			{
				return aNode instanceof RIInternalDispatcher;
			}
		};
		
		/**
		 * Indicates if the given dispatch node is compatible with this role.
		 */
		public abstract boolean isCompatible(RIDispatchNode aNode);
	}
	
	
	
	public DispatchTreeStructure(
			int aExpectedDatabaseNodes,
			int aExpectedLeafDispatchers,
			int aExpectedInternalDispatchers)
	{
		itsExpectedLeafDispatchers = aExpectedLeafDispatchers;
		itsExpectedInternalDispatchers = aExpectedInternalDispatchers;
		itsExpectedDatabaseNodes = aExpectedDatabaseNodes;
	}

	/**
	 * Returns the total number of nodes in the tree.
	 */
	public int total()
	{
		return itsExpectedLeafDispatchers+itsExpectedInternalDispatchers+itsExpectedDatabaseNodes;
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

	public List<RIEventDispatcher> getInternalDispatchers()
	{
		return itsInternalDispatchers;
	}

	public List<RILeafDispatcher> getLeafDispatchers()
	{
		return itsLeafDispatchers;
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

	protected void incExpectedLeafDispatchers()
	{
		itsExpectedLeafDispatchers++;
	}
	
	protected void incExpectedInternalDispatchers()
	{
		itsExpectedInternalDispatchers++;
	}
	
	protected int getExpectedDatabaseNodes()
	{
		return itsExpectedDatabaseNodes;
	}

	protected int getExpectedInternalDispatchers()
	{
		return itsExpectedInternalDispatchers;
	}

	protected int getExpectedLeafDispatchers()
	{
		return itsExpectedLeafDispatchers;
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
		else if (aNode instanceof RILeafDispatcher)
		{
			RILeafDispatcher theLeafDispatcher = (RILeafDispatcher) aNode;
			theId = registerLeafDispatcher(theLeafDispatcher, aHostname);
		}
		else if (aNode instanceof RIInternalDispatcher)
		{
			RIInternalDispatcher theInternalDispatcher = (RIInternalDispatcher) aNode;
			theId = registerInternalDispatcher(theInternalDispatcher, aHostname);
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
	
	protected String registerLeafDispatcher(
			RILeafDispatcher aDispatcher, 
			String aHostname) throws NodeRejectedException
	{
		int theId = itsLeafDispatchers.size()+1;
		itsLeafDispatchers.add(aDispatcher);
		System.out.println("Registered leaf dispatcher (RMI): "+theId+" from "+aHostname);
		
		return "leaf-"+theId;
	}
	
	protected String registerInternalDispatcher(
			RIEventDispatcher aDispatcher, 
			String aHostname) throws NodeRejectedException
	{
		int theId = itsInternalDispatchers.size()+1;
		itsInternalDispatchers.add(aDispatcher);
		System.out.println("Registered internal dispatcher (RMI): "+theId+" from "+aHostname);
		
		return "internal-"+theId;
	}
	
	/**
	 * Waits until all expected nodes are connected.
	 */
	protected void waitNodes() throws InterruptedException
	{
		while (getDatabaseNodes().size() < getExpectedDatabaseNodes()
				|| getLeafDispatchers().size() < getExpectedLeafDispatchers()
				|| getInternalDispatchers().size() < getExpectedInternalDispatchers())
		{
			Thread.sleep(1000);
			System.out.println(String.format(
					"Found %d/%d nodes, %d/%d internal dispatchers, %d/%d leaf dispatchers.",
					getDatabaseNodes().size(), getExpectedDatabaseNodes(),
					getInternalDispatchers().size(), getExpectedInternalDispatchers(),
					getLeafDispatchers().size(), getExpectedLeafDispatchers()));
		}
	}
	

	/**
	 * Creates the appropriate root dispatcher according to the
	 * number of expected nodes.
	 */
	protected void createRootDispatcher(GridMaster aMaster) throws RemoteException
	{
		ILocationStore theLocationStore = aMaster.getLocationStore();
		
		// Create root dispatcher (local)
		if (getExpectedLeafDispatchers() == 0)
		{
			assert getExpectedInternalDispatchers() == 0;
			IGridImplementationFactory theFactory = GridImpl.getFactory(aMaster.getConfig());
			LeafEventDispatcher theDispatcher = theFactory.createLeafDispatcher(false, theLocationStore);
			theDispatcher.connectToLocalMaster(aMaster, "root");
			setRootDispatcher(theDispatcher);
			getLeafDispatchers().add(theDispatcher);
		}
		else
		{
			InternalEventDispatcher theDispatcher = new InternalEventDispatcher();
			if (theLocationStore != null) theDispatcher.forwardLocations(theLocationStore.getLocations());
			setRootDispatcher(theDispatcher);
		}

	}
	/**
	 * Waits until all nodes and dispatchers are properly connected.
	 * @param aMaster The master that is using this {@link DispatchTreeStructure}.
	 */
	public abstract void waitReady(GridMaster aMaster);

	@Override
	public String toString()
	{
		return String.format(
				"Tree structure: %d db nodes, %d leaf dispatch, %d internal dispatch",
				itsExpectedDatabaseNodes,
				itsExpectedLeafDispatchers,
				itsExpectedInternalDispatchers);
	}
	

}
