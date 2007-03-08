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
package tod.utils;

import java.io.FileReader;
import java.rmi.registry.Registry;

import tod.agent.DebugFlags;
import tod.core.LocationRegisterer;
import tod.core.config.TODConfig;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IBehaviorInfo;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.dispatch.tree.DispatchTreeStructure;
import tod.impl.dbgrid.dispatch.tree.DynamicDispatchTreeStructure;
import tod.impl.dbgrid.dispatch.tree.FixedDispatchTreeStructure;

public class TODUtils
{
	/**
	 * Returns a filter that accepts only events that occurred at the given location
	 */
	public static IEventFilter getLocationFilter(
			ILogBrowser aLogBrowser,
			IBehaviorInfo aBehavior, 
			int aLine)
	{
		int[] theLocations = aBehavior.getBytecodeLocations(aLine);
		if (theLocations != null && theLocations.length>0)
		{
			IEventFilter[] theLocationFilters = new IEventFilter[theLocations.length];
			for(int i=0;i<theLocationFilters.length;i++)
			{
				theLocationFilters[i] = 
					aLogBrowser.createLocationFilter(aBehavior, theLocations[i]);
			}
			return aLogBrowser.createUnionFilter(theLocationFilters);
		}
		else return null;
	}

	/**
	 * Standard setup of a grid master that waits for a number
	 * of database nodes to connect
	 */
	public static GridMaster setupMaster(Registry aRegistry, String[] args) throws Exception
	{
		DispatchTreeStructure theDispatchTreeStructure;
		
		if (args.length == 4)
		{
			// The first arg is the total number of nodes, used by the scripts
			int theTotal = Integer.parseInt(args[0]);
			int theExpectedNodes = Integer.parseInt(args[1]);
			int theExpectedLeafDispatchers = Integer.parseInt(args[2]);
			int theExpectedInternalDispatchers = Integer.parseInt(args[3]);

			if (theTotal != theExpectedNodes + theExpectedLeafDispatchers + theExpectedInternalDispatchers)
			{
				throw new IllegalArgumentException();
			}
			
			theDispatchTreeStructure = new DynamicDispatchTreeStructure(theExpectedNodes, theExpectedLeafDispatchers, theExpectedInternalDispatchers);
		}
		else if (args.length == 1)
		{
			// Only the total number of nodes is specified.
			int theTotalNodes = Integer.parseInt(args[0]);
			
			if (DebugFlags.DISPATCH_FAKE_1)
			{
				theDispatchTreeStructure = new DynamicDispatchTreeStructure(1, 1, 0);
			}
			else if (theTotalNodes == 0)
			{
				theDispatchTreeStructure = new DynamicDispatchTreeStructure(0, 0, 0);
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
		
		LocationRegisterer theRegistrer = new LocationRegisterer();
		
		ASMDebuggerConfig theDebuggerConfig = new ASMDebuggerConfig(
				aConfig,
				theRegistrer);

		System.out.println(theRegistrer.getStats());
		
		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theDebuggerConfig);

		GridMaster theMaster = new GridMaster(
				aConfig, 
				theRegistrer, 
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
