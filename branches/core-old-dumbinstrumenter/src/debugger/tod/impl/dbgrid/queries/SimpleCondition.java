/*
 * Created on Aug 8, 2006
 */
package tod.impl.dbgrid.queries;

/**
 * Represents a simple clause condition.
 * @author gpothier
 */
public abstract class SimpleCondition extends EventCondition
{
	@Override
	public int getClausesCount()
	{
		return 1;
	}
}
