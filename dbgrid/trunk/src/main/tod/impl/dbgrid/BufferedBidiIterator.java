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
package tod.impl.dbgrid;


/**
 * A bidirectional iterator that fetchs items by blocks.
 * @author gpothier
 */
public abstract class BufferedBidiIterator<B, I> extends AbstractBidiIterator<I>
{
	private BufferIterator itsBufferIterator = new BufferIterator();
	
	private boolean itsInitialized = false;
	private B itsCurrentBuffer;
	
	private int itsIndex;
	
	/**
	 * Direction of last move: -1 for previous, 1 for next, 0 for none
	 */
	private int itsLastMove = 0;
	
	private boolean itsEndReached = false;
	private boolean itsStartReached = false;
	
	@Override
	protected void reset()
	{
		super.reset();
		itsCurrentBuffer = null;
		itsInitialized = false;
		itsBufferIterator.reset();
		itsLastMove = 0;
		itsEndReached = false;
		itsStartReached = false;
	}
	
	/**
	 * Fetches the next available buffer.
	 * @return A buffer, or null if no more elements are available.
	 */
	protected abstract B fetchNextBuffer();
	
	/**
	 * Fetches the previous available buffer.
	 */
	protected abstract B fetchPreviousBuffer();
	
	/**
	 * Returns an item of the given buffer.
	 */
	protected abstract I get(B aBuffer, int aIndex);
	
	/**
	 * Returns the size of the given buffer.
	 */
	protected abstract int getSize(B aBuffer);

	@Override
	protected final I fetchNext()
	{
		if ((! itsInitialized || itsStartReached))
		{
			if (itsBufferIterator.hasNext())
			{
				itsCurrentBuffer = itsBufferIterator.next();
				assert itsCurrentBuffer != null;
//				if (itsCurrentBuffer == null) itsEndReached = true;
				itsIndex = 0;
				itsInitialized = true;
			}
			else 
			{
				itsCurrentBuffer = null;
				itsEndReached = true;
			}
		}
		
		if (itsEndReached) return null;
		
		if (itsLastMove == -1 && ! itsStartReached) 
		{
			B theNextBuffer = itsBufferIterator.next();
			int theCurrentSize = getSize(itsCurrentBuffer);
			int theNextSize = getSize(theNextBuffer);
			assert theNextSize >= theCurrentSize;
			itsCurrentBuffer = theNextBuffer;
		}
		itsLastMove = 1;
		
		if (itsIndex >= getSize(itsCurrentBuffer))
		{
			if (itsBufferIterator.hasNext())
			{
				itsCurrentBuffer = itsBufferIterator.next();
				itsIndex = 0;			
			}
			else
			{
				itsCurrentBuffer = null;
				itsEndReached = true;
			}
		}
		
		if (itsEndReached) return null;
		itsStartReached = false;
		
		return get(itsCurrentBuffer, itsIndex++);
	}

	@Override
	protected final I fetchPrevious()
	{
		if ((! itsInitialized || itsEndReached))
		{
			if (itsBufferIterator.hasPrevious())
			{
				itsCurrentBuffer = itsBufferIterator.previous();
				assert itsCurrentBuffer != null;
//				if (itsCurrentBuffer == null) itsStartReached = true;
				itsIndex = getSize(itsCurrentBuffer);
				itsInitialized = true;
			}
			else
			{
				itsCurrentBuffer = null;
				itsStartReached = true;
			}
		}
		
		if (itsStartReached) return null;
		
		if (itsLastMove == 1 && ! itsEndReached)
		{
			B thePreviousBuffer = itsBufferIterator.previous();
			int theCurrentSize = getSize(itsCurrentBuffer);
			int thePreviousSize = getSize(thePreviousBuffer);
			assert thePreviousSize >= theCurrentSize;
			itsIndex += thePreviousSize-theCurrentSize;
			itsCurrentBuffer = thePreviousBuffer;
		}
		itsLastMove = -1;
		
		
		if (itsIndex <= 0)
		{
			if (itsBufferIterator.hasPrevious())
			{
				itsCurrentBuffer = itsBufferIterator.previous();
				itsIndex = getSize(itsCurrentBuffer);			
			}
			else
			{
				itsCurrentBuffer = null;
				itsStartReached = true;
			}
		}
		
		if (itsStartReached) return null;
		itsEndReached = false;
		
		return get(itsCurrentBuffer, --itsIndex);
	}

	private class BufferIterator extends AbstractBidiIterator<B>
	{
		@Override
		protected B fetchNext()
		{
			return fetchNextBuffer();
		}

		@Override
		protected B fetchPrevious()
		{
			return fetchPreviousBuffer();
		}
	}
}
