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
package tod.impl.evdb1.db.file;

import tod.impl.evdb1.db.file.HardPagedFile.Page;
import zz.utils.bit.BitStruct;

/**
 * A tuple is a kind of record of a fixed length (determined by
 * {@link TupleCodec#getTupleSize()}) that can be stored in a {@link Page}.
 * @author gpothier
 */
public abstract class Tuple
{
	/**
	 * Writes a serialized representation of this tuple to
	 * the specified struct.
	 * Subclasses should override to serialize additional attributes,
	 * and call super first.
	 */
	public void writeTo(BitStruct aBitStruct)
	{
	}
	
	/**
	 * Indicates the number of bits required to serialize this tuple.
	 */
	public int getBitCount()
	{
		return 0;
	}
	
	/**
	 * Indicates if this tuple is null.
	 * A null tuple typically indicates the end of a page.
	 */
	public final boolean isNull() {return false;}


}