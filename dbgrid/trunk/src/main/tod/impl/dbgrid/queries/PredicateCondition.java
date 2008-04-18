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


import tod.core.database.browser.IEventPredicate;
import tod.impl.database.AbstractFilteredBidiIterator;
import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.db.EventList;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition that filters events based on a predicate
 * @author gpothier
 */
public class PredicateCondition extends SimpleCondition
{
	private static final long serialVersionUID = 2132857394221092337L;
	private final EventCondition itsBaseCondition;
	private final IEventPredicate itsPredicate;


	public PredicateCondition(EventCondition aBaseCondition, IEventPredicate aPredicate)
	{
		itsBaseCondition = aBaseCondition;
		itsPredicate = aPredicate;
	}

	@Override
	public IBidiIterator<StdTuple> createTupleIterator(
			final EventList aEventList, 
			Indexes aIndexes, 
			long aTimestamp)
	{
		IBidiIterator<StdTuple> theBaseIterator = itsBaseCondition.createTupleIterator(aEventList, aIndexes, aTimestamp);
		
		return new AbstractFilteredBidiIterator<StdTuple, StdTuple>(theBaseIterator)
		{
			@Override
			protected Object transform(StdTuple aInput)
			{
				GridEvent theEvent = aEventList.getEvent(aInput.getEventPointer());
				return itsPredicate.match(theEvent.toLogEvent(null)) ? aInput : REJECT;
			}
		};
	}

	@Override
	public boolean _match(GridEvent aEvent)
	{
		return itsPredicate.match(aEvent.toLogEvent(null));
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("Predicate = %s on %s", itsPredicate, itsBaseCondition);
	}

}
