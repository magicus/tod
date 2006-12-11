/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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

import zz.utils.bit.BitStruct;

/**
 * A tuple codec is able to serialized and deserialize tuples in a {@link BitStruct}
 * @author gpothier
 */
public abstract class TupleCodec<T>
{
	/**
	 * Returns the size (in bits) of each tuple.
	 */
	public abstract int getTupleSize();
	
	/**
	 * Reads a tuple from the given struct.
	 */
	public abstract T read(BitStruct aBitStruct);
	
	/**
	 * Writes the tuple to the given struct.
	 */
	public abstract void write(BitStruct aBitStruct, T aTuple);
	
	/**
	 * Indicates if a tuple is null.
	 * A null tuple typically indicates the end of a page.
	 */
	public abstract boolean isNull(T aTuple);
}