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
package tod.impl.evdb1.db;

import tod.impl.evdb1.db.IndexSet.IndexManager;
import tod.impl.evdb1.db.file.HardPagedFile;
import tod.impl.evdb1.db.file.IndexTuple;
import tod.impl.evdb1.db.file.IndexTupleCodec;
import tod.impl.evdb1.db.file.TupleCodec;
import zz.utils.bit.BitStruct;

public class StdIndexSet extends IndexSet<StdIndexSet.StdTuple> 
{
	public static final IndexTupleCodec TUPLE_CODEC = new StdTupleCodec();
	
	public StdIndexSet(
			String aName, 
			IndexManager aIndexManager,
			HardPagedFile aFile, 
			int aIndexCount)
	{
		super(aName, aIndexManager, aFile, aIndexCount);
	}
	
	@Override
	public TupleCodec<StdTuple> getTupleCodec()
	{
		return TUPLE_CODEC;
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
		
		public void set(long aTimestamp, long aEventPointer)
		{
			super.set(aTimestamp);
			itsEventPointer = aEventPointer;
		}

		@Override
		public void writeTo(BitStruct aBitStruct)
		{
			super.writeTo(aBitStruct);
			aBitStruct.writeLong(getEventPointer(), 64);
		}
		
		@Override
		public int getBitCount()
		{
			return super.getBitCount() + 64;
		}
		
		public long getEventPointer()
		{
			return itsEventPointer;
		}
		
		@Override
		public boolean equals(Object aObj)
		{
			if (aObj instanceof StdTuple)
			{
				StdTuple theOther = (StdTuple) aObj;
				return theOther.getEventPointer() == getEventPointer();
			}
			else return false;
		}
	}

}
