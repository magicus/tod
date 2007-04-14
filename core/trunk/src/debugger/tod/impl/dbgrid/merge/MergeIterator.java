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
package tod.impl.dbgrid.merge;

import java.util.ArrayList;
import java.util.List;

import tod.impl.dbgrid.AbstractBidiIterator;
import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.db.file.IndexTuple;
import tod.impl.dbgrid.db.file.TupleIterator;
import zz.utils.ITask;
import zz.utils.Utils;

/**
 * Base class for merge iterators. Merge iterators merge the elements
 * provided by a number of source iterators, in ascending key order, where
 * the key is a long. The source iterators must also provide their elements
 * in ascending key order.
 * This abstract class maintains an array of head items,
 * one for each source iterator.
 * @author gpothier
 */
public abstract class MergeIterator<T> extends AbstractBidiIterator<T>
{
	private final BidiIterator<T>[] itsIterators;
	
	private final ITask<Integer, T> PREV_HEAD = new ITask<Integer, T>()
					{
						public T run(Integer aIndex)
						{
							return getPreviousHead(aIndex);
						}
					};
					
	private final ITask<Integer, T> NEXT_HEAD = new ITask<Integer, T>()
					{
						public T run(Integer aIndex)
						{
							return getNextHead(aIndex);
						}
					};
					
	private final List<Integer> INDEXES;

	public MergeIterator(BidiIterator<T>[] aIterators)
	{
		itsIterators = aIterators;
		INDEXES = new ArrayList<Integer>(getHeadCount());
		for(int i=0;i<getHeadCount();i++) INDEXES.add(i);
	}
	
	/**
	 * Returns the number of heads (base iterators) of this merge
	 * iterator.
	 */
	protected int getHeadCount()
	{
		return itsIterators.length;
	}

	/**
	 * Moves the specified head to the next element.
	 * @return True if it was possible to move, false otherwise.
	 */
	protected boolean moveNext(int aHeadIndex)
	{
		BidiIterator<T> theIterator = itsIterators[aHeadIndex];
		if (theIterator.hasNext())
		{
			theIterator.next();
			return true;
		}
		else return false;
	}
	
	/**
	 * Moves the specified head to the next element whose timestamp is at least
	 * the specified minimum timestamp.
	 */
	protected void moveForward(int aHeadIndex, long aMinKey)
	{
		BidiIterator<T> theIterator = itsIterators[aHeadIndex];
		boolean theMustAdvance = true;
		if (theIterator instanceof TupleIterator)
		{
			TupleIterator theTupleIterator = (TupleIterator) theIterator;
			IndexTuple theLastTuple = theTupleIterator.getLastTuple();
			
			if (aMinKey > theLastTuple.getKey())
			{
				theIterator = theTupleIterator.iteratorNextKey(aMinKey);
				itsIterators[aHeadIndex] = theIterator;
				theMustAdvance = false;
			}
		}
		
		if (theMustAdvance && theIterator.hasNext()) theIterator.next();
		while (theIterator.hasNext())
		{
			T theTuple = theIterator.peekNext();
			if (getKey(theTuple) >= aMinKey) break;
			theIterator.next();
		}
	}
	
	/**
	 * Moves the specified head to the previous element whose timestamp is at most
	 * the specified maximum timestamp.
	 */
	protected void moveBackward(int aHeadIndex, long aMaxKey)
	{
		BidiIterator<T> theIterator = itsIterators[aHeadIndex];
		boolean theMustAdvance = true;
		if (theIterator instanceof TupleIterator)
		{
			TupleIterator theTupleIterator = (TupleIterator) theIterator;
			IndexTuple theFirstTuple = theTupleIterator.getFirstTuple();
			
			if (aMaxKey < theFirstTuple.getKey())
			{
				theIterator = theTupleIterator.iteratorNextKey(aMaxKey);
				itsIterators[aHeadIndex] = theIterator;
				theMustAdvance = false;
				
				while (theIterator.hasNext())
				{
					T theTuple = theIterator.next();
					if (getKey(theTuple) > aMaxKey) break;
				}
			}
		}
		
		if (theMustAdvance && theIterator.hasPrevious()) theIterator.previous();
		while (theIterator.hasPrevious())
		{
			T theTuple = theIterator.peekPrevious();
			if (getKey(theTuple) <= aMaxKey) break;
			theIterator.previous();
		}
	}
	
	/**
	 * Moves the specified head to the previous element.
	 * @return True if it was possible to move, false otherwise.
	 */
	protected boolean movePrevious(int aHeadIndex)
	{
		BidiIterator<T> theIterator = itsIterators[aHeadIndex];
		if (theIterator.hasPrevious())
		{
			theIterator.previous();
			return true;
		}
		else return false;
	}
	
	/**
	 * Returns the key of the specified tuple.
	 */
	protected abstract long getKey(T aItem);
	
	/**
	 * Indicates if the specified items represent the same event.
	 */
	protected abstract boolean sameEvent(T aItem1, T aItem2);
	
	/**
	 * Indicates if the specified items are the same.
	 * This is potentially more restrictive than {@link #sameEvent(Object, Object)},
	 * for example for role tuples the role equality is checked.
	 */
	protected boolean sameItem(T aItem1, T aItem2)
	{
		return aItem1.equals(aItem2);
	}

	protected T getNextHead(int aHead)
	{
		return itsIterators[aHead].peekNext();
	}
	
	protected List<T> getNextHeads(List<T> aBuffer)
	{
		return fork(aBuffer, NEXT_HEAD);
	}

	protected T getPreviousHead(int aHead)
	{
		return itsIterators[aHead].peekPrevious();
	}
	
	protected List<T> getPreviousHeads(List<T> aBuffer)
	{
		return fork(aBuffer, PREV_HEAD);
	}

	/**
	 * Forks a given task to all heads. The tasks are executed in parallel
	 * iff {@link #parallelFetch()} returns true.
	 * @param aTask The task to fork, which receives a head index as input.
	 * @param aBuffer A buffer to use to store the results, used if possible.
	 * @return The result of each task, in the same order as the heads.
	 */
	protected List<T> fork(List<T> aBuffer, ITask<Integer, T> aTask)
	{
		if (parallelFetch())
		{
			return Utils.fork(INDEXES, aTask);
		}
		else
		{
			for(int i=0;i<getHeadCount();i++) Utils.listSet(aBuffer, i, aTask.run(i));
			return aBuffer;
		}
	}
	
	/**
	 * Whether heads should be fetched in parallel.
	 */
	protected boolean parallelFetch()
	{
		return false;
	}
}
