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

import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.db.RINodeEventIterator;
import tod.impl.dbgrid.dispatch.RIDatabaseNode;
import zz.utils.Future;
import zz.utils.ITask;
import zz.utils.Utils;

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
		List<T> theBuffer = new ArrayList<T>(getHeadCount());
		T theMinTimestampItem = null;
		long theMinTimestamp = Long.MAX_VALUE;

		List<T> theHeads = getNextHeads(theBuffer);
		
		// Find the item with the minimum timestamp
		for (int i = 0; i < getHeadCount(); i++)
		{
			T theItem = theHeads.get(i);

			if (theItem != null)
			{
				long theTimestamp = getKey(theItem);
				if (theTimestamp < theMinTimestamp)
				{
					theMinTimestamp = theTimestamp;
					theMinTimestampItem = theItem;
				}
			}
		}
		
		if (theMinTimestampItem == null) return null;

		// Move all heads that point to the same event
		final T theMinTimestampItem0 = theMinTimestampItem;
		fork(theBuffer, new ITask<Integer, T>()
				{
					public T run(Integer aIndex)
					{
						T theItem = getNextHead(aIndex);

						if (theItem != null && sameEvent(theMinTimestampItem0, theItem))
						{
							moveNext(aIndex);
						}
						return null;
					}
				});

		return theMinTimestampItem;
	}

	@Override
	protected T fetchPrevious()
	{
		List<T> theBuffer = new ArrayList<T>(getHeadCount());
		T theMaxTimestampItem = null;
		long theMaxTimestamp = 0;

		List<T> theHeads = getPreviousHeads(theBuffer);
		
		// Find the item with the maximum timestamp
		for (int i = 0; i < getHeadCount(); i++)
		{
			T theItem = theHeads.get(i);

			if (theItem != null)
			{
				long theTimestamp = getKey(theItem);
				if (theTimestamp > theMaxTimestamp)
				{
					theMaxTimestamp = theTimestamp;
					theMaxTimestampItem = theItem;
				}
			}
		}
		
		if (theMaxTimestampItem == null) return null;

		// Move all heads that point to the same event
		final T theMaxTimestampItem0 = theMaxTimestampItem;
		fork(theBuffer, new ITask<Integer, T>()
				{
					public T run(Integer aIndex)
					{
						T theItem = getPreviousHead(aIndex);

						if (theItem != null && sameEvent(theMaxTimestampItem0, theItem))
						{
							movePrevious(aIndex);
						}
						return null;
					}
				});

		return theMaxTimestampItem;
	}
}
