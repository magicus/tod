/*
 * Created on Aug 9, 2006
 */
package tod.impl.dbgrid;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import static tod.impl.dbgrid.DebuggerGridConfig.*;

import tod.impl.dbgrid.btree.BTree;
import tod.impl.dbgrid.dbnode.PagedFile;

public class TestBTree
{
	private static final int n = 20000;
	
	@Test public void testPut() throws FileNotFoundException
	{
		PagedFile theFile = new PagedFile(new File("btree.bin"), DB_BTREE_PAGE_SIZE);
		BTree theTree = new BTree(theFile);

		Random theRandom = new Random(0);
		for (int i=0;i<n;i++)
		{
			byte[] theKey = new byte[(EVENTID_POINTER_SIZE+7) / 8];
			theRandom.nextBytes(theKey);
			theKey[2] |= 8;
			long theValue = theRandom.nextInt() & 0xffffffffL;
			
			theTree.put(theKey, theValue);
		}
		
		theRandom = new Random(0);
		for (int i=0;i<n;i++)
		{
			byte[] theKey = new byte[(EVENTID_POINTER_SIZE+7) / 8];
			theRandom.nextBytes(theKey);
			theKey[2] |= 8;
			long theExpectedValue = theRandom.nextInt() & 0xffffffffL;
			
			Long theValue = theTree.get(theKey);
			assertTrue(theValue != null);
			assertTrue(theValue.longValue() == theExpectedValue);
		}
	}
	

}
