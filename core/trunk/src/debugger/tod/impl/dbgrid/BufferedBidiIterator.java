/*
 * Created on Nov 2, 2006
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
