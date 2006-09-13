/*
 * Created on Sep 12, 2006
 */
package tod.impl.dbgrid.bench;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import tod.core.ILogCollector;
import tod.core.config.GeneralConfig;
import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.bench.BenchBase.BenchResults;
import zz.utils.Utils;

public class GridQuery
{
	public static void main(String[] args) throws Exception
	{
		String theFileName = GeneralConfig.STORE_EVENTS_FILE;
		File theFile = new File(theFileName);
		
		GridMaster theMaster = Fixtures.setupMaster(args);
		ILogCollector theCollector = theMaster.createCollector(1);
		
		long theEventsCount = Fixtures.replay(theFile, theMaster, theCollector);
		System.out.println("Replayed "+theEventsCount+" events");

		Registry theRegistry = LocateRegistry.getRegistry();
		RIGridMaster theRemoteMaster = (RIGridMaster) theRegistry.lookup(GridMaster.RMI_ID);
		
		GridLogBrowser theBrowser = new GridLogBrowser(theRemoteMaster);
		ILocationsRepository theLocationsRepository = theBrowser.getLocationsRepository();

		List<IHostInfo> theHosts = list(theBrowser.getHosts());
		List<IThreadInfo> theThreads = list(theBrowser.getThreads());
		List<IClassInfo> theClasses = list(theLocationsRepository.getClasses());
		List<IFieldInfo> theFields = list(theLocationsRepository.getFields());
		List<IBehaviorInfo> theBehaviors = list(theLocationsRepository.getBehaviours());
		
		System.out.println("Hosts: "+theHosts);
		System.out.println("Threads: "+theThreads);
		System.out.println("Types: "+theClasses);
		System.out.println("Fields: "+theFields);
		System.out.println("Behaviors: "+theBehaviors);
		
		ICompoundFilter theFilter = theBrowser.createIntersectionFilter(
				theBrowser.createHostFilter(theHosts.get(1)),
				theBrowser.createThreadFilter(theThreads.get(0)),
				theBrowser.createBehaviorCallFilter(theBehaviors.get(4))
				);
		
		System.out.println("Filter: "+theFilter);
		
		final IEventBrowser theEventBrowser = theBrowser.createBrowser(theFilter);
		
		BenchResults theResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				long theCount = 0;
				while (theEventBrowser.hasNext())
				{
					ILogEvent theEvent = theEventBrowser.next();
//					System.out.println(theEvent);
					theCount++;
					if (theCount % 1000 == 0) System.out.println(theCount);
				}
				
				System.out.println("Event count: "+theCount);
			}
		});
		
		System.out.println(theResults);
		System.exit(0);
	}
	
	private static <T> List<T> list(Iterable<T> aIterable)
	{
		List<T> theList = new ArrayList<T>();
		Utils.fillCollection(theList, aIterable);
		return theList;
	}
}
