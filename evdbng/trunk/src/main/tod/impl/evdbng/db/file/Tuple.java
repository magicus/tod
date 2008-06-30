/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
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