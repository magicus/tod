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

import tod.impl.dbgrid.AbstractBidiIterator;
import tod.impl.dbgrid.BidiIterator;

/**
 * Base class for merge iterators. Maintains an array of head items,
 * one for each source iterator.
 * @author gpothier
 */
public abstract class MergeIterator<T> extends AbstractBidiIterator<T>
{
	private final BidiIterator<T>[] itsIterators;

	public MergeIterator(BidiIterator<T>[] aIterators)
	{
		itsIterators = aIterators;
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
		if (itsIterators[aHeadIndex].hasNext())
		{
			itsIterators[aHeadIndex].next();
			return true;
		}
		else return false;
	}

	/**
	 * Moves the specified head to the previous element.
	 * @return True if it was possible to move, false otherwise.
	 */
	protected boolean movePrevious(int aHeadIndex)
	{
		if (itsIterators[aHeadIndex].hasPrevious())
		{
			itsIterators[aHeadIndex].previous();
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

	protected T getNextHead(int aHead)
	{
		return itsIterators[aHead].peekNext();
	}

	protected T getPreviousHead(int aHead)
	{
		return itsIterators[aHead].peekPrevious();
	}
	
}
