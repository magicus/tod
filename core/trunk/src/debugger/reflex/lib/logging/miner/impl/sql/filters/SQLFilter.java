/*
 * Created on Nov 26, 2004
 */
package reflex.lib.logging.miner.impl.sql.filters;

import tod.core.model.trace.IEventFilter;

/**
 * @author gpothier
 */
public abstract class SQLFilter implements IEventFilter
{
	/**
	 * Returns an expression that will be placed in an SQL 'WHERE' clause.
	 */
	public abstract String getSQLCondition();
}
