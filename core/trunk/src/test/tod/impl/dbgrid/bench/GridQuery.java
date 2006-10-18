/*
 * Created on Sep 12, 2006
 */
package tod.impl.dbgrid.bench;

import java.io.File;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import tod.core.ILogCollector;
import tod.core.config.GeneralConfig;
import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ObjectId;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.bench.BenchBase.BenchResults;
import tod.impl.dbgrid.dbnode.EventsCounter;
import zz.utils.Utils;

public class GridQuery
{
	public static void main(String[] args) throws Exception
	{
		Registry theRegistry = LocateRegistry.createRegistry(1099);
		
		String theFileName = GeneralConfig.STORE_EVENTS_FILE;
		final File theFile = new File(theFileName);
		
		final GridMaster theMaster = Fixtures.setupMaster(theRegistry, args);
		final ILogCollector theCollector = theMaster.createCollector(1);
		
		final long[] theEventsCount = new long[1];
		BenchResults theReplayTime = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				try
				{
					theEventsCount[0] = Fixtures.replay(theFile, theMaster, theCollector);
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		});
		System.out.println(theReplayTime);
		float theEpS = 1000f*theEventsCount[0]/theReplayTime.totalTime;
		System.out.println("Replayed "+theEventsCount[0]+" events: "+theEpS+"ev/s");

		System.out.println("Looking up master in registry");
		RIGridMaster theRemoteMaster = (RIGridMaster) theRegistry.lookup(GridMaster.RMI_ID);
		
		final GridLogBrowser theBrowser = new GridLogBrowser(theRemoteMaster);
		
		long theFirstTimestamp = theBrowser.getFirstTimestamp();
		long theLastTimestamp = theBrowser.getLastTimestamp();

		System.out.println("\nPerforming count benchmarks --- pass #1\n");
		
		int theSlots = 1000;
		
		EventsCounter.FORCE_MERGE_COUNTS = true;
		benchCounts(theBrowser, theFirstTimestamp, theLastTimestamp, theSlots);
		
		EventsCounter.FORCE_MERGE_COUNTS = false;
		benchCounts(theBrowser, theFirstTimestamp, theLastTimestamp, theSlots);
		
		System.out.println("\nPerforming count benchmarks --- pass #2\n");
		
		EventsCounter.FORCE_MERGE_COUNTS = true;
		long[] theMergeCounts = benchCounts(theBrowser, theFirstTimestamp, theLastTimestamp, theSlots);
		
		EventsCounter.FORCE_MERGE_COUNTS = false;
		long[] theFastCounts = benchCounts(theBrowser, theFirstTimestamp, theLastTimestamp, theSlots);
		
		printDistortion(theMergeCounts, theFastCounts);

		
		benchCursors(theBrowser, 1, 1000);
		benchCursors(theBrowser, 1000, 10);
		
