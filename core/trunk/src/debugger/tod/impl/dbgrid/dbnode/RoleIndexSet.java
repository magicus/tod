/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.HierarchicalIndex.IndexTupleCodec;

import zz.utils.AbstractFilteredIterator;
import zz.utils.bit.BitStruct;

/**
 * An index set where index tuples have associated roles
 * @author gpothier
 */
public class RoleIndexSet extends IndexSet<RoleIndexSet.RoleTuple>
{
	public static final byte ROLE_BEHAVIOR_ANY = 0;
	public static final byte ROLE_BEHAVIOR_CALLED = 1;
	public static final byte ROLE_BEHAVIOR_EXECUTED = 2;
	public static final byte ROLE_BEHAVIOR_EXIT = 3;
	
	public static final byte ROLE_OBJECT_TARGET = -1;
	public static final byte ROLE_OBJECT_VALUE = -2;
	public static final byte ROLE_OBJECT_RESULT = -3;
	public static final byte ROLE_OBJECT_EXCEPTION = -4;
	
	public static final TupleCodec TUPLE_CODEC = new RoleTupleCodec();
	
	public RoleIndexSet(String aName, PagedFile aFile, int aIndexCount)
	{
		super(aName, aFile, aIndexCount);
	}
	
	@Override
	protected HierarchicalIndex<RoleTuple> createIndex(String aName, PagedFile aFile)
	{
		return new HierarchicalIndex<RoleTuple>(aName, aFile, TUPLE_CODEC);
	}
	
	/**
	 * Creates an iterator that filters out the tuples from a source iterator that
	 * don't have the specified role.
	 */
	public static Iterator<RoleTuple> createFilteredIterator(Iterator<RoleTuple> aIterator, final byte aRole)
	{
		return new AbstractFilteredIterator<RoleTuple, RoleTuple>(aIterator)
		{
			@Override
			protected Object transform(RoleTuple aIn)
			{
				return aIn.getRole() == aRole ? aIn : REJECT;
			}
		};
	}
	
	/**
	 * Creates an iterator that filters out duplicate tuples, which is useful when the 
	 * role is not checked: for instance if a behavior call event has the same called
	 * and executed method, it would appear twice in the behavior index with
	 * a different role.
	 */
	public static Iterator<RoleTuple> createFilteredIterator(Iterator<RoleTuple> aIterator)
	{
		return new DuplicateFilterIterator(aIterator);
	}
	
	private static class DuplicateFilterIterator extends AbstractFilteredIterator<RoleTuple, RoleTuple>
	{
		private long itsLastEventPointer;
		
		public DuplicateFilterIterator(Iterator<RoleTuple> aIterator)
		{
			super(aIterator);
		}
		
		@Override
		protected void init()
		{
			itsLastEventPointer = -1;
		}

		@Override
		protected Object transform(RoleTuple aIn)
		{
			if (aIn.getEventPointer() == itsLastEventPointer) return REJECT;
			itsLastEventPointer = aIn.getEventPointer();
			
			return aIn;
		}
	}
	

	private static class RoleTupleCodec extends IndexTupleCodec<RoleTuple>
	{

		@Override
		public int getTupleSize()
		{
			return StdIndexSet.TUPLE_CODEC.getTupleSize() + 8;
		}

		@Override
		public RoleTuple read(BitStruct aBitStruct)
		{
			return new RoleTuple(aBitStruct);
		}
	}
	
	public static class RoleTuple extends StdIndexSet.StdTuple
	{
		private byte itsRole;

		public RoleTuple(long aTimestamp, long aEventPointer, int aRole)
		{
			super(aTimestamp, aEventPointer);
			if (aRole > Byte.MAX_VALUE) throw new RuntimeException("Role overflow");
			itsRole = (byte) aRole;
		}

		public RoleTuple(BitStruct aBitStruct)
		{
			super(aBitStruct);
			itsRole = (byte) aBitStruct.readInt(8);
		}
		
		@Override
		public void writeTo(BitStruct aBitStruct)
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
