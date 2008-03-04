/*
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.database;


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