		System.out.println("Bench look up valid fields");
		BenchResults theQueryTime = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				createValidFields(theBrowser, 1000);
			}
		});
		
		System.out.println(theQueryTime);

		
		System.exit(0);
	}
	
	private static void printDistortion(long[] c1, long[] c2)
	{
		assert c1.length == c2.length;
		
		long theAbsSum = 0;
		long t1 = 0;
		long t2 = 0;
		float theAvgSum = 0;
		
		for (int i=0;i<c1.length;i++)
		{
			long theAbs = Math.abs(c1[i]-c2[i]);
			theAbsSum += theAbs;
			t1 += c1[i];
			t2 += c2[i];
			
			long theRef = Math.min(c1[i], c2[i]);
			float theAvg = theRef != 0 ? 
					1f * theAbs / theRef 
					: (theAbs != 0 ? 1 : 0);
			theAvgSum += theAvg;
		}
		
		System.out.println(String.format(
				"Distortion - abs. diff: %d, t1: %d, t2: %d, %%: %.2f, avg.: %f",
				theAbsSum,
				t1,
				t2,
				100f*theAbsSum/Math.min(t1, t2),
				theAvgSum/c1.length));
	}
	
	private static <T> List<T> list(Iterable<T> aIterable)
	{
		List<T> theList = new ArrayList<T>();
		Utils.fillCollection(theList, aIterable);
		return theList;
	}
	
	private static long[] benchCounts(final ILogBrowser aBrowser, final long aT1, final long aT2, final int aSlots)
	{
		System.out.println("Count benchmarks (force merge: "+EventsCounter.FORCE_MERGE_COUNTS+")");
//		System.out.println("t1: "+AgentUtils.formatTimestamp(aT1));
//		System.out.println("t2: "+AgentUtils.formatTimestamp(aT2));
//		System.out.println("Slots: "+aSlots);
		
		System.out.println("");
		
		final long[] theCounts = new long[aSlots];
		
		BenchResults theQueryTime = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				for (IThreadInfo theThread: aBrowser.getThreads())
				{
					System.out.println("Retrieving counts for thread "+theThread.getId());
					
					IEventFilter theFilter = aBrowser.createThreadFilter(theThread);
					IEventBrowser theEventBrowser = aBrowser.createBrowser(theFilter);
					
					long[] theThreadCounts = theEventBrowser.getEventCounts(
							aT1, 
							aT2, 
							aSlots);
					
					long theCount = 0;
					for (int i = 0; i < theThreadCounts.length; i++)
					{
						long l = theThreadCounts[i];
						theCount += l;
						theCounts[i] += l;
					}
	
					System.out.println("  Event count: "+theCount);
				}
			}
		});
		
		System.out.println(theQueryTime);
		
		return theCounts;
	}
	
	private static void benchCursors(
			final ILogBrowser aBrowser, 
			final int aBulk, 
			final int aCount)
	{
		System.out.println("Benchmark "+aBulk+"-cursors: "+aCount);
		
//		final Map<ObjectId, IFieldInfo> theFields = createValidFields(aBrowser, aCount);
		final List<IThreadInfo> theThreads = list(aBrowser.getThreads());
		
		final long theFirstTimestamp = aBrowser.getFirstTimestamp();
		final long theLastTimestamp = aBrowser.getLastTimestamp();
		final long theTimeSpan = theLastTimestamp-theFirstTimestamp;
		
		BenchResults theQueryTime = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				Random theRandom = new Random(0);
				for (int i=0;i<aCount;i++)
				{
					IThreadInfo theThread = theThreads.get(theRandom.nextInt(theThreads.size()));
					IEventFilter theFilter = aBrowser.createIntersectionFilter(
							aBrowser.createThreadFilter(theThread),
							aBrowser.createDepthFilter(3+theRandom.nextInt(20)));
					
					long theTimestamp = theRandom.nextLong();
					theTimestamp = theFirstTimestamp + theTimestamp % theTimeSpan; 
					
					benchCursor(aBrowser, theFilter, theTimestamp, aBulk);
				}
//				
//				for(Map.Entry<ObjectId, IFieldInfo> theEntry : theFields.entrySet())
//				{
//					benchCursor(aBrowser, theEntry.getKey(), null, aBulk);
//				}
			}
		});
		
		System.out.println(theQueryTime);
		float theQpS = 1000f * aCount / theQueryTime.totalTime;
		System.out.println("Queries/s: "+theQpS);
		
	}
	
	private static Map<ObjectId, IFieldInfo> createValidFields(ILogBrowser aBrowser, int aCount)
	{
		Map<ObjectId, IFieldInfo> theMap = new HashMap<ObjectId, IFieldInfo>();
		
		Random theRandom = new Random(0);
		while(theMap.size() < aCount)
		{
			ObjectId theId = new ObjectId.ObjectUID(theRandom.nextInt(10000));
			IFieldInfo theField = getValidField(aBrowser, theId);
			if (theField == null) continue;
			
			theMap.put(theId, theField);
		}
		
		return theMap;
	}
	
	/**
	 * Returns a field id that corresponds to a field of the given object
	 */
	private static IFieldInfo getValidField(ILogBrowser aBrowser, ObjectId aObjectId)
	{
		IEventFilter theFilter = aBrowser.createIntersectionFilter(
				aBrowser.createTargetFilter(aObjectId),
				aBrowser.createFieldWriteFilter());
		
		IEventBrowser theBrowser = aBrowser.createBrowser(theFilter);
		
		if (theBrowser.hasNext())
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) theBrowser.next();
			return theEvent.getField();
		}
		else return null;
	}
	
	private static void benchCursor(
			ILogBrowser aBrowser, 
			long aTimestamp,
			ObjectId aObjectId, 
			IFieldInfo aField, 
			int aCount)
	{
		ICompoundFilter theFilter = aBrowser.createIntersectionFilter(
				aBrowser.createTargetFilter(aObjectId),
				aField != null ? 
						aBrowser.createFieldFilter(aField)
						: aBrowser.createFieldWriteFilter());
		
		benchCursor(aBrowser, theFilter, aTimestamp, aCount);
	}
	
	private static void benchCursor(
			ILogBrowser aBrowser,
			long aTimestamp,
			ObjectId aObjectId, 
			IBehaviorInfo aBehavior, 
			int aCount)
	{
		ICompoundFilter theFilter = aBrowser.createIntersectionFilter(
				aBrowser.createTargetFilter(aObjectId),
				aBehavior != null ? 
						aBrowser.createBehaviorCallFilter(aBehavior)
						: aBrowser.createBehaviorCallFilter());
		
		benchCursor(aBrowser, theFilter, aTimestamp, aCount);
	}
	
	private static void benchCursor(
			ILogBrowser aBrowser,
			IEventFilter aFilter,
			long aTimestamp,
			int aCount)
	{
		IEventBrowser theBrowser = aBrowser.createBrowser(aFilter);
		theBrowser.setNextTimestamp(aTimestamp);
		
		int i = 0;
		while (theBrowser.hasNext() && i < aCount)
		{
			theBrowser.next();
			i++;
		}
		
//		if (i < aCount) System.out.println(i);
	}
}
