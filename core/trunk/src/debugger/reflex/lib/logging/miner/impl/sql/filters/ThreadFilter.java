/*
 * Created on Nov 26, 2004
 */
package reflex.lib.logging.miner.impl.sql.filters;

import reflex.lib.logging.miner.impl.sql.tables.Events;
import tod.core.model.structure.ThreadInfo;

/**
 * A filter that accepts only events of a given thread
 * @author gpothier
 */
public class ThreadFilter extends ColumnFilter
{
	public ThreadFilter (ThreadInfo aThreadInfo)
	{
		super (Events.THREAD_ID, ""+aThreadInfo.getId());
	}
}
