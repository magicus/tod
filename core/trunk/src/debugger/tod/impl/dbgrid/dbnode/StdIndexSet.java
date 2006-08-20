/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.dbnode.file.IndexTuple;
import tod.impl.dbgrid.dbnode.file.IndexTupleCodec;
import tod.impl.dbgrid.dbnode.file.TupleCodec;
import zz.utils.bit.BitStruct;

public class StdIndexSet extends IndexSet<StdIndexSet.StdTuple> 
{
	public static final TupleCodec TUPLE_CODEC = new StdTupleCodec();
	
	public StdIndexSet(String aName, HardPagedFile aFile, int aIndexCount)
	{
		super(aName, aFile, aIndexCount);
	}
	
	@Override
	protected HierarchicalIndex<StdTuple> createIndex(String aName, HardPagedFile aFile)
	{
		return new HierarchicalIndex<StdTuple>(aName, aFile, TUPLE_CODEC);
	}

	public static class StdTupleCodec extends IndexTupleCodec<StdTuple>
	{
		@Override
		public int getTupleSize()
		{
			return super.getTupleSize() + 64;
		}

		@Override
		public StdTuple read(BitStruct aBitStruct)
		{
			return new StdTuple(aBitStruct);
		}
	}
	
	public static class StdTuple extends IndexTuple
	{
		/**
		 * Internal event pointer
		 */
		private long itsEventPointer;

		public StdTuple(long aTimestamp, long aEventPointer)
		{
			super(aTimestamp);
			itsEventPointer = aEventPointer;
		}
		
		public StdTuple(BitStruct aBitStruct)
		{
			super(aBitStruct);
			itsEventPointer = aBitStruct.readLong(64);
		}

		@Override
		public void writeTo(BitStruct aBitStruct)
		{
			super.writeTo(aBitStruct);
			aBitStruct.writeLong(getEventPointer(), 64);
		}
		
		public long getEventPointer()
		{
			return itsEventPointer;
		}
	}

}
