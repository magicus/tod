/*
 * Created on Aug 9, 2006
 */
package tod.impl.dbgrid.bench;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_BTREE_PAGE_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.EVENTID_POINTER_SIZE;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

import org.junit.Test;

import tod.impl.dbgrid.bench.BenchBase.BenchResults;
import tod.impl.dbgrid.dbnode.btree.BTree;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;

public class BenchBTree
{
	private static final int n = 100000;
	
	@Test public void bench() throws FileNotFoundException
	{
		HardPagedFile theFile = new HardPagedFile(new File("btree.bin"), DB_BTREE_PAGE_SIZE);
		final BTree theTree = new BTree(theFile);

		BenchResults theResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				Random theRandom = new Random(0);
				for (int i=0;i<n;i++)
				{
					byte[] theKey = new byte[(EVENTID_POINTER_SIZE+7) / 8];
					theRandom.nextBytes(theKey);
					theKey[2] |= 8;
					long theValue = theRandom.nextInt() & 0xffffffffL;
					
					theTree.put(theKey, theValue);
				}
			}
		});
		
		float thePpS = 1000f * n / theResults.totalTime;
		
		System.out.println(theResults);
		System.out.println("Put/s: "+thePpS);
	}
	

}
