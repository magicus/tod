/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

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
package tod.impl.dbgrid.db;

import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.merge.ConjunctionIterator;
import tod.impl.dbgrid.merge.DisjunctionIterator;

/**
 * This class permit to perform n-ary merge joins between indexes.
 * @author gpothier
 */
public class IndexMerger
{
	/**
	 * Returns an iterator that retrieves all the events that are common to all
	 * the specified indexes starting from a specified timestamp. 
	 */
	public static <T extends StdIndexSet.StdTuple> BidiIterator<T> conjunction(
			BidiIterator<T>[] aIterators)
	{
		return new MyConjunctionIterator<T>(aIterators);
	}
	
	/**
	 * Returns an iterator that retrieves all the events of all the specified indexes. 
	 */
	public static <T extends StdIndexSet.StdTuple> BidiIterator<T> disjunction(
			BidiIterator<T>[] aIterators)
	{
		return new MyDisjunctionIterator<T>(aIterators);
	}
	
	/**
	 * Returns the tuple iterators of all the specified indexes, starting
	 * at the specified timestamp. 
	 */
	public static <T extends StdIndexSet.StdTuple> BidiIterator<T>[] getIterators(
			HierarchicalIndex<T>[] aIndexes,
			long aTimestamp)
	{
		BidiIterator<T>[] theIterators = new BidiIterator[aIndexes.length];
		for (int i = 0; i < aIndexes.length; i++)
		{
			HierarchicalIndex<T> theIndex = aIndexes[i];
			theIterators[i] = theIndex.getTupleIterator(aTimestamp);
		}
		return theIterators;
	}
	
	
	public static class MyConjunctionIterator<T extends StdIndexSet.StdTuple> 
	extends ConjunctionIterator<T>
	{
		public MyConjunctionIterator(BidiIterator<T>[] aIterators)
		{
			super(aIterators);
		}

		@Override
		protected long getKey(T aItem)
		{
			return aItem.getKey();
		}

		@Override
		protected boolean sameEvent(T aItem1, T aItem2)
		{
			return aItem1.getEventPointer() == aItem2.getEventPointer();
		}
	}
	
	public static class MyDisjunctionIterator<T extends StdIndexSet.StdTuple> 
	extends DisjunctionIterator<T>
	{
		public MyDisjunctionIterator(BidiIterator<T>[] aIterators)
		{
			super(aIterators);
		}
		
		@Override
		protected long getKey(T aItem)
		{
			return aItem.getKey();
		}

		@Override
		protected boolean sameEvent(T aItem1, T aItem2)
		{
			return aItem1.getEventPointer() == aItem2.getEventPointer();
		}
	}
}
