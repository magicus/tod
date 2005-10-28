/*
 * Created on Nov 26, 2004
 */
package reflex.lib.logging.miner.impl.sql.filters;

import reflex.lib.logging.miner.impl.sql.EventType;
import reflex.lib.logging.miner.impl.sql.tables.Events;

/**
 * A filter that accepts only events of a specific type.
 * It defines constant instance for the existing event types.
 * @author gpothier
 */
public class EventTypeFilter extends ColumnFilter
{
	public static final EventTypeFilter BEHAVIOUR_ENTER = new EventTypeFilter (EventType.BEHAVIOUR_ENTER);
	public static final EventTypeFilter BEHAVIOUR_EXIT = new EventTypeFilter (EventType.BEHAVIOUR_EXIT);
	public static final EventTypeFilter FIELD_WRITE = new EventTypeFilter (EventType.FIELD_WRITE);
	public static final EventTypeFilter INSTANTIATION = new EventTypeFilter (EventType.INSTANTIATION);
	
	private EventTypeFilter(EventType aEventType)
	{
		super(Events.TYPE, ""+aEventType.ordinal());
	}
}
