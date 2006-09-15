/*
 * Created on Sep 1, 2006
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
		final EventGenerator theGenerator = BenchDatabaseNode.createGenerator();

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
