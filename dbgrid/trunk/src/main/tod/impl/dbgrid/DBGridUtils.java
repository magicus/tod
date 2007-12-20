/*
 * Created on Nov 7, 2007
 */
package tod.impl.dbgrid;

import java.io.FileReader;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import tod.agent.DebugFlags;
import tod.core.config.TODConfig;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.database.structure.standard.StructureDatabase;
import tod.impl.dbgrid.dispatch.DatabaseNode;
import tod.impl.dbgrid.dispatch.tree.DispatchTreeStructure;
import tod.impl.dbgrid.dispatch.tree.DynamicDispatchTreeStructure;
import tod.impl.dbgrid.dispatch.tree.FixedDispatchTreeStructure;

public class DBGridUtils
{
	/**
	 * Standard setup of a grid master that waits for a number
	 * of database nodes to connect
	 */
	public static GridMaster setupMaster(Registry aRegistry, String[] args) throws Exception
	{
		DispatchTreeStructure theDispatchTreeStructure;
		
		if (args.length == 3)
		{
			// The first arg is the total number of nodes, used by the scripts
			int theTotal = Integer.parseInt(args[0]);
			int theExpectedNodes = Integer.parseInt(args[1]);
			int theExpectedInternalDispatchers = Integer.parseInt(args[2]);

			if (theTotal != theExpectedNodes + theExpectedInternalDispatchers)
			{
				throw new IllegalArgumentException();
			}
			
			theDispatchTreeStructure = new DynamicDispatchTreeStructure(
					theExpectedNodes, 
					theExpectedInternalDispatchers);
		}
		else if (args.length == 1)
		{
			// Only the total number of nodes is specified.
			int theTotalNodes = Integer.parseInt(args[0]);
			
			if (DebugFlags.DISPATCH_FAKE_1)
			{
				theDispatchTreeStructure = new DynamicDispatchTreeStructure(1, 0);
			}
			else if (theTotalNodes == 0)
			{
				return setupLocalMaster(aRegistry);
			}
			else
			{
				theDispatchTreeStructure = DynamicDispatchTreeStructure.computeFromTotal(theTotalNodes);
			}
		}
		else if (args.length == 2)
		{
			// Args: total number of nodes (for script), xml file name
			String theFileName = args[1];
			FileReader theReader = new FileReader(theFileName);
			theDispatchTreeStructure = new FixedDispatchTreeStructure(theReader);
		}
		else
		{
			throw new IllegalArgumentException("Don't know what to do");
		}

		return setupMaster(aRegistry, theDispatchTreeStructure); 
	}
		
	public static GridMaster setupLocalMaster(Registry aRegistry) throws RemoteException
	{
		TODConfig theConfig = new TODConfig();
		IMutableStructureDatabase theStructureDatabase = StructureDatabase.create(theConfig);
		ASMDebuggerConfig theDebuggerConfig = new ASMDebuggerConfig(theConfig);

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theStructureDatabase, theDebuggerConfig);
		DatabaseNode theNode = DatabaseNode.createLocalNode();
		GridMaster theMaster = GridMaster.createLocal(
				theConfig, 
				theStructureDatabase, 
				theInstrumenter, 
				theNode, 
				true);
		
		if (aRegistry != null)
		{
			System.out.println("Binding master...");
			aRegistry.rebind(GridMaster.RMI_ID, theMaster);
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
			DispatchTreeStructure aDispatchTreeStructure) throws Exception
	{
		return setupMaster(new TODConfig(), aRegistry, aDispatchTreeStructure); 
	}
	
	public static GridMaster setupMaster(
			TODConfig aConfig,
			Registry aRegistry,
			DispatchTreeStructure aDispatchTreeStructure) throws Exception
	{
		System.out.println("Dispatch structure: " + aDispatchTreeStructure);
		
		IMutableStructureDatabase theStructureDatabase = StructureDatabase.create(aConfig);
		ASMDebuggerConfig theDebuggerConfig = new ASMDebuggerConfig(aConfig);

		System.out.println(theStructureDatabase.getStats());
		
		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theStructureDatabase, theDebuggerConfig);

		GridMaster theMaster = GridMaster.create(
				aConfig, 
				theStructureDatabase, 
				theInstrumenter,
				aDispatchTreeStructure);
		
		System.out.println("Binding master...");
		aRegistry.rebind(GridMaster.RMI_ID, theMaster);
		System.out.println("Bound master");
		
		theMaster.waitReady();

		// TODO: When should we create a node?
//		if (aDispatchTreeStructure. == 0) GridImpl.getFactory(aConfig).createNode(true);

		return theMaster;
	}

}
