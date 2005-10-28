/*
 * Created on May 15, 2005
 */
package reflex.lib.logging.miner.impl.sql.queries;

import java.sql.Connection;

import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.backend.ISQLBackend;

/**
 * Abstract class for implementing queries.
 * @author gpothier
 */
public class AbstractQuery
{
	private Queries itsQueries;

	public AbstractQuery(Queries aQueries)
	{
		itsQueries = aQueries;
	}

	protected Queries getQueries()
	{
		return itsQueries;
	}
	
	protected ISQLBackend getBackend()
	{
		return getQueries().getBackend();
	}
}
