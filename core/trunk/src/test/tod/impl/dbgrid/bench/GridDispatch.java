/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.impl.dbgrid.bench;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import tod.impl.dbgrid.EventGenerator;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.bench.BenchBase.BenchResults;
import tod.impl.dbgrid.dispatcher.EventDispatcher;
import tod.impl.dbgrid.messages.GridEvent;

public class GridDispatch
{
	public static void main(String[] args) throws Exception
	{
		Registry theRegistry = LocateRegistry.createRegistry(1099);
		
		int theExpectedNodes;
		int theEventsCount;
		theExpectedNodes = Integer.parseInt(args[0]);
		theEventsCount = Integer.parseInt(args[1]);
		
		final GridMaster theMaster = Fixtures.setupMaster(theRegistry, theExpectedNodes);
		final EventDispatcher theDispatcher = theMaster.getDispatcher();
		final EventGenerator theGenerator = BenchEventDatabase.createGenerator();

		final int n = theEventsCount;
		
		BenchResults theGenResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				for (int i=0;i<n;i++) theGenerator.next();
			}
		});
		
		System.out.println("Gen: "+theGenResults);
		
		BenchResults theDispatchResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				GridEvent theEvent = theGenerator.next();
				
				for (int i=0;i<n;i++)
				{
					theDispatcher.dispatchEvent(theGenerator.next());
				}
			}
		});
		
		System.out.println("Dispatch: "+theDispatchResults);
		
		float dt = (theDispatchResults.totalTime-theGenResults.totalTime)/1000f;
		System.out.println("DeltaT: "+dt);
		
		float theEpS = n/dt;
		System.out.println("events/s: "+theEpS);
		System.exit(0);
	}
}
