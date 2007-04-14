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
import tod.impl.dbgrid.db.file.IndexTuple;
import zz.utils.ITask;

/**
 * Conjunction (boolean AND) merge iterator.
 * @author gpothier
 */
public abstract class ConjunctionIterator<T> extends MergeIterator<T>
{
	public ConjunctionIterator(BidiIterator<T>[] aIterators)
	{
		super(aIterators);
	}

	@Override
	protected T fetchNext()
	{
		List<T> theBuffer = new ArrayList<T>(getHeadCount());
		T theResult = null;
		boolean theMatch;
		do
		{
			theMatch = true;

			T theRefItem = null;
			int theMinTimestampHead = -1;
			long theMinTimestamp = Long.MAX_VALUE;
			long theMaxTimestamp = 0;

			List<T> theHeads = getNextHeads(theBuffer);
			
			// Check if current head set is a match (ie. all head tuples point
			// to the same event).
			// At the same time find the head that has the minimum timestamp
			for (int i = 0; i < getHeadCount(); i++)
			{
				T theItem = theHeads.get(i);
				if (theItem == null) return null;

				if (theRefItem == null) theRefItem = theItem;
				else if (! sameItem(theRefItem, theItem)) theMatch = false;

				long theTimestamp = getKey(theItem);
				if (theTimestamp < theMinTimestamp)
				{
					theMinTimestamp = theTimestamp;
					theMinTimestampHead = i;
				}
				if (theTimestamp > theMaxTimestamp) theMaxTimestamp = theTimestamp;
			}

			if (theMatch)
			{
				theResult = theRefItem;
				fork(theBuffer, new ITask<Integer, T>()
						{
							public T run(Integer aIndex)
							{
								moveNext(aIndex);
								return null;
							}
						});
			}
			else
			{
				moveForward(theMinTimestampHead, theMaxTimestamp);
			}
		}
		while (!theMatch);

		return theResult;
	}

	@Override
	protected T fetchPrevious()
	{
		List<T> theBuffer = new ArrayList<T>(getHeadCount());
		T theResult = null;
		boolean theMatch;
		do
		{
			theMatch = true;

			T theRefItem = null;
			int theMaxTimestampHead = -1;
			long theMaxTimestamp = 0;
			long theMinTimestamp = Long.MAX_VALUE;
			
			List<T> theHeads = getPreviousHeads(theBuffer);

			// Check if current head set is a match (ie. all head tuples point
			// to the same event).
			// At the same time find the head that has the maximum timestamp
			for (int i = 0; i < getHeadCount(); i++)
			{
				T theItem = theHeads.get(i);
				if (theItem == null) return null;

				if (theRefItem == null) theRefItem = theItem;
				else if (! sameItem(theRefItem, theItem)) theMatch = false;

				long theTimestamp = getKey(theItem);
				if (theTimestamp > theMaxTimestamp)
				{
					theMaxTimestamp = theTimestamp;
					theMaxTimestampHead = i;
				}
				if (theTimestamp < theMinTimestamp) theMinTimestamp = theTimestamp;
			}

			if (theMatch)
			{
				theResult = theRefItem;
				fork(theBuffer, new ITask<Integer, T>()
						{
							public T run(Integer aIndex)
							{
								movePrevious(aIndex);
								return null;
							}
						});

			}
			else
			{
				moveBackward(theMaxTimestampHead, theMinTimestamp);
			}
		}
		while (!theMatch);

		return theResult;
	}
}
