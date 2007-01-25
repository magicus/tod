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


import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on event type.
 * @author gpothier
 */
public class TypeCondition extends SimpleCondition
{
	private static final long serialVersionUID = 5860441411500604107L;
	private MessageType itsType;

	public TypeCondition(MessageType aType)
	{
		itsType = aType;
	}

	@Override
	public BidiIterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.getTypeIndex(itsType.ordinal()).getTupleIterator(aTimestamp);
	}

	@Override
	public boolean _match(GridEvent aEvent)
	{
		return aEvent.getEventType() == itsType;
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("Event type = %s", itsType);
	}

}
