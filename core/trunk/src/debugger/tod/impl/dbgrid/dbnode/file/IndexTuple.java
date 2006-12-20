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
package tod.impl.dbgrid.dbnode.file;

import static tod.impl.dbgrid.DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
import zz.utils.bit.BitStruct;

/**
 * Base class for all index tuples. Only contains the timestamp.
 */
public class IndexTuple extends Tuple
{
	private long itsTimestamp;

	public IndexTuple(long aTimestamp)
	{
		itsTimestamp = aTimestamp;
	}
	
	public IndexTuple(BitStruct aBitStruct)
	{
		itsTimestamp = aBitStruct.readLong(EVENT_TIMESTAMP_BITS);
	}

	protected void set(long aTimestamp)
	{
		itsTimestamp = aTimestamp;
	}
	
	/**
	 * Writes a serialized representation of this tuple to
	 * the specified struct.
	 * Subclasses should override to serialize additional attributes,
	 * and call super first.
	 */
	public void writeTo(BitStruct aBitStruct)
	{
		aBitStruct.writeLong(getTimestamp(), EVENT_TIMESTAMP_BITS);
	}
	
	@Override
	public int getBitCount()
	{
		return super.getBitCount() + EVENT_TIMESTAMP_BITS;
	}
	
	/**
	 * Returns the timestamp of this tuple.
	 */
	public long getTimestamp()
	{
		return itsTimestamp;
	}
	
	@Override
	public String toString()
	{
		return String.format("%s: t=%d",
				getClass().getSimpleName(),
				getTimestamp());
	}
}