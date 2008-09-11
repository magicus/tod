package tod.impl.evdbng.db;

import tod.impl.evdbng.messages.GridEventNG;

/**
 * Read-only interface for {@link EventList}
 * @author gpothier
 *
 */
public interface IEventList
{
	public GridEventNG getEvent(int aId);

}
