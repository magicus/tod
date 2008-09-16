/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.dbgrid;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import tod.core.config.TODConfig;
import tod.impl.database.structure.standard.StructureDatabase;
import tod.impl.dbgrid.db.DatabaseNode;

public class DBGridUtils
{
	/**
	 * Standard setup of a grid master that waits for a number
	 * of database nodes to connect
	 */
	public static GridMaster setupMaster(Registry aRegistry, String[] args) throws Exception
	{
		if (args.length == 1)
		{
			// Only the total number of nodes is specified.
			int theTotalNodes = Integer.parseInt(args[0]);
			
			if (theTotalNodes == 0)
			{
				return setupLocalMaster(aRegistry);
			}
			else
			{
				return setupMaster(aRegistry, theTotalNodes);
			}
		}
		else
		{
			throw new IllegalArgumentException("Don't know what to do");
		}
	}
		
	public static GridMaster setupLocalMaster(Registry aRegistry) throws RemoteException
	{
		TODConfig theConfig = new TODConfig();
		StructureDatabase theStructureDatabase = StructureDatabase.create(theConfig);
		DatabaseNode theNode = DebuggerGridConfig.createDatabaseNode();
		
		GridMaster theMaster = GridMaster.createLocal(
				theConfig, 
				theStructureDatabase, 
				theNode, 
				true);
		
		if (aRegistry != null)
		{
			System.out.println("Binding master...");
			aRegistry.rebind(GridMaster.getRMIId(theConfig), theMaster);
			System.out.println("Bound master");
		}

		theMaster.waitReady();
		return theMaster;
	}
	
	/**
	 * Standard setup of a grid master that waits for a number
	 * of database nodes to connect
	 */
	public static GridMaster setupMaster(
			Registry aRegistry,
			int aExpectedNodes) throws Exception
	{
		TODConfig theConfig = new TODConfig();
		StructureDatabase theStructureDatabase = StructureDatabase.create(theConfig);
		System.out.println(theStructureDatabase.getStats());
		
		GridMaster theMaster = GridMaster.create(
				theConfig, 
				theStructureDatabase, 
				aExpectedNodes);
		
		System.out.println("Binding master...");
		aRegistry.rebind(GridMaster.getRMIId(theConfig), theMaster);
		System.out.println("Bound master");
		
		theMaster.waitReady();

		// TODO: When should we create a node?
//		if (aDispatchTreeStructure. == 0) GridImpl.getFactory(aConfig).createNode(true);

		return theMaster;
	}

}
