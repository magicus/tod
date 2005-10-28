/*
 * Created on Nov 24, 2004
 */
package reflex.lib.logging.miner.impl.sql.queries;

import java.sql.SQLException;

import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.backend.ISQLBackend;
import reflex.lib.logging.miner.impl.sql.tables.Arguments;
import reflex.lib.logging.miner.impl.sql.tables.Events;
import reflex.lib.logging.miner.impl.sql.tables.Locations;
import reflex.lib.logging.miner.impl.sql.tables.Threads;
import reflex.lib.logging.miner.impl.sql.tables.Values;

/**
 * Initializes an SQL database so that it can be used by {@link reflex.lib.logging.miner.impl.sql.SQLCollector}
 * @author gpothier
 */
public class DatabaseInit extends AbstractQuery
{
	public DatabaseInit(Queries aQueries)
	{
		super(aQueries);
	}
	
	public void init() throws SQLException
	{
		init(getBackend());
	}
	
	public static void init(ISQLBackend aBackend) throws SQLException
	{
		aBackend.createSequence(Queries.ARGUMENT_IDS_SEQUENCE);
		
		aBackend.createTable(Events.TABLE);
		aBackend.createTable(Arguments.TABLE);
		aBackend.createTable(Locations.TABLE);
		aBackend.createTable(Threads.TABLE);
		aBackend.createTable(Values.TABLE);
	}
}
