/*
 * Created on Nov 24, 2004
 */
package reflex.lib.logging.miner.impl.sql.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.tables.Values;

/**
 * A request that adds a value to the values table
 * @author gpothier
 */
public class AddValue extends AbstractQuery
{
	private PreparedStatement itsInsertStatement;
	
	public AddValue(Queries aQueries) throws SQLException
	{
		super(aQueries);
		itsInsertStatement = getBackend().prepareStatement(
				"INSERT INTO '"+Values.TABLE+"' ('"+Values.VALUE+"') VALUES (?)");
	}

	public long addValue (String aName) throws SQLException
	{
		itsInsertStatement.setString(1, aName);
		itsInsertStatement.execute();
		
		return getBackend().getCurrentIdentityValue(Values.TABLE);
	}
	
}
