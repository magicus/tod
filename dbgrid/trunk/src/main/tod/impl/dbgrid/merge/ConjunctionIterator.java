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

import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.merge.MergeIterator.Nav;
import zz.utils.ITask;

/**
 * Conjunction (boolean AND) merge iterator.
 * @author gpothier
 */
public abstract class ConjunctionIterator<T> extends MergeIterator<T>
{
	private ThreadLocal<ForwardFetcher> itsForwardFetchers = new ThreadLocal<ForwardFetcher>()
	{
		@Override
		protected ForwardFetcher initialValue()
		{
			return new ForwardFetcher(getHeadCount());
		}
	};
	
	private ThreadLocal<BackwardFetcher> itsBackwardFetchers = new ThreadLocal<BackwardFetcher>()
	{
		@Override
		protected BackwardFetcher initialValue()
		{
			return new BackwardFetcher(getHeadCount());
		}
	};
	
	public ConjunctionIterator(IBidiIterator<T>[] aIterators)
	{
		super(aIterators);
	}

	@Override
	protected T fetchNext()
	{
		return itsForwardFetchers.get().fetch();
	}
	
	@Override
	protected T fetchPrevious()
	{
		return itsBackwardFetchers.get().fetch();
	}
	
	/**
	 * Helper class for implementing fetch operations. Most of the 
	 * direction-related code is abstracted with the {@link Nav} class.
	 * Instances of this class are be obtained through Thread-Local Storage,
	 * so it is safe to store temporary data in fields.
	 * @author gpothier
	 */
	private abstract class Fetcher
	{
		private final Nav itsDirection;
		
		/**
		 * Buffer for current head values
		 */
		private final T[] itsHeadBuffer;
		
		/**
		 * Buffers used to store previous tuples when all tuples point to the
		 * same event but for different roles.
		 */
		private final List<T>[] itsBackBuffers;
		
		public Fetcher(Nav aDirection, int aHeadCount)
		{
			itsDirection = aDirection;
			itsHeadBuffer = (T[]) new Object[aHeadCount];
			itsBackBuffers = new List[aHeadCount];
			for (int i=0;i<aHeadCount;i++) itsBackBuffers[i] = new ArrayList<T>();
		}
		
		private void clearBackBuffers()
		{
			for (int i=0;i<itsBackBuffers.length;i++)
			{
				itsBackBuffers[i].clear();
			}
		}
		
		public T fetch()
		{
			T theResult = null;
			boolean theMatch;
			do
			{
				theMatch = true;
				boolean theSameEvent = true;
				boolean theSameKey = true;

				T theRefTuple = null;
				initTimestamps();

				T[] theHeads = itsDirection.peekHeads(itsHeadBuffer);
				
				// Check if current head set is a match (ie. all head tuples point
				// to the same event).
				// At the same time find the head that has the minimum/maximum timestamp
				for (int i = 0; i < getHeadCount(); i++)
				{
					T theItem = theHeads[i];
					if (theItem == null) return null;

					if (theRefTuple == null) theRefTuple = theItem;
					else 
					{
						if (! sameItem(theRefTuple, theItem)) theMatch = false;
						if (! sameEvent(theRefTuple, theItem)) theSameEvent = false;
						if (! sameKey(theRefTuple, theItem)) theSameKey = false;
					}

					long theTimestamp = getKey(theItem);
					registerTimestamp(i, theTimestamp);
				}

				if (theMatch)
				{
					// Current head set matched: advance all heads.
					theResult = theRefTuple;
					fork(itsHeadBuffer, new ITask<Integer, T>()
							{
								public T run(Integer aIndex)
								{
									itsDirection.move(aIndex);
									return null;
								}
							});
				}
				else
				{
					if (theSameEvent)
					{
						T theTuple = fetchSameEvent(theHeads);
						if (theTuple != null) return theTuple;
					}
					
					if (! theSameKey) itsDirection.move(getSelectedHead(), getGoalTimestamp());
					else itsDirection.move(getSelectedHead());
				}
			}
			while (!theMatch);

			return theResult;
		}
		
