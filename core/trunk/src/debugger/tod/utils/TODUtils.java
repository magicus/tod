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

import java.io.File;
import java.rmi.registry.Registry;

import tod.core.LocationRegisterer;
import tod.core.config.TODConfig;
import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IBehaviorInfo;
import tod.gui.seed.FilterSeed;
import tod.gui.seed.LogViewSeed;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.bci.asm.ASMLocationPool;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.gridimpl.GridImpl;

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
		int theExpectedNodes = 0;
		if (args.length > 0)
		{
			theExpectedNodes = Integer.parseInt(args[0]);
		}
		
		return setupMaster(aRegistry, theExpectedNodes);
	}
		
	/**
	 * Standard setup of a grid master that waits for a number
	 * of database nodes to connect
	 */
	public static GridMaster setupMaster(Registry aRegistry, int aExpectedNodes) throws Exception
	{
		return setupMaster(new TODConfig(), aRegistry, aExpectedNodes);
	}
	
	public static GridMaster setupMaster(
			TODConfig aConfig,
			Registry aRegistry,
			int aExpectedNodes) throws Exception
	{
		System.out.println("Expecting "+aExpectedNodes+" nodes");
		
		LocationRegisterer theRegistrer = new LocationRegisterer();
		
		ASMDebuggerConfig theDebuggerConfig = new ASMDebuggerConfig(
				aConfig,
				theRegistrer);

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theDebuggerConfig);

		GridMaster theMaster = new GridMaster(
				aConfig, 
				theRegistrer, 
				theInstrumenter,
				aExpectedNodes);
		
		System.out.println("Binding master...");
		aRegistry.rebind(GridMaster.RMI_ID, theMaster);
		System.out.println("Bound master");
		
		theMaster.waitReady();

		if (aExpectedNodes == 0) GridImpl.getFactory(aConfig).createNode(true);

		return theMaster;
	}
	

}
