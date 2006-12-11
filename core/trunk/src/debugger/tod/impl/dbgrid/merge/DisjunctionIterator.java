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
package tod.impl.dbgrid.merge;

import tod.impl.dbgrid.BidiIterator;

/**
 * A disjunction (boolean OR) merge iterator.
 * @author gpothier
 */
public abstract class DisjunctionIterator<T> extends MergeIterator<T>
{
	public DisjunctionIterator(BidiIterator<T>[] aIterators)
	{
		super(aIterators);
	}
	
	@Override
	protected T fetchNext()
	{
		T theMinTimestampItem = null;
		long theMinTimestamp = Long.MAX_VALUE;

		// Find the item with the minimum timestamp
		for (int i = 0; i < getHeadCount(); i++)
		{
			T theItem = getNextHead(i);

			if (theItem != null)
			{
				long theTimestamp = getTimestamp(theItem);
				if (theTimestamp < theMinTimestamp)
				{
					theMinTimestamp = theTimestamp;
					theMinTimestampItem = theItem;
				}
			}
		}
		
		if (theMinTimestampItem == null) return null;

		// Move all heads that point to the same event
		for (int i = 0; i < getHeadCount(); i++)
		{
			T theItem = getNextHead(i);

			if (theItem != null && sameEvent(theMinTimestampItem, theItem))
			{
				moveNext(i);
			}
		}

		return theMinTimestampItem;
	}

	@Override
	protected T fetchPrevious()
	{
		T theMaxTimestampItem = null;
		long theMaxTimestamp = 0;

		// Find the item with the maximum timestamp
		for (int i = 0; i < getHeadCount(); i++)
		{
			T theItem = getPreviousHead(i);

			if (theItem != null)
			{
				long theTimestamp = getTimestamp(theItem);
				if (theTimestamp > theMaxTimestamp)
				{
					theMaxTimestamp = theTimestamp;
					theMaxTimestampItem = theItem;
				}
			}
		}
		
		if (theMaxTimestampItem == null) return null;

		// Move all heads that point to the same event
		for (int i = 0; i < getHeadCount(); i++)
		{
			T theItem = getPreviousHead(i);

			if (theItem != null && sameEvent(theMaxTimestampItem, theItem))
			{
				movePrevious(i);
			}
		}

		return theMaxTimestampItem;
	}
}
