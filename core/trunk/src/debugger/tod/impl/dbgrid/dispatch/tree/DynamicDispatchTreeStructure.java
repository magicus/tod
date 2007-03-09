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

import static tod.impl.dbgrid.DebuggerGridConfig.DISPATCH_BRANCHING_FACTOR;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import tod.agent.DebugFlags;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.db.NodeRejectedException;
import tod.impl.dbgrid.dispatch.RIDatabaseNode;
import tod.impl.dbgrid.dispatch.RIDispatchNode;
import tod.impl.dbgrid.dispatch.RIEventDispatcher;
import zz.utils.Utils;

/**
 * A dispatch tree structure that is created dynamically as
 * (previously unknown) hosts connect to the master.
 * @author gpothier
 */
public class DynamicDispatchTreeStructure extends DispatchTreeStructure
{
	/**
	 * Pre-registered database nodes
	 * (see {@link #getRoleForNode(String)}).
	 */
	private int itsPreDatabase;
	private int itsPreDispatchers;

	private Set<String> itsNodeHosts = new HashSet<String>();
	
	/**
	 * We maintain a separate set of host names for pre-registration.
	 * This can probably be improved...
	 */
	private Set<String> itsPreNodeHosts = new HashSet<String>();
	
	public DynamicDispatchTreeStructure(int aDatabaseNodes, int aDispatchers)
	{
		super(aDatabaseNodes, aDispatchers);
		
		try
		{
			if (DebuggerGridConfig.CHECK_SAME_HOST) 
			{
				itsNodeHosts.add(InetAddress.getLocalHost().getHostName());
				itsPreNodeHosts.add(InetAddress.getLocalHost().getHostName());
			}
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}


	@Override
	public NodeRole getRoleForNode0(String aHostName)
	{
		NodeRole theRole;
		
		if (itsPreDatabase < getExpectedDatabaseNodes() 
				&& (! DebuggerGridConfig.CHECK_SAME_HOST || itsPreNodeHosts.add(aHostName)))
		{
			theRole = NodeRole.DATABASE;
			itsPreDatabase++;
		}
		else if (itsPreDispatchers < getExpectedDispatchers())
		{
			theRole = NodeRole.DISPATCHER;
			itsPreDispatchers++;
		}
		else theRole = null;
		
		return theRole;
	}
	
	public void waitReady(GridMaster aMaster)
	{
		try
		{
			waitNodes();
			createRootDispatcher(aMaster);

			int theBranchingFactor = DebugFlags.DISPATCH_FAKE_1 ? 1 : DISPATCH_BRANCHING_FACTOR;
			if (theBranchingFactor == 0) theBranchingFactor = Integer.MAX_VALUE;
			
			// Queue of nodes that must be connected to a parent.
			LinkedList<RIDispatchNode> theChildrenQueue =
				new LinkedList<RIDispatchNode>();
			
			Utils.fillCollection(theChildrenQueue, getDatabaseNodes());

			// Queue of parents.
			LinkedList<RIEventDispatcher> theParentsQueue =
				new LinkedList<RIEventDispatcher>();
			
			Utils.fillCollection(theParentsQueue, getDispatchers());
			theParentsQueue.addFirst(getRootDispatcher());
			
			while (! theParentsQueue.isEmpty())
			{
				RIEventDispatcher theParent = theParentsQueue.removeLast();
				
				for(int i=0;i<theBranchingFactor && !theChildrenQueue.isEmpty();i++)
				{
					RIDispatchNode theChild = theChildrenQueue.removeLast();
					theChild.connectToDispatcher(theParent.getAdress());
				}
				
				theChildrenQueue.addFirst(theParent);
			}
			
			assert theChildrenQueue.removeLast() == getRootDispatcher();
			assert theChildrenQueue.isEmpty();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	protected String registerDatabaseNode(RIDatabaseNode aNode, String aHostname) throws NodeRejectedException
	{
		if (getExpectedDatabaseNodes() > 0 && getDatabaseNodes().size() >= getExpectedDatabaseNodes()) 
			throw new NodeRejectedException("Maximum number of nodes reached");
		
		if (DebuggerGridConfig.CHECK_SAME_HOST && ! itsNodeHosts.add(aHostname)) 
			throw new NodeRejectedException("Refused node from same host");

		return super.registerDatabaseNode(aNode, aHostname);
	}
	
	protected String registerDispatcher(
			RIEventDispatcher aDispatcher, 
			String aHostname) throws NodeRejectedException
	{
		if (getDispatchers().size() >= getExpectedDispatchers()) 
			throw new NodeRejectedException("Maximum number of internal dispatchers reached");
		
		return super.registerDispatcher(aDispatcher, aHostname);
	}
	


	
	/**
	 * Computes a dispatch tree structure given a number of available nodes.
	 */
	public static DispatchTreeStructure computeFromTotal(int aNodes)
	{
		int i=1;
		DispatchTreeStructure theResult = null;
		while(true)
		{
			DispatchTreeStructure theStructure = compute(i++);
			if (theStructure.total() > aNodes) return theResult;
			theResult = theStructure;
		}
	}
	
	public static DispatchTreeStructure compute(int aDatabaseNodes)
	{
		if (DISPATCH_BRANCHING_FACTOR == 0) return new DynamicDispatchTreeStructure(aDatabaseNodes, 0);
		
		int theTotalDispatchers = 0;
		int theDispatchers = (aDatabaseNodes+DISPATCH_BRANCHING_FACTOR-1)/DISPATCH_BRANCHING_FACTOR;
		while (true)
		{
			if (theDispatchers == 1) return new DynamicDispatchTreeStructure(
					aDatabaseNodes, 
					theTotalDispatchers);
			theTotalDispatchers += theDispatchers;
			theDispatchers = (theDispatchers+DISPATCH_BRANCHING_FACTOR-1)/DISPATCH_BRANCHING_FACTOR;
		}
	}
	
	public static void main(String[] args)
	{
		for(int i=1;i<1000;i++)
		{
			System.out.println(compute(i));
		}
	}


}
