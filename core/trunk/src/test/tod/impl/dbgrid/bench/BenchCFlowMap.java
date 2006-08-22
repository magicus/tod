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
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.bench.BenchBase.BenchResults;
import tod.impl.dbgrid.dbnode.CFlowMap;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;


public class BenchCFlowMap
{
	@Test public void bench() throws FileNotFoundException
	{
//		bench(10000, 1000000);
		bench(10000, 10000000);
	}
	
	private void bench(final int aKeysCount, final int aChildrenCount) throws FileNotFoundException
	{
		System.out.println("Bench with keys count: "+aKeysCount+", children count: "+aChildrenCount);
		
		DatabaseNode theNode = new DatabaseNode(1);
		HardPagedFile theIndexFile = new HardPagedFile(new File("cflow-index.bin"), DB_INDEX_PAGE_SIZE);
		HardPagedFile theDataFile = new HardPagedFile(new File("cflow-data.bin"), DB_CFLOW_PAGE_SIZE);
		final CFlowMap theMap = new CFlowMap(theNode, theIndexFile, theDataFile);

		BenchResults theResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				// Populate the map
				System.out.println("Fill...");
				Fixtures.FakeThread[] theThreads = new Fixtures.FakeThread[aKeysCount];
				for (int i=0;i<aKeysCount;i++) theThreads[i] = new Fixtures.FakeThread(i%100 + 1, i/100 + 1);
				
				Random theRandom = new Random(0);
				for(int i=0;i<aChildrenCount;i++)
				{
					int theIndex = theRandom.nextInt(theThreads.length);
					Fixtures.FakeThread theThread = theThreads[theIndex];
					
					if (theThread != null)
					{
						boolean theContinue = theThread.addNextToMap(theMap, null);
						if (! theContinue) theThreads[theIndex] = null;
					}
					
					if (i % 100000 == 0) System.out.println(i);
				}
				
			}
		});
		
		System.out.println(theResults);
		float theCpS = 1000f * aChildrenCount / theResults.totalTime;
		System.out.println("Children/s: "+theCpS);
		long theStorage = theIndexFile.getStorageSpace()+theDataFile.getStorageSpace();
		System.out.println("Storage: "+theStorage);
		
	}

}
