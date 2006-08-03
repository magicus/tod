/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid;

import org.junit.Test;

import tod.impl.dbgrid.BenchBase.BenchResults;
import tod.impl.dbgrid.dbnode.DatabaseNode;

public class BenchDatabaseNode
{
	private DatabaseNode itsNode;

	@Test public void store()
	{
		itsNode = new DatabaseNode();
		final EventGenerator theGenerator = new EventGenerator(0);
		
		BenchResults theResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				Fixtures.fillNode(itsNode, theGenerator, 10000000);
			}
		});
		
		System.out.println(theResults);
		
		long theEventCount = itsNode.getEventsCount();
		long theStorage = itsNode.getStorageSpace();
		
		float theMBs = (1.0f * theStorage / (1024*1024)) / (theResults.totalTime / 1000.0f);
		float theEvents = 1.0f * theEventCount / (theResults.totalTime / 1000.0f);
		
		System.out.println("Events count: "+theEventCount);
		System.out.println("Storage space: "+theStorage);
		System.out.println("MB/s: "+theMBs);
		System.out.println("event/s: "+theEvents);
	}
	
}
