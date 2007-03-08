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
package tod.impl.dbgrid;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import tod.core.LocationRegisterer;
import tod.core.config.TODConfig;
import tod.impl.dbgrid.dispatch.DatabaseNode;
import tod.impl.dbgrid.dispatch.EventDispatcher;
import tod.impl.dbgrid.dispatch.tree.DispatchTreeStructure.NodeRole;

/**
 * Main class for starting nodes of the dispatching tree.
 * Nodes are created as leaf dispatcher, internal dispatcher or
 * database node according to the task id, or by asking to the
 * master, depending on the command-line arguments.
 * @author gpothier
 */
public class StartNode
{
	/**
	 * Determines the role of this node by asking to the master.
	 */
	public static void askRoleToMaster() throws Exception
	{
		System.out.println("StartNode");
		Registry theRegistry = LocateRegistry.getRegistry(DebuggerGridConfig.MASTER_HOST);
		RIGridMaster theMaster = (RIGridMaster) theRegistry.lookup(GridMaster.RMI_ID);

		String theHostName = InetAddress.getLocalHost().getHostName();
		
		System.out.println("Asking role to master (hostname: "+theHostName+")");
		NodeRole theRole = theMaster.getRoleForNode(theHostName);

		System.out.println("Node role: "+theRole);
		if (theRole == null)
		{
			System.err.println("Master rejected this node with a null role.");
			return;
		}
		
		TODConfig theConfig = theMaster.getConfig();
		
		switch(theRole)
		{
		case DATABASE:
			System.out.println("Starting database node.");
			DatabaseNode theNode = new DatabaseNode();
			theNode.connectToMaster();
			break;
			
		case DISPATCHER:
			System.out.println("Starting internal dispatcher.");
			EventDispatcher theDispatcher = new EventDispatcher();
			theDispatcher.connectToMaster();
			break;
			
		default: throw new RuntimeException("Not handled: "+theRole); 
		}
	}
	
	/**
	 * Determines the role of this node according to the
	 * value of task id.
	 */
	public static void determineRoleFromTaskId(int aTaskId, int aExpectedNodes) throws Exception
	{
//		
//		System.out.println(String.format(
//				"StartNode [%d]: expecting %d database nodes.",
//				aTaskId,
//				aExpectedNodes));
//		
//		DispatchTreeStructure theStructure = DispatchTreeStructure.compute(aExpectedNodes);
//		System.out.println(theStructure);
//
//		TODConfig theConfig = new TODConfig();
//		
//		aTaskId -= 2; //first node has task id == 2
//		if (aTaskId < theStructure.databaseNodes)
//		{
//			System.out.println("Starting database node.");
//			GridImpl.getFactory(theConfig).createNode(true);
//			return;
//		}
//		
//		aTaskId -= theStructure.databaseNodes;
//		
//		if (aTaskId < theStructure.leafNodes)
//		{
//			System.out.println("Starting leaf dispatcher.");
//			GridImpl.getFactory(theConfig).createLeafDispatcher(true, new LocationRegisterer());
//			return;
//		}
//		
//		aTaskId -= theStructure.leafNodes;
//
//		if (aTaskId < theStructure.internalNodes)
//		{
//			System.out.println("Starting internal dispatcher.");
//			InternalEventDispatcher theDispatcher = new InternalEventDispatcher();
//			theDispatcher.connectToMaster();
//			return;
//		}

		throw new UnsupportedOperationException("Don't know what to do.");
		
	}
	
	public static void main(String[] args) throws Exception
	{
		if (args.length == 0) askRoleToMaster();
		else
		{
			String theMasterHost = args[0];
			int theExpectedNodes = Integer.parseInt(args[1]);
			int theTaskId = Integer.parseInt(System.getProperty("task-id"));
	
			determineRoleFromTaskId(theTaskId, theExpectedNodes);
		}
	}
}