		/**
		 * Handles the case in which all head tuples point to the same event but with
		 * a different role.
		 * In this case, all the tuples that refer to the same event are fetched, and
		 * a match is searched amongst them.
		 */
		private T fetchSameEvent(T[] aHeads)
		{
			clearBackBuffers();
			T theRefTuple = aHeads[0];
			
			// Read the heads that point to the same event.
			int theSmallestHeadSize = Integer.MAX_VALUE;
			int theSmallestHead = -1;
			for(int i=0;i<getHeadCount();i++)
			{
				while(true)
				{
					T theTuple = itsDirection.peekHead(i);
					if (theTuple == null) break;
					if (! sameEvent(theRefTuple, theTuple)) break;
					
					itsBackBuffers[i].add(theTuple);
					itsDirection.move(i);
				}
				
				int theHeadSize = itsBackBuffers[i].size();
				if (theHeadSize < theSmallestHeadSize)
				{
					theSmallestHeadSize = theHeadSize;
					theSmallestHead = i;
				}
			}
			
			// Check if there is a match
			for (int i=0;i<theSmallestHeadSize;i++)
			{
				boolean theMatch = true;
				T theTuple = itsBackBuffers[theSmallestHead].get(i);
				for (int h=0;h<getHeadCount();h++)
				{
					if (h == i) continue;
					if (! hasTuple(theTuple, itsBackBuffers[h]))
					{
						theMatch = false;
						break;
					}
				}
				if (theMatch) return theTuple;
			}
			
			return null;
		}

		/**
		 * Determines whether the specified list of tuples contains a tuple
		 * that is identical to the given reference tuple.
		 */
		private boolean hasTuple(T aRefTuple, List<T> aList)
		{
			for (T theTuple : aList)
			{
				if (sameItem(aRefTuple, theTuple)) return true;
			}
			return false;
		}

		protected abstract void initTimestamps();
		protected abstract void registerTimestamp(int aHead, long aTimestamp);
		
		/**
		 * Returns the index of the head that was selected for advancing
		 */
		protected abstract int getSelectedHead();
		
		/**
		 * Returns the timestamp to which the selected head should be advanced.
		 */
		protected abstract long getGoalTimestamp();
		
	}
	
	private class ForwardFetcher extends Fetcher
	{
		private int itsMinTimestampHead;
		private long itsMinTimestamp;
		private long itsMaxTimestamp;

		public ForwardFetcher(int aHeadCount)
		{
			super(FORWARD, aHeadCount);
			
		}

		@Override
		protected void initTimestamps()
		{
			itsMinTimestampHead = -1;
			itsMinTimestamp = Long.MAX_VALUE;
			itsMaxTimestamp = 0;
		}

		@Override
		protected void registerTimestamp(int aHead, long aTimestamp)
		{
			if (aTimestamp < itsMinTimestamp)
			{
				itsMinTimestamp = aTimestamp;
				itsMinTimestampHead = aHead;
			}
			if (aTimestamp > itsMaxTimestamp) itsMaxTimestamp = aTimestamp;
		}
		
		@Override
		protected long getGoalTimestamp()
		{
			return itsMaxTimestamp;
		}

		@Override
		protected int getSelectedHead()
		{
			return itsMinTimestampHead;
		}
	}
	
	private class BackwardFetcher extends Fetcher
	{
		private int itsMaxTimestampHead;
		private long itsMaxTimestamp;
		private long itsMinTimestamp;

		public BackwardFetcher(int aHeadCount)
		{
			super(BACKWARD, aHeadCount);
		}

		@Override
		protected void initTimestamps()
		{
			itsMaxTimestampHead = -1;
			itsMaxTimestamp = 0;
			itsMinTimestamp = Long.MAX_VALUE;
		}

		@Override
		protected void registerTimestamp(int aHead, long aTimestamp)
		{
			if (aTimestamp > itsMaxTimestamp)
			{
				itsMaxTimestamp = aTimestamp;
				itsMaxTimestampHead = aHead;
			}
			if (aTimestamp < itsMinTimestamp) itsMinTimestamp = aTimestamp;
		}
		
		@Override
		protected long getGoalTimestamp()
		{
			return itsMinTimestamp;
		}

		@Override
		protected int getSelectedHead()
		{
			return itsMaxTimestampHead;
		}

	}
}
