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
package tod.impl.evdbng.queries;


import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.evdbng.db.EventList;
import tod.impl.evdbng.db.file.SimpleTuple;

/**
 * Represents a condition on the advice source id of an event
 * @author gpothier
 */
public class AdviceSourceIdCondition extends SimpleCondition<SimpleTuple>
{
	private static final long serialVersionUID = 3724400547911298424L;
	
	private int itsAdviceSourceId;

	public AdviceSourceIdCondition(int aAdviceSourceId)
	{
		itsAdviceSourceId = aAdviceSourceId;
	}
	
	@Override
	public IBidiIterator<SimpleTuple> createTupleIterator(EventList aEventList, tod.impl.evdbng.db.Indexes aIndexes, long aEventId)
	{
		return aIndexes.getAdviceSourceIdIndex(itsAdviceSourceId).getTupleIterator(aEventId);
	}

	@Override
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
