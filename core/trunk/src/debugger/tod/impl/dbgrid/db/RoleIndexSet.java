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
package tod.impl.dbgrid.db;

import tod.impl.dbgrid.AbstractFilteredBidiIterator;
import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.db.StdIndexSet.StdTuple;
import tod.impl.dbgrid.db.file.HardPagedFile;
import tod.impl.dbgrid.db.file.IndexTupleCodec;
import tod.impl.dbgrid.db.file.TupleCodec;
import zz.utils.bit.BitStruct;

/**
 * An index set where index tuples have associated roles
 * @author gpothier
 */
public class RoleIndexSet extends IndexSet<RoleIndexSet.RoleTuple>
{
	/**
	 * Represents any of the behavior roles.
	 */
	public static final byte ROLE_BEHAVIOR_ANY = 0;
	
	/**
	 * Represents either {@link #ROLE_BEHAVIOR_CALLED} or {@link #ROLE_BEHAVIOR_EXECUTED}.
	 */
	public static final byte ROLE_BEHAVIOR_ANY_ENTER = 1;
	
	public static final byte ROLE_BEHAVIOR_CALLED = 2;
	public static final byte ROLE_BEHAVIOR_EXECUTED = 3;
	public static final byte ROLE_BEHAVIOR_EXIT = 4;
	public static final byte ROLE_BEHAVIOR_OPERATION = 5;
	
	
	public static final byte ROLE_OBJECT_TARGET = -1;
	public static final byte ROLE_OBJECT_VALUE = -2;
	public static final byte ROLE_OBJECT_RESULT = -3;
	public static final byte ROLE_OBJECT_EXCEPTION = -4;
	public static final byte ROLE_OBJECT_ANYARG = -5;
	public static final byte ROLE_OBJECT_ANY = -6;
	
	public static final IndexTupleCodec<RoleTuple> TUPLE_CODEC = new RoleTupleCodec();
	
	public RoleIndexSet(String aName, HardPagedFile aFile, int aIndexCount)
	{
		super(aName, aFile, aIndexCount);
	}
	
	@Override
	public TupleCodec<RoleTuple> getTupleCodec()
	{
		return TUPLE_CODEC;
	}

	/**
	 * Creates an iterator that filters out the tuples from a source iterator that
	 * don't have one of the specified roles.
	 */
	public static BidiIterator<RoleTuple> createFilteredIterator(
			BidiIterator<RoleTuple> aIterator,
			final byte... aRole)
	{
		return new AbstractFilteredBidiIterator<RoleTuple, RoleTuple>(aIterator)
		{
			@Override
			protected Object transform(RoleTuple aIn)
			{
				byte theRole = aIn.getRole();
				for (byte theAllowedRole : aRole)
				{
					if (theRole == theAllowedRole) return aIn;
				}
				return REJECT;
			}
		};
	}
	
	/**
	 * Creates an iterator that filters out duplicate tuples, which is useful when the 
	 * role is not checked: for instance if a behavior call event has the same called
	 * and executed method, it would appear twice in the behavior index with
	 * a different role.
	 */
	public static BidiIterator<RoleTuple> createFilteredIterator(
			BidiIterator<RoleTuple> aIterator)
	{
		return new DuplicateFilterIterator(aIterator);
	}
	
	private static class DuplicateFilterIterator extends AbstractFilteredBidiIterator<RoleTuple, RoleTuple>
	{
		private long itsLastEventPointer;
		private int itsDirection = 0;
		
		public DuplicateFilterIterator(BidiIterator<RoleTuple> aIterator)
		{
			super(aIterator);
			itsLastEventPointer = -1;
		}
		
		@Override
		protected RoleTuple fetchNext()
		{
			if (itsDirection != 1) itsLastEventPointer = -1;
			itsDirection = 1;
			return super.fetchNext();
		}
		
		@Override
		protected RoleTuple fetchPrevious()
		{
			if (itsDirection != -1) itsLastEventPointer = -1;
			itsDirection = -1;
			return super.fetchPrevious();
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
		
		public void set(long aTimestamp, long aEventPointer, int aRole)
		{
			super.set(aTimestamp, aEventPointer);
			itsRole = (byte) aRole;
		}
		
		@Override
		public void writeTo(BitStruct aBitStruct)
		{
			super.writeTo(aBitStruct);
			aBitStruct.writeInt(getRole(), 8);
		}
		
		@Override
		public int getBitCount()
		{
			return super.getBitCount() + 8;
		}
		
		public byte getRole()
		{
			return itsRole;
		}
		
		@Override
		public boolean equals(Object aObj)
		{
			if (aObj instanceof RoleTuple)
			{
				RoleTuple theOther = (RoleTuple) aObj;
				return theOther.getEventPointer() == getEventPointer()
						&& theOther.getRole() == getRole();
			}
			else if (aObj instanceof StdTuple)
			{
				StdTuple theOther = (StdTuple) aObj;
				return theOther.getEventPointer() == getEventPointer();
			}
			else return false;
		}

	}
}
