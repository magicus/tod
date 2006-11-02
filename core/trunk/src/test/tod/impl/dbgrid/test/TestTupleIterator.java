/*
 * Created on Nov 2, 2006
 */
package tod.impl.dbgrid.test;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;
import static org.junit.Assert.*;


import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTupleCodec;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.dbnode.file.TupleIterator;
import tod.impl.dbgrid.dbnode.file.TupleWriter;
import tod.impl.dbgrid.dbnode.file.HardPagedFile.Page;

public class TestTupleIterator
{
	@Test public void testIteration() throws FileNotFoundException
	{
		File theFile = new File("iterator.bin");
		theFile.delete();
		HardPagedFile thePagedFile = new HardPagedFile(theFile, DebuggerGridConfig.DB_PAGE_SIZE);
		StdTupleCodec theCodec = new StdTupleCodec();
		Page theFirstPage = thePagedFile.create();
		TupleWriter<StdTuple> theWriter = new TupleWriter<StdTuple>(thePagedFile, theCodec, theFirstPage, 0);
		
		for (int i=1;i<=10000;i++)
		{
			theWriter.add(new StdTuple(i, i));
		}
		
		TupleIterator<StdTuple> theIterator = new TupleIterator<StdTuple>(thePagedFile, theCodec, theFirstPage.asBitStruct());
		
		// From start, iterate until end
		assertTrue(theIterator.hasNext());
		assertFalse(theIterator.hasPrevious());
		
		for (int i=1;i<=10000;i++)
		{
			StdTuple theTuple = theIterator.next();
			assertTrue(theTuple.getTimestamp() == i);
			assertTrue(theTuple.getEventPointer() == i);
		}
		
		// From end, iterate until beginning
		assertFalse(theIterator.hasNext());
		assertTrue(theIterator.hasPrevious());
		
		for (int i=10000;i>=1;i--)
		{
			StdTuple theTuple = theIterator.previous();
			assertTrue(theTuple.getTimestamp() == i);
			assertTrue(theTuple.getEventPointer() == i);
		}

		// From start, iterate until end
		assertTrue(theIterator.hasNext());
		assertFalse(theIterator.hasPrevious());
		
		for (int i=1;i<=10000;i++)
		{
			StdTuple theTuple = theIterator.next();
			assertTrue(theTuple.getTimestamp() == i);
			assertTrue(theTuple.getEventPointer() == i);
		}
		
		// From end, iterate until beginning
		assertFalse(theIterator.hasNext());
		assertTrue(theIterator.hasPrevious());
		
		for (int i=10000;i>=1;i--)
		{
			StdTuple theTuple = theIterator.previous();
			assertTrue(theTuple.getTimestamp() == i);
			assertTrue(theTuple.getEventPointer() == i);
		}

		// From start iterate until middle
		assertTrue(theIterator.hasNext());
		assertFalse(theIterator.hasPrevious());
		
		for (int i=1;i<=5000;i++)
		{
			StdTuple theTuple = theIterator.next();
			assertTrue(theTuple.getTimestamp() == i);
			assertTrue(theTuple.getEventPointer() == i);
		}
		
		// From middle, iterate until beginning
		assertTrue(theIterator.hasNext());
		assertTrue(theIterator.hasPrevious());
		
		for (int i=5000;i>=1;i--)
		{
			StdTuple theTuple = theIterator.previous();
			assertTrue(theTuple.getTimestamp() == i);
			assertTrue(theTuple.getEventPointer() == i);
		}

		assertTrue(theIterator.hasNext());
		assertFalse(theIterator.hasPrevious());
	}
}
