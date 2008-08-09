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


import tod.core.database.browser.IEventPredicate;
import tod.impl.database.AbstractFilteredBidiIterator;
import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.evdbng.db.EventList;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.file.SimpleTuple;

/**
 * Represents a condition that filters events based on a predicate
 * @author gpothier
 */
public class PredicateCondition extends SimpleCondition<SimpleTuple>
{
	private static final long serialVersionUID = 2132857394221092337L;
	private final EventCondition<SimpleTuple> itsBaseCondition;
	private final IEventPredicate itsPredicate;


	public PredicateCondition(EventCondition<SimpleTuple> aBaseCondition, IEventPredicate aPredicate)
	{
		itsBaseCondition = aBaseCondition;
		itsPredicate = aPredicate;
	}

	
	@Override
	public IBidiIterator<SimpleTuple> createTupleIterator(
			final EventList aEventList, 
			Indexes aIndexes, 
			long aEventId)
	{
		IBidiIterator<SimpleTuple> theBaseIterator = itsBaseCondition.createTupleIterator(aEventList, aIndexes, aEventId);
		
		return new AbstractFilteredBidiIterator<SimpleTuple, SimpleTuple>(theBaseIterator)
		{
			@Override
			protected Object transform(SimpleTuple aInput)
			{
				long theEventId = aInput.getKey();
				assert theEventId < Integer.MAX_VALUE;
				
				GridEvent theEvent = aEventList.getEvent((int) theEventId);
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
