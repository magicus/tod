/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.util.Iterator;

import zz.utils.AbstractFilteredIterator;
import zz.utils.bit.BitStruct;

/**
 * An index set where index tuples have associated roles
 * @author gpothier
 */
public class RoleIndexSet extends IndexSet<RoleIndexSet.Tuple>
{
	public static final byte ROLE_BEHAVIOR_ANY = 0;
	public static final byte ROLE_BEHAVIOR_CALLED = 1;
	public static final byte ROLE_BEHAVIOR_EXECUTED = 2;
	public static final byte ROLE_BEHAVIOR_EXIT = 3;
	
	public static final byte ROLE_OBJECT_TARGET = -1;
	public static final byte ROLE_OBJECT_VALUE = -2;
	public static final byte ROLE_OBJECT_RESULT = -3;
	public static final byte ROLE_OBJECT_EXCEPTION = -4;
	
	public static final TupleCodec TUPLE_CODEC = new TupleCodec();
	
	public RoleIndexSet(String aName, PagedFile aFile, int aIndexCount)
	{
		super(aName, aFile, aIndexCount);
	}
	
	@Override
	protected HierarchicalIndex<Tuple> createIndex(String aName, PagedFile aFile)
	{
		return new HierarchicalIndex<Tuple>(aName, aFile, TUPLE_CODEC);
	}
	
	/**
	 * Creates an iterator that filters out the tuples from a source iterator that
	 * don't have the specified role.
	 */
	public static Iterator<Tuple> createFilteredIterator(Iterator<Tuple> aIterator, final byte aRole)
	{
		return new AbstractFilteredIterator<Tuple, Tuple>(aIterator)
		{
			@Override
			protected Object transform(Tuple aIn)
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
	public static Iterator<Tuple> createFilteredIterator(Iterator<Tuple> aIterator)
	{
		return new DuplicateFilterIterator(aIterator);
	}
	
	private static class DuplicateFilterIterator extends AbstractFilteredIterator<Tuple, Tuple>
	{
		private long itsLastEventPointer;
		
		public DuplicateFilterIterator(Iterator<Tuple> aIterator)
		{
			super(aIterator);
		}
		
		@Override
		protected void init()
		{
			itsLastEventPointer = -1;
		}

		@Override
		protected Object transform(Tuple aIn)
		{
			if (aIn.getEventPointer() == itsLastEventPointer) return REJECT;
			itsLastEventPointer = aIn.getEventPointer();
			
			return aIn;
		}
	}
	

	private static class TupleCodec extends HierarchicalIndex.TupleCodec<Tuple>
	{

		@Override
		public int getTupleSize()
		{
			return StdIndexSet.TUPLE_CODEC.getTupleSize() + 8;
		}

		@Override
		public Tuple read(BitStruct aBitStruct)
		{
			return new Tuple(aBitStruct);
		}
		
	}
	
	public static class Tuple extends StdIndexSet.Tuple
	{
		private byte itsRole;

		public Tuple(long aTimestamp, long aEventPointer, int aRole)
		{
			super(aTimestamp, aEventPointer);
			if (aRole > Byte.MAX_VALUE) throw new RuntimeException("Role overflow");
			itsRole = (byte) aRole;
		}

		public Tuple(BitStruct aBitStruct)
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
