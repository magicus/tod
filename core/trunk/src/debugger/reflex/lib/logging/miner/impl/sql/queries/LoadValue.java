package reflex.lib.logging.miner.impl.sql.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.tables.Values;

/**
 * This query permits to load values from the values table.
 */
public class LoadValue extends AbstractQuery
{
	private PreparedStatement itsStatement;
	
	public LoadValue(Queries aQueries) throws SQLException
	{
		super(aQueries);
		itsStatement = getBackend().prepareStatement(
				"SELECT * FROM '"+Values.TABLE+"' " 
				+"WHERE '"+Values.ID+"' = ? ");
	}
	
	/**
	 * Loads the value for the specified id.
	 */
	public String loadValue(long aId) throws SQLException
	{
		itsStatement.setLong(1, aId);
		ResultSet theResultSet = itsStatement.executeQuery();
		if (theResultSet.next())
		{
			String theValue = theResultSet.getString(Values.VALUE.getIndex());
			return theValue;
		}
		else throw new RuntimeException("No value for id:"+aId);
	}

}
