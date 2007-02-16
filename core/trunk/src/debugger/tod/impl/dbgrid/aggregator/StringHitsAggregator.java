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
package tod.impl.dbgrid.aggregator;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.BufferedBidiIterator;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.db.RIBufferIterator;
import tod.impl.dbgrid.dispatch.LeafEventDispatcher;
import tod.impl.dbgrid.dispatch.RILeafDispatcher;
import tod.impl.dbgrid.dispatch.RILeafDispatcher.StringSearchHit;
import tod.impl.dbgrid.merge.DisjunctionIterator;
import zz.utils.Future;

/**
 * Aggregates string hits provided by the {@link LeafEventDispatcher}s.
 * @author gpothier
 */
public class StringHitsAggregator extends UnicastRemoteObject
implements RIBufferIterator<StringSearchHit[]>
{
	private final GridMaster itsMaster;
	private final String itsSearchText;
	private MergeIterator itsMergeIterator;

	public StringHitsAggregator(GridMaster aMaster, String aSearchText) throws RemoteException
	{
		itsMaster = aMaster;
		itsSearchText = aSearchText;
		initIterators();
	}
	
	private void initIterators()
	{
		final List<RILeafDispatcher> theDispatchers = itsMaster.getLeafDispatchers();
		final SearchHitIterator[] theIterators = new SearchHitIterator[theDispatchers.size()];
		
		List<Future<SearchHitIterator>> theFutures = new ArrayList<Future<SearchHitIterator>>();
		
		for (int i=0;i<theDispatchers.size();i++)
		{
			final int i0 = i;
			theFutures.add(new Future<SearchHitIterator>()
					{
						@Override
						protected SearchHitIterator fetch() throws Throwable
						{
							RILeafDispatcher theNode = theDispatchers.get(i0);
							RIBufferIterator<StringSearchHit[]> theIterator = theNode.searchStrings(itsSearchText);
							theIterators[i0] = new SearchHitIterator(theIterator);
							
							return theIterators[i0];
						}
					});
			
		}

		// Ensure all futures have completed
		for (Future<SearchHitIterator> theFuture : theFutures) theFuture.get();
		
		itsMergeIterator = new MergeIterator(theIterators);
	}

	private static StringSearchHit[] toArray(List<StringSearchHit> aList)
	{
		return aList.size() > 0 ?
				aList.toArray(new StringSearchHit[aList.size()])
				: null;
	}
	
	public StringSearchHit[] next(int aCount)
	{
		List<StringSearchHit> theList = new ArrayList<StringSearchHit>(aCount);
		for (int i=0;i<aCount;i++)
		{
			if (itsMergeIterator.hasNext()) theList.add(itsMergeIterator.next());
			else break;
		}

		return toArray(theList);
	}

	public StringSearchHit[] previous(int aCount)
	{
		List<StringSearchHit> theList = new ArrayList<StringSearchHit>(aCount);
		for (int i=0;i<aCount;i++)
		{
			if (itsMergeIterator.hasPrevious()) theList.add(itsMergeIterator.previous());
			else break;
		}

		int theSize = theList.size();
		if (theSize == 0) return null;
		
		StringSearchHit[] theResult = new StringSearchHit[theSize];
		for (int i=0;i<theSize;i++) theResult[i] = theList.get(theSize-i-1);
		
		return theResult;
	}


	/**
	 * A real iterator that wraps a {@link RIBufferIterator}
	 * @author gpothier
	 */
	private static class SearchHitIterator extends BufferedBidiIterator<StringSearchHit[], StringSearchHit>
	{
		private RIBufferIterator<StringSearchHit[]> itsIterator;

		public SearchHitIterator(RIBufferIterator<StringSearchHit[]> aIterator)
		{
			assert aIterator != null;
			itsIterator = aIterator;
		}
		
		@Override
		protected StringSearchHit[] fetchNextBuffer()
		{
			try
			{
				return itsIterator.next(DebuggerGridConfig.QUERY_ITERATOR_BUFFER_SIZE);
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		protected StringSearchHit[] fetchPreviousBuffer()
		{
			try
			{
				return itsIterator.previous(DebuggerGridConfig.QUERY_ITERATOR_BUFFER_SIZE);
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		protected int getSize(StringSearchHit[] aBuffer)
		{
			return aBuffer.length;
		}

		@Override
		protected StringSearchHit get(StringSearchHit[] aBuffer, int aIndex)
		{
			return aBuffer[aIndex];
		}
	}
	
	/**
	 * The iterator that merges results from all the nodes
	 * @author gpothier
	 */
	private static class MergeIterator extends DisjunctionIterator<StringSearchHit>
	{
		public MergeIterator(BidiIterator<StringSearchHit>[] aIterators)
		{
			super(aIterators);
		}

		@Override
		protected long getKey(StringSearchHit aItem)
		{
			return aItem.getScore();
		}

		@Override
		protected boolean sameEvent(StringSearchHit aItem1, StringSearchHit aItem2)
		{
			return aItem1.getObjectId() == aItem2.getObjectId();
		}
	}

}
