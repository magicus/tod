/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on behavior id and corresponding role.
 * @author gpothier
 */
public class BehaviorCondition extends SimpleCondition
{
	private static final long serialVersionUID = -9029772284148605574L;
	private int itsBehaviorId;
	private byte itsRole;
	
	public BehaviorCondition(int aBehaviorId, byte aRole)
	{
		itsBehaviorId = aBehaviorId;
		itsRole = aRole;
	}
	
	@Override
	public BidiIterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		BidiIterator<RoleIndexSet.RoleTuple> theTupleIterator = aIndexes.behaviorIndex.getIndex(itsBehaviorId).getTupleIterator(aTimestamp);
		switch (itsRole)
		{
		case RoleIndexSet.ROLE_BEHAVIOR_ANY:
			theTupleIterator = RoleIndexSet.createFilteredIterator(theTupleIterator);
			break;
			
		case RoleIndexSet.ROLE_BEHAVIOR_ANY_ENTER:
			theTupleIterator = RoleIndexSet.createFilteredIterator(theTupleIterator, RoleIndexSet.ROLE_BEHAVIOR_CALLED, RoleIndexSet.ROLE_BEHAVIOR_EXECUTED);
			break;
			
		default:
			theTupleIterator = RoleIndexSet.createFilteredIterator(theTupleIterator, itsRole);
		}
		
		return (BidiIterator) theTupleIterator;
	}
	
	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.matchBehaviorCondition(itsBehaviorId, itsRole);
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("BehaviorId = %d (role %d)", itsBehaviorId, itsRole);
	}
}
