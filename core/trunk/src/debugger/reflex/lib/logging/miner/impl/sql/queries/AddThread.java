/*
 * Created on Nov 24, 2004
 */
package reflex.lib.logging.miner.impl.sql.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.tables.Threads;

/**
 * A request that adds locations to the locations table
 * @author gpothier
 */
public class AddThread extends AbstractQuery
{
	private PreparedStatement itsInsertStatement;
	
	public AddThread(Queries aQueries) throws SQLException
	{
		super(aQueries);
		itsInsertStatement = getBackend().prepareStatement(
				"INSERT INTO '"+Threads.TABLE+"' VALUES (?, ?)");
	}
	

	public void addThread (long aId, String aName) throws SQLException
	{
		int i=1;
		itsInsertStatement.setLong(i++, aId);
		itsInsertStatement.setString(i++, aName);
		
		itsInsertStatement.execute();
	}
	
}
