/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.queries;

import tod.impl.evdbng.db.file.Tuple;

/**
 * Represents a simple clause condition.
 * @author gpothier
 */
public abstract class SimpleCondition<T extends Tuple> extends EventCondition<T>
{
	@Override
	public int getClausesCount()
	{
		return 1;
	}
}
