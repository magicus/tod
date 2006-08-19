/*
 * Created on Aug 17, 2006
 */
package tod.impl.dbgrid.bench;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_CFLOW_PAGE_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_INDEX_PAGE_SIZE;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

import org.junit.Test;

import tod.impl.dbgrid.EventGenerator;
import tod.impl.dbgrid.bench.BenchBase.BenchResults;
import tod.impl.dbgrid.dbnode.CFlowMap;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.dbnode.PagedFile;


public class BenchCFlowMap
{
	@Test public void bench() throws FileNotFoundException
	{
		bench(10000, 1000000);
	}
	
	private void bench(final int aParentsCount, final int aChildrenCount) throws FileNotFoundException
	{
		System.out.println("Bench with parents count: "+aParentsCount+", children count: "+aChildrenCount);
		
		DatabaseNode theNode = new DatabaseNode(0);
		PagedFile theIndexFile = new PagedFile(new File("cflow-index.bin"), DB_INDEX_PAGE_SIZE);
		PagedFile theDataFile = new PagedFile(new File("cflow-data.bin"), DB_CFLOW_PAGE_SIZE);
		final CFlowMap theMap = new CFlowMap(theNode, theIndexFile, theDataFile);

		final EventGenerator theGenerator = new EventGenerator(0);
		final byte[][] theParents = new byte[aParentsCount][];
		
		// Create a list of parents
		for(int i=0;i<aParentsCount;i++) 
		{
			theParents[i] = theGenerator.genExternalPointer();
		}
		
		BenchResults theResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				// Populate the map
				System.out.println("Fill...");
				Random theRandom = new Random(-120);
				for (int i=0;i<aChildrenCount;i++)
				{
					int theIndex = theRandom.nextInt(aParentsCount);
					
					byte[] thePointer = theParents[theIndex];
					theMap.add(thePointer, theGenerator.genExternalPointer());
				}
			}
		});
		
		System.out.println(theResults);
		float theCpS = 1000f * aChildrenCount / theResults.totalTime;
		System.out.println("Children/s: "+theCpS);
		
	}

}
