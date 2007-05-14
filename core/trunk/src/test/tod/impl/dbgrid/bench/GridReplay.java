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
package tod.impl.dbgrid.bench;

import java.io.File;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import tod.Util;
import tod.core.ILogCollector;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.GridMaster;
import tod.utils.StoreTODServer;
import tod.utils.TODUtils;

/**
 * Reads events stored on the disk by a {@link StoreTODServer}
 * and sends it to a grid master.
 * @author gpothier
 */
public class GridReplay
{
	public static void main(String[] args) throws Exception
	{
		replay(args);
//		System.exit(0);
	}
	
	public static GridMaster replay(String[] args) throws Exception
	{
		Registry theRegistry = LocateRegistry.createRegistry(Util.TOD_REGISTRY_PORT);
		
		String theFileName = DebuggerGridConfig.STORE_EVENTS_FILE;
		File theFile = new File(theFileName);
		
		GridMaster theMaster = TODUtils.setupMaster(theRegistry, args);
		
		long t0 = System.currentTimeMillis();
		long theCount = Fixtures.replay(theFile, theMaster);
		long t1 = System.currentTimeMillis();
		float dt = (t1-t0)/1000f;
		float theEpS = theCount/dt;
		System.out.println("Events: "+theCount+" time: "+dt+"s rate: "+theEpS+"ev/s");
		
		return theMaster;
	}

}
