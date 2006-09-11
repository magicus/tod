/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid.bench;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import tod.impl.dbgrid.ConditionGenerator;
import tod.impl.dbgrid.EventGenerator;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.bench.BenchBase.BenchResults;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;

public class BenchDatabaseNode
{
	private DatabaseNode itsNode;

	public BenchDatabaseNode()
	{
	}

	public BenchDatabaseNode(DatabaseNode aNode)
	{
		itsNode = aNode;
	}

	@Test public void test() throws RemoteException 
	{
		itsNode = new DatabaseNode(false);
		store();
//		fetchSimple(1000);
//		fetchCompound(100, 8);
	}
	
	public static EventGenerator createGenerator()
	{
		return new EventGenerator(0, 100, 100, 100, 100, 100, 100, 100, 100);
	}
	
	void store()
	{
		System.out.println("Store");
		
		final EventGenerator theGenerator = createGenerator();
		
		BenchResults theResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				Fixtures.fillNode(itsNode, theGenerator, 10*1000*1000);
				itsNode.flush();
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

	private void fetchCompound(int aCount, int aMaxClauses)
	{
		System.out.println("Fetch compound");
		
		ConditionGenerator theConditionGenerator = new ConditionGenerator(0, createGenerator());
		theConditionGenerator.next();
		
		// We store all conditions beforehand so as not to measure generation time.
		final List<EventCondition> theConditions = new ArrayList<EventCondition>(aCount);
		while(theConditions.size() < aCount)
		{
			EventCondition theCondition = theConditionGenerator.next();
			if (theCondition.getClausesCount() < aMaxClauses) theConditions.add(theCondition);
		}
		
		fetch(theConditions, 1);
		fetch(theConditions, 10);
		fetch(theConditions, 100);
		fetch(theConditions, 1000);
	}
	
	private void fetchSimple(int aCount)
	{
		System.out.println("Fetch simple");
		
		ConditionGenerator theConditionGenerator = new ConditionGenerator(0, createGenerator());
		theConditionGenerator.next();
		
		// We store all conditions beforehand so as not to measure generation time.
		final List<EventCondition> theConditions = new ArrayList<EventCondition>(aCount);
		for(int i=0;i<aCount;i++) theConditions.add(theConditionGenerator.nextSimpleCondition());
		
		fetch(theConditions, 1);
		fetch(theConditions, 10);
		fetch(theConditions, 100);
		fetch(theConditions, 1000);
	}
	
	/**
	 * Fetches a certain number of results for each specified queries
	 * @param aConditions Queries
	 * @param aCount Number of results to fetch for each query.
	 */
	private void fetch(final List<EventCondition> aConditions, final int aCount)
	{
		int theTotalClausesCount = 0;
		int theMaxClausesCount = 0;
		for (EventCondition theCondition : aConditions)
		{
			int theClausesCount = theCondition.getClausesCount();
			theTotalClausesCount += theClausesCount;
			theMaxClausesCount = Math.max(theMaxClausesCount, theClausesCount);
		}
		
		System.out.println(String.format(
				"Testing with %d conditions (%d clauses, max %d) and count=%d",
				aConditions.size(),
				theTotalClausesCount,
				theMaxClausesCount,
				aCount));
		
		BenchResults theResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				for (int i=0;i<aConditions.size();i++)
				{
					EventCondition theCondition = aConditions.get(i);
					
					Iterator<GridEvent> theIterator = itsNode.evaluate(theCondition, 0);;
					for (int j=0;j<aCount;j++)
					{
						if (! theIterator.hasNext()) break;
						theIterator.next();
					}
				}
			}
		});
		
		float theQpS = 1000f * aConditions.size() / theResults.totalTime;
		float theEpS = 1000f * aConditions.size() * aCount / theResults.totalTime;
		float theCpS = 1000f * theTotalClausesCount / theResults.totalTime;
		System.out.println(theResults);
		System.out.println("Queries/s: "+theQpS);
		System.out.println("Events/s: "+theEpS);
		System.out.println("Clauses/s: "+theCpS);
		System.out.println("---");
	}
	
	public static void main(String[] args) throws RemoteException
	{
		new BenchDatabaseNode(new DatabaseNode(false)).store();
	}

}
