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
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on event host.
 * @author gpothier
 */
public class HostCondition extends SimpleCondition
{
	private static final long serialVersionUID = -6783805983938219464L;
	private int itsHost;

	public HostCondition(int aHost)
	{
		itsHost = aHost;
	}

	@Override
	public BidiIterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.getHostIndex(itsHost).getTupleIterator(aTimestamp);
	}

	@Override
	public boolean _match(GridEvent aEvent)
	{
		return aEvent.getHost() == itsHost;
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("HostId = %d", itsHost);
	}

}
