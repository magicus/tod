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
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.ConditionGenerator;
import tod.impl.dbgrid.EventGenerator;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.bench.BenchBase.BenchResults;
import tod.impl.dbgrid.gridimpl.uniform.UniformEventDatabase;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;

public class BenchEventDatabase
{
	private UniformEventDatabase itsDatabase;

	public BenchEventDatabase()
	{
	}

	public BenchEventDatabase(UniformEventDatabase aDatabase)
	{
		itsDatabase = aDatabase;
	}

	@Test public void test()  
	{
		itsDatabase = new UniformEventDatabase(0, new File("test.bin"));
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
				Fixtures.fillDatabase(itsDatabase, theGenerator, 10*1000*1000);
				itsDatabase.flush();
			}
		});
		
		System.out.println(theResults);
		
		long theEventCount = itsDatabase.getEventsCount();
		long theStorage = itsDatabase.getStorageSpace();
		
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
					
					BidiIterator<GridEvent> theIterator = itsDatabase.evaluate(theCondition, 0);;
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
	
	public static void main(String[] args) 
	{
		new BenchEventDatabase(new UniformEventDatabase(0, new File("test.bin"))).store();
	}

}
