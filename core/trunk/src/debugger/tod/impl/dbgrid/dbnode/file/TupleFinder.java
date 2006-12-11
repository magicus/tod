/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.dbgrid.dbnode.file;

import zz.utils.bit.BitStruct;

/**
 * Provides binary search of {@link IndexTuple}s in {@link Page}s.
 * @author gpothier
 */
public class TupleFinder
{
	public static <T> int getTuplesPerPage(
			int aPageSize, 
			int aPagePointerSize,
			TupleCodec<T> aTupleCodec)
	{
		return (aPageSize - 2*aPagePointerSize) / aTupleCodec.getTupleSize();
	}
	
	/**
	 * Finds the first tuple that verifies a condition on timestamp.
	 * See {@link #findTupleIndex(PageBitStruct, long, tod.impl.dbgrid.dbnode.HierarchicalIndex.TupleCodec, boolean)}
	 * @return The first matching tuple, or null if no tuple matches,
	 * ie. if {@link #findTupleIndex(PageBitStruct, long, tod.impl.dbgrid.dbnode.HierarchicalIndex.TupleCodec, boolean)}
	 * returns -1.
	 */
	public static <T extends IndexTuple> T findTuple(
			BitStruct aPage, 
			int aPagePointerSize,
			long aTimestamp, 
			TupleCodec<T> aTupleCodec,
			boolean aBefore)
	{
		int theIndex = findTupleIndex(aPage, aPagePointerSize, aTimestamp, aTupleCodec, aBefore);
		if (theIndex < 0) return null;
		return readTuple(aPage, aTupleCodec, theIndex);
	}
	
	/**
	 * Binary search of tuple.
	 * @param aBefore If true, then the search will return the tuple with the greatest timestamp
	 * that is smaller than the given timestamp.
	 * If false, the search will return the tuple which has the smallest timestamp value that is
	 * greater than or equeal to the given timestamp
	 * @param aPagePointerSize Size in bits of page pointers for linking to next/previous pages.
	 */
	public static <T extends IndexTuple> int findTupleIndex(
			BitStruct aPage, 
			int aPagePointerSize,
			long aTimestamp, 
			TupleCodec<T> aTupleCodec,
			boolean aBefore)
	{
		int theTupleCount = getTuplesPerPage(aPage.getTotalBits(), aPagePointerSize, aTupleCodec);
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
		assert aIndex >= 0;
		aPage.setPos(aIndex * aTupleCodec.getTupleSize());
		return aTupleCodec.read(aPage);
	}


}
