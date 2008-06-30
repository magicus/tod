package tod.impl.evdbng.db.file;

import tod.impl.evdbng.db.file.PagedFile.Page;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;

/**
 * Stores the decoded tuples of a {@link Page}.
 * @author gpothier
 */
public abstract class TupleBuffer<T extends Tuple>
{
	/**
	 * tuple count of the previous page
	 */
	private long itsTupleCount = -1;
	
	private final int itsPreviousPageId;
	private final int itsNextPageId;
	
	private final long[] itsKeyBuffer;
	private int itsPosition;
	
	public TupleBuffer(int aSize, int aPreviousPageId, int aNextPageId)
	{
		itsKeyBuffer = new long[aSize];
		itsPreviousPageId = aPreviousPageId;
		itsNextPageId = aNextPageId;
	}
	
	public void read(long aKey, PageIOStream aStream)
	{
		itsKeyBuffer[itsPosition] = aKey;
		read0(itsPosition, aStream);
		itsPosition++;
	}
	
	/**
	 * Returns the number of tuples in this buffer.
	 */
	public int getSize()
	{
		return itsPosition;
	}

	/**
	 * Returns the key at the specified position.
	 */
	public long getKey(int aPosition)
	{
		return itsKeyBuffer[aPosition];
	}
	
	/**
	 * Creates a tuple object corresponding to the tuple at the 
	 * specified position.
	 */
	public abstract T getTuple(int aPosition);
	
	/**
	 * Reads a tuple data into internal buffers.
	 */
	public abstract void read0(int aPosition, PageIOStream aStream);
	
	public int getPreviousPageId()
	{
		return itsPreviousPageId;
	}

	public int getNextPageId()
	{
		return itsNextPageId;
	}

	/**
	 * Returns the number of tuples before the beginning of the page,
	 * if the info is available (otherwise returns -1).
	 */
	public long getTupleCount()
	{
		return itsTupleCount;
	}

	public void setTupleCount(long aTupleCount)
	{
		itsTupleCount = aTupleCount;
	}


	/**
	 * Tuple data reader for simple tuples
	 * @author gpothier
	 */
	public static class SimpleTupleBuffer extends TupleBuffer<SimpleTuple>
	{
		public SimpleTupleBuffer(int aSize, int aPreviousPageId, int aNextPageId)
		{
			super(aSize, aPreviousPageId, aNextPageId);
		}

		@Override
		public void read0(int aPosition, PageIOStream aStream)
		{
		}

		@Override
		public SimpleTuple getTuple(int aPosition)
		{
			return new SimpleTuple(getKey(aPosition));
		}
	}
	
	/**
	 * Tuple data reader for role tuples
	 * @author gpothier
	 */
	public static class RoleTupleBuffer extends TupleBuffer<RoleTuple>
	{
		private byte[] itsBuffer;
		
		public RoleTupleBuffer(int aSize, int aPreviousPageId, int aNextPageId)
		{
			super(aSize, aPreviousPageId, aNextPageId);
			itsBuffer = new byte[aSize];
		}

		@Override
		public void read0(int aPosition, PageIOStream aStream)
		{
			itsBuffer[aPosition] = (byte) aStream.readRole();
		}

		@Override
		public RoleTuple getTuple(int aPosition)
		{
			return new RoleTuple(getKey(aPosition), itsBuffer[aPosition]);
		}
	}
	
	public static class InternalTupleBuffer extends TupleBuffer<InternalTuple>
	{
		private int[] itsPageIdBuffer;
		private long[] itsTupleCountBuffer;
		
		public InternalTupleBuffer(int aSize, int aPreviousPageId, int aNextPageId)
		{
			super(aSize, aPreviousPageId, aNextPageId);
			itsPageIdBuffer = new int[aSize];
			itsTupleCountBuffer = new long[aSize];
		}

		@Override
		public void read0(int aPosition, PageIOStream aStream)
		{
			itsPageIdBuffer[aPosition] = aStream.readPagePointer();
			itsTupleCountBuffer[aPosition] = aStream.readTupleCount();
		}

		@Override
		public InternalTuple getTuple(int aPosition)
		{
			return new InternalTuple(
					getKey(aPosition), 
					itsPageIdBuffer[aPosition], 
					itsTupleCountBuffer[aPosition]);
		}
	}

	public static class ObjectPointerTupleBuffer extends TupleBuffer<ObjectPointerTuple>
	{
		private int[] itsPageIdBuffer;
		private short[] itsOffsetBuffer;
		
		public ObjectPointerTupleBuffer(int aSize, int aPreviousPageId, int aNextPageId)
		{
			super(aSize, aPreviousPageId, aNextPageId);
			itsPageIdBuffer = new int[aSize];
			itsOffsetBuffer = new short[aSize];
		}
		
		@Override
		public void read0(int aPosition, PageIOStream aStream)
		{
			itsPageIdBuffer[aPosition] = aStream.readPagePointer();
			itsOffsetBuffer[aPosition] = aStream.readPageOffset();
		}
		
		@Override
		public ObjectPointerTuple getTuple(int aPosition)
		{
			return new ObjectPointerTuple(
					getKey(aPosition), 
					itsPageIdBuffer[aPosition], 
					itsOffsetBuffer[aPosition]);
		}
	}
	

}