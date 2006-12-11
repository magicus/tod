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
		if (! itsInitialized && itsBufferIterator.hasNext())
		{
			itsCurrentBuffer = itsBufferIterator.next();
			itsIndex = 0;
		}
		
		if (itsCurrentBuffer == null) return null;
		
		if (itsIndex >= getSize(itsCurrentBuffer) && itsBufferIterator.hasNext())
		{
			itsCurrentBuffer = itsBufferIterator.next();
			itsIndex = 0;			
		}
		
		if (itsCurrentBuffer == null) return null;
		
		return get(itsCurrentBuffer, itsIndex++);
	}

	@Override
	protected final I fetchPrevious()
	{
		if (! itsInitialized && itsBufferIterator.hasPrevious())
		{
			itsCurrentBuffer = itsBufferIterator.previous();
			itsIndex = getSize(itsCurrentBuffer);
		}
		
		if (itsCurrentBuffer == null) return null;
		
		if (itsIndex < 0 && itsBufferIterator.hasPrevious())
		{
			itsCurrentBuffer = itsBufferIterator.previous();
			itsIndex = getSize(itsCurrentBuffer);			
		}
		
		if (itsCurrentBuffer == null) return null;
		
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
