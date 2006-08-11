/*
 * Created on Aug 10, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_POINTER_BITS;
import tod.impl.dbgrid.dbnode.HierarchicalIndex.IndexTuple;
import tod.impl.dbgrid.dbnode.PagedFile.Page;
import tod.impl.dbgrid.dbnode.PagedFile.PageBitStruct;
import zz.utils.bit.BitStruct;

/**
 * Provides binary search of {@link IndexTuple}s in {@link Page}s.
 * @author gpothier
 */
public class TupleFinder
{
	/**
	 * Finds the first tuple that verifies a condition on timestamp.
	 * See {@link #findTupleIndex(PageBitStruct, long, tod.impl.dbgrid.dbnode.HierarchicalIndex.TupleCodec, boolean)}
	 */
	public static <T extends IndexTuple> T findTuple(
			BitStruct aPage, 
			long aTimestamp, 
			TupleCodec<T> aTupleCodec,
			boolean aBefore)
	{
		int theIndex = findTupleIndex(aPage, aTimestamp, aTupleCodec, aBefore);
		return readTuple(aPage, aTupleCodec, theIndex);
	}
	
	/**
	 * Binary search of tuple.
	 * @param aBefore If true, then the search will return the tuple with the greatest timestamp
	 * that is smaller than the given timestamp.
	 * If false, the search will return the tuple which has the smallest timestamp value that is
	 * greater than or equeal to the given timestamp
	 */
	public static <T extends IndexTuple> int findTupleIndex(
			BitStruct aPage, 
			long aTimestamp, 
			TupleCodec<T> aTupleCodec,
			boolean aBefore)
	{
		aPage.setPos(0);
		int thePageSize = aPage.getRemainingBits();
		int theTupleCount = (thePageSize - DB_PAGE_POINTER_BITS) 
			/ aTupleCodec.getTupleSize();
		
		return findTupleIndex(aPage, aTimestamp, aTupleCodec, 0, theTupleCount-1, aBefore);
	}
	
	/**
	 * Binary search of tuple. 
	 * See {@link #findTupleIndex(PageBitStruct, long, tod.impl.dbgrid.dbnode.HierarchicalIndex.TupleCodec)}.
	 */
	public static <T extends IndexTuple> int findTupleIndex(
			BitStruct aPage, 
			long aTimestamp, 
			TupleCodec<T> aTupleCodec, 
			int aFirst, 
			int aLast,
			boolean aBefore)
	{
		assert aLast-aFirst > 0;
		
		T theFirstTuple = readTuple(aPage, aTupleCodec, aFirst);
		long theFirstTimestamp = theFirstTuple.getTimestamp();
		if (theFirstTimestamp == 0) theFirstTimestamp = Long.MAX_VALUE;
		
		T theLastTuple = readTuple(aPage, aTupleCodec, aLast);
		long theLastTimestamp = theLastTuple.getTimestamp();
		if (theLastTimestamp == 0) theLastTimestamp = Long.MAX_VALUE;
		
//		System.out.println(String.format("First  %d:%d", theFirstTimestamp, aFirst));
//		System.out.println(String.format("Last   %d:%d", theLastTimestamp, aLast));
		
		if (aTimestamp < theFirstTimestamp) return aBefore ? -1 : aFirst;
		if (aTimestamp == theFirstTimestamp) return aFirst;
		if (aTimestamp == theLastTimestamp) return aLast;
		if (aTimestamp > theLastTimestamp) return aBefore ? aLast : -1;
		
		if (aLast-aFirst == 1) return aFirst;
		
		int theMiddle = (aFirst + aLast) / 2;
		T theMiddleTuple = readTuple(aPage, aTupleCodec, theMiddle);
		long theMiddleTimestamp = theMiddleTuple.getTimestamp();
		if (theMiddleTimestamp == 0) theMiddleTimestamp = Long.MAX_VALUE;
		
//		System.out.println(String.format("Middle %d:%d", theMiddleTimestamp, theMiddle));
		
		if (aTimestamp == theMiddleTimestamp) return theMiddle;
		if (aTimestamp < theMiddleTimestamp) return findTupleIndex(aPage, aTimestamp, aTupleCodec, aFirst, theMiddle, aBefore);
		else return findTupleIndex(aPage, aTimestamp, aTupleCodec, theMiddle, aLast, aBefore);
	}
	
	public static <T extends Tuple> T readTuple(BitStruct aPage, TupleCodec<T> aTupleCodec, int aIndex)
	{
		aPage.setPos(aIndex * aTupleCodec.getTupleSize());
		return aTupleCodec.read(aPage);
	}


}