/*
 * Created on Aug 20, 2006
 */
package tod.impl.dbgrid.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import tod.impl.dbgrid.dbnode.file.ExponentialPageBank;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.dbnode.file.SoftPagedFile;
import tod.impl.dbgrid.dbnode.file.SoftPagedFile.SoftPage;
import tod.impl.dbgrid.dbnode.file.SoftPagedFile.SoftPageBitStruct;

public class TestSoftPagedFile
{
	@Test public void testRaw() throws FileNotFoundException
	{
		testRaw(4096, 128, 100);
		testRaw(16384, 16, 100);
	}
	
	private void testRaw(int aMaxSize, int aMinSize, int aPagesCount) throws FileNotFoundException
	{
		File theFile = new File("softFile.bin");
		HardPagedFile theHardFile = new HardPagedFile(theFile, aMaxSize);
		SoftPagedFile theSoftFile = new SoftPagedFile(theHardFile, aMinSize);
		
		List<Long> thePageIds = new ArrayList<Long>();
		
		while (aMinSize <= aMaxSize)
		{
			for(int i=0;i<aPagesCount;i++) 
			{
				SoftPage thePage = theSoftFile.create(aMinSize);
				long thePageId = thePage.getPageId();
				thePageIds.add(thePageId);
				
				SoftPageBitStruct theStruct = thePage.asBitStruct();
				int theTotalBits = theStruct.getTotalBits();
				assertTrue("Bad struct size", theTotalBits == aMinSize*8);
				
				theStruct.writeInt(aMinSize, 32);
				while(theStruct.getRemainingBits() >= 64)
				{
					theStruct.writeLong(thePageId, 64);
				}
			}
			
			aMinSize *= 2;
		}
		
		for (Long thePageId : thePageIds)
		{
			SoftPage thePage = theSoftFile.get(thePageId);
			
			SoftPageBitStruct theStruct = thePage.asBitStruct();
			int theTotalBits = theStruct.getTotalBits();
			
			int theSize = theStruct.readInt(32);
			assertTrue("Bad struct size", theTotalBits == theSize*8);
			
			while(theStruct.getRemainingBits() >= 64)
			{
				long theValue = theStruct.readLong(64);
				assertTrue("Bad value", theValue == thePageId.longValue());
			}
		}
	}
	
	@Test public void testExponential() throws FileNotFoundException
	{
		testExponential(4096, 128, 100000);
	}
	
	private void testExponential(int aMaxSize, int aMinSize, int aTupleCount) throws FileNotFoundException
	{
		File theFile = new File("softFile.bin");
		HardPagedFile theHardFile = new HardPagedFile(theFile, aMaxSize);
		SoftPagedFile theSoftFile = new SoftPagedFile(theHardFile, aMinSize);
		
		for(int i=0;i<aTupleCount;i++)
		{
			
		}
	}
	
}
