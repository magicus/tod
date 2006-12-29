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
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.AbstractFilteredBidiIterator;
import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.RoleIndexSet;
import tod.impl.dbgrid.db.RoleIndexSet.RoleTuple;
import tod.impl.dbgrid.db.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;
import zz.utils.AbstractFilteredIterator;

/**
 * Represents a condition on an object, with a corresponding role.
 * @author gpothier
 */
public class ObjectCondition extends SimpleCondition
{
	private static final long serialVersionUID = 4506201457044007004L;
	private int itsObjectId;
	private byte itsRole;
	
	public ObjectCondition(int aObjectId, byte aRole)
	{
		itsObjectId = aObjectId;
		itsRole = aRole;
	}

	@Override
	public BidiIterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		BidiIterator<RoleIndexSet.RoleTuple> theTupleIterator = 
			aIndexes.getObjectIndex(itsObjectId).getTupleIterator(aTimestamp);
		if (itsRole == RoleIndexSet.ROLE_OBJECT_ANY)
		{
			theTupleIterator = new AbstractFilteredBidiIterator<RoleTuple, RoleTuple>(theTupleIterator)
			{
				@Override
				protected Object transform(RoleTuple aIn)
				{
					return aIn;
				}
			};
		}
		else if (itsRole == RoleIndexSet.ROLE_OBJECT_ANYARG)
		{
			theTupleIterator = new AbstractFilteredBidiIterator<RoleTuple, RoleTuple>(theTupleIterator)
			{
				@Override
				protected Object transform(RoleTuple aIn)
				{
					return aIn.getRole() >= 0 ? aIn : REJECT;
				}
			};
		}
		else
		{
			theTupleIterator = RoleIndexSet.createFilteredIterator(theTupleIterator, itsRole);
		}
		
		return (BidiIterator) theTupleIterator;
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.matchObjectCondition(itsObjectId, itsRole);
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("ObjectId = %d (role %d)", itsObjectId, itsRole);
	}

}
