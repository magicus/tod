/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid;

import static org.junit.Assert.assertTrue;

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
		
		assertTrue(theMBs > 20);
	}
	
	private void fill(long aTupleCount)
	{
		itsIndex = Fixtures.createStdIndex();
		Fixtures.fillStdIndex(itsIndex, new TimestampGenerator(0), aTupleCount);
	}
}
