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


import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.db.EventList;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on the advice source id of an event
 * @author gpothier
 */
public class AdviceSourceIdCondition extends SimpleCondition
{
	private static final long serialVersionUID = 3724400547911298424L;
	
	private int itsAdviceSourceId;

	public AdviceSourceIdCondition(int aAdviceSourceId)
	{
		itsAdviceSourceId = aAdviceSourceId;
	}

	@Override
	public IBidiIterator<StdTuple> createTupleIterator(EventList aEventList, Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.getAdviceSourceIdIndex(itsAdviceSourceId).getTupleIterator(aTimestamp);
	}

	public boolean _match(GridEvent aEvent)
	{
		return aEvent.getProbeInfo().adviceSourceId == itsAdviceSourceId;
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("Advice source id = %d", itsAdviceSourceId);
	}

}
