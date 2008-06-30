package tod.impl.evdbng.db.file;

import tod.impl.evdbng.db.file.PagedFile.PageIOStream;
import tod.impl.evdbng.db.file.TupleBuffer.InternalTupleBuffer;
import tod.impl.evdbng.db.file.TupleBuffer.ObjectPointerTupleBuffer;
import tod.impl.evdbng.db.file.TupleBuffer.RoleTupleBuffer;
import tod.impl.evdbng.db.file.TupleBuffer.SimpleTupleBuffer;

/**
 * A factory of {@link TupleBuffer}s
 * @author gpothier
 */
public abstract class TupleBufferFactory<T extends Tuple>
{
	public static final TupleBufferFactory<SimpleTuple> SIMPLE = new TupleBufferFactory<SimpleTuple>()
	{
		@Override
		public SimpleTupleBuffer create(int aSize, int aPreviousPageId, int aNextPageId)
		{
			return new SimpleTupleBuffer(aSize, aPreviousPageId, aNextPageId);
		}
		
		@Override
		public int getDataSize()
		{
			return 0;
		}
	};
	
	public static final TupleBufferFactory<RoleTuple> ROLE = new TupleBufferFactory<RoleTuple>()
	{
		@Override
		public RoleTupleBuffer create(int aSize, int aPreviousPageId, int aNextPageId)
		{
			return new RoleTupleBuffer(aSize, aPreviousPageId, aNextPageId);
		}
		
		@Override
		public int getDataSize()
		{
			return PageIOStream.roleSize();
		}
	};
	
	public static final TupleBufferFactory<InternalTuple> INTERNAL = new TupleBufferFactory<InternalTuple>()
	{
		@Override
		public InternalTupleBuffer create(int aSize, int aPreviousPageId, int aNextPageId)
		{
			return new InternalTupleBuffer(aSize, aPreviousPageId, aNextPageId);
		}
		
		@Override
		public int getDataSize()
		{
			return PageIOStream.internalTupleDataSize();
		}
	};
	
	public static final TupleBufferFactory<ObjectPointerTuple> OBJECT_POINTER = new TupleBufferFactory<ObjectPointerTuple>()
	{

		@Override
		public ObjectPointerTupleBuffer create(int aSize, int aPreviousPageId, int aNextPageId)
		{
			return new ObjectPointerTupleBuffer(aSize, aPreviousPageId, aNextPageId);
		}

		@Override
		public int getDataSize()
		{
			return PageIOStream.pagePointerSize()+PageIOStream.pageOffsetSize();
		}
	};
	
	/**
	 * Creates a new buffer.
	 */
	public abstract TupleBuffer<T> create(int aSize, int aPreviousPageId, int aNextPageId);

	/**
	 * Returns the size of extra tuple data.
	 */
	public abstract int getDataSize();
	

}