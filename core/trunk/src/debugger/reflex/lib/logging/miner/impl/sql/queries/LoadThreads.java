package reflex.lib.logging.miner.impl.sql.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.tables.Threads;
import tod.core.ILocationRegistrer;

/**
 * This query permits to reload thrad information stored in the locations table into
 * a location registrer
 */
public class LoadThreads extends AbstractQuery
{

	public LoadThreads(Queries aQueries)
	{
		super(aQueries);
	}

	public void load(ILocationRegistrer aRegistrer) throws SQLException
	{
		ResultSet theResultSet = getBackend().executeQuery("SELECT * from '" + Threads.TABLE + "'");
		while (theResultSet.next())
		{
			long theId = theResultSet.getLong(Threads.ID.getIndex());
			String theName = theResultSet.getString(Threads.THREAD_NAME.getIndex());

			aRegistrer.registerThread(theId, theName);
		}
	}
}
