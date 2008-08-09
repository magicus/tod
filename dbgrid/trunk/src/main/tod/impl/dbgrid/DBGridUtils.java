/*
 * Created on Nov 7, 2007
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
