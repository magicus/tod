/*
 * Created on Aug 10, 2006
 */
package tod.impl.dbgrid.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_CFLOW_PAGE_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_INDEX_PAGE_SIZE;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.dbnode.CFlowMap;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import zz.utils.ListMap;
import zz.utils.Utils;

public class TestCFlowMap
{
	@Test public void test() throws FileNotFoundException, RemoteException
	{
		test(100, 10000);
		test(1000, 1000000);
		test(10000, 10000000);
	}
	
	private void test(int aKeysCount, int aChildrenCount) throws FileNotFoundException, RemoteException
	{
		DatabaseNode theNode = new DatabaseNode(1);
		HardPagedFile theIndexFile = new HardPagedFile(new File("cflow-index.bin"), DB_INDEX_PAGE_SIZE);
		HardPagedFile theDataFile = new HardPagedFile(new File("cflow-data.bin"), DB_CFLOW_PAGE_SIZE);
		CFlowMap theMap = new CFlowMap(theNode, theIndexFile, theDataFile);

		ListMap<byte[], byte[]> theMemMap = new ListMap<byte[], byte[]>();
		
		// Fill map
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
				boolean theContinue = theThread.addNextToMap(theMap, theMemMap);
				if (! theContinue) theThreads[theIndex] = null;
			}
			
			if (i % 100000 == 0) System.out.println(i);
		}
		
		//  Check
		System.out.println("Parents count: "+theMemMap.size());
		System.out.println("Check...");
		
		int i = 0;
		for(Map.Entry<byte[], List<byte[]>> theEntry : theMemMap.entrySet())
		{
			byte[] theParentPointer = theEntry.getKey();
			Iterator<byte[]> theChildrenPointers = theMap.getChildrenPointers(theParentPointer);
			Iterator<byte[]> theRefPointers = theEntry.getValue().iterator();
			
			while (theRefPointers.hasNext())
			{
				byte[] theRefChild = theRefPointers.next();
				assertTrue("Count mismatch", theChildrenPointers.hasNext());
				byte[] theChild = theChildrenPointers.next();
				assertTrue("Data mismatch", Utils.compare(theRefChild, theChild) == 0);
			}
			
			assertFalse("Count mismatch", theChildrenPointers.hasNext());
			if (i % 100000 == 0) System.out.println(i);
			i++;
			
		}
		
		System.out.println("Done.");
		
	}
}
