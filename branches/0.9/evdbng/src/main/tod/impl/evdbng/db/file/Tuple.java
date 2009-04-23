/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.evdbng.db.file;

import tod.impl.evdbng.db.file.PagedFile.Page;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;

/**
 * A tuple is a kind of record of a fixed length (determined by
 * {@link TupleCodec#getTupleSize()}) that can be stored in a {@link Page}.
 * @author gpothier
 */
public abstract class Tuple
{
	private long itsKey;

	public Tuple(long aKey)
	{
		itsKey = aKey;
	}

	public long getKey()
	{
		return itsKey;
	}

	/**
	 * Indicates if this tuple is null.
	 * A null tuple typically indicates the end of a page.
	 */
	public final boolean isNull() {return false;}

	@Override
	public boolean equals(Object aObj)
	{
		if (aObj instanceof Tuple)
		{
			Tuple theOther = (Tuple) aObj;
			return theOther.getKey() == getKey();
		}
		else return false;
	}

}