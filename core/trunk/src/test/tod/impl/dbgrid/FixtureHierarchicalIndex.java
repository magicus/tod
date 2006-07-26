/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid;

import java.io.File;
import java.io.FileNotFoundException;

import tod.impl.dbgrid.dbnode.HierarchicalIndex;
import tod.impl.dbgrid.dbnode.PagedFile;
import tod.impl.dbgrid.dbnode.StdIndexSet;

public class FixtureHierarchicalIndex
{

	public static HierarchicalIndex<StdIndexSet.Tuple> createStdIndex() throws FileNotFoundException
	{
		File theFile = new File("indexTest.bin");
		theFile.delete();
		PagedFile thePagedFile = new PagedFile(theFile, DebuggerGridConfig.DB_INDEX_PAGE_SIZE);
		return new HierarchicalIndex<StdIndexSet.Tuple>(thePagedFile, StdIndexSet.TUPLE_CODEC);
	}

	public static long inventData(long aTimestamp)
	{
		return aTimestamp*7;
	}

	/**
	 * Fills an index with values.
	 */
	public static void fillIndex(
			HierarchicalIndex<StdIndexSet.Tuple> aIndex, 
			TestHierarchicalIndex.TimestampGenerator aGenerator,
			long aTupleCount)
	{
		for (long i=0;i<aTupleCount;i++)
		{
			long theTimestamp = aGenerator.next();
			long theData = inventData(theTimestamp);
			
			aIndex.add(new StdIndexSet.Tuple(theTimestamp, theData));
			
			if (i % 1000000 == 0) System.out.println("w: "+i);
		}
	}

}
