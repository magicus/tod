/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid;

import java.io.FileNotFoundException;

import org.junit.Test;

import tod.impl.dbgrid.BenchBase.BenchResults;
import tod.impl.dbgrid.TestHierarchicalIndex.TimestampGenerator;
import tod.impl.dbgrid.dbnode.HierarchicalIndex;
import tod.impl.dbgrid.dbnode.StdIndexSet.Tuple;

public class BenchIndexes
{
	private HierarchicalIndex<Tuple> itsIndex;
	
	@Test
	public void hierarchicalWriteBench()
	{
		// Warm-up
		fill(10000000);

		BenchResults theResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				fill(100000000);
			}
		});
		
		System.out.println(theResults);
		
		long theTupleCount = itsIndex.getLeafTupleCount();
		long theStorage = 1L * itsIndex.getPageSize() * itsIndex.getTotalPageCount();
		
		float theMBs = 1.0f * (theStorage / (1024*1024)) / (theResults.totalTime / 1000);
		
		System.out.println("Tuple count: "+theTupleCount);
		System.out.println("Storage space: "+theStorage);
		System.out.println("MB/s: "+theMBs);
	}
	
	private void fill(long aTupleCount)
	{
		try
		{
			itsIndex = FixtureHierarchicalIndex.createStdIndex();
			FixtureHierarchicalIndex.fillIndex(itsIndex, new TimestampGenerator(0), aTupleCount);
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}		
	}
}
