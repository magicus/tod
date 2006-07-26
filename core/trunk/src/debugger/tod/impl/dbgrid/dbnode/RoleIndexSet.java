/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;

/**
 * An index set where index tuples have associated roles
 * @author gpothier
 */
public class RoleIndexSet<K> extends IndexSet<K, RoleIndexSet.Tuple>
{
	public static final byte ROLE_BEHAVIOR_ANY = 0;
	public static final byte ROLE_BEHAVIOR_CALLED = 1;
	public static final byte ROLE_BEHAVIOR_EXECUTED = 2;
	
	public static final byte ROLE_OBJECT_TARGET = -1;
	public static final byte ROLE_OBJECT_VALUE = -2;
	public static final byte ROLE_OBJECT_RESULT = -3;
	public static final byte ROLE_OBJECT_EXCEPTION = -4;
	
	public static final TupleCodec TUPLE_CODEC = new TupleCodec();
	
	public RoleIndexSet(PagedFile aFile)
	{
		super(aFile);
	}

	@Override
	protected HierarchicalIndex<Tuple> createIndex(PagedFile aFile)
	{
		return new HierarchicalIndex<Tuple>(aFile, TUPLE_CODEC);
	}

	private static class TupleCodec extends HierarchicalIndex.TupleCodec<Tuple>
	{

		@Override
		public int getTupleSize()
		{
			return StdIndexSet.TUPLE_CODEC.getTupleSize() + 8;
		}

		@Override
		public Tuple read(IntBitStruct aBitStruct)
		{
			return new Tuple(aBitStruct);
		}
		
	}
	
	public static class Tuple extends StdIndexSet.Tuple
	{
		private byte itsRole;

		public Tuple(long aTimestamp, long aEventPointer, byte aRole)
		{
			super(aTimestamp, aEventPointer);
			itsRole = aRole;
		}

		public Tuple(IntBitStruct aBitStruct)
		{
			super(aBitStruct);
			itsRole = (byte) aBitStruct.readInt(8);
		}
		
		@Override
		public void writeTo(IntBitStruct aBitStruct)
		{
			super.writeTo(aBitStruct);
			aBitStruct.writeInt(getRole(), 8);
		}
		
		public byte getRole()
		{
			return itsRole;
		}
		

	}

}
