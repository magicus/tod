package reflex.lib.logging.miner.impl.sql.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.tables.Arguments;

/**
 * This query permits to load arguments.
 */
public class LoadArguments extends AbstractQuery
{
	private PreparedStatement itsStatement;
	
	public LoadArguments(Queries aQueries) throws SQLException
	{
		super(aQueries);
		itsStatement = getBackend().prepareStatement(
				"SELECT * FROM '"+Arguments.TABLE+"' " 
				+"WHERE '"+Arguments.ID+"' = ? "
				+"ORDER BY '"+Arguments.INDEX+"'");
	}
	
	/**
	 * Loads an array of arguments for the specified id.
	 */
	public Object[] loadArguments(long aId) throws SQLException
	{
		List<Object> theValues = new ArrayList<Object>(); 
		
		itsStatement.setLong(1, aId);
		ResultSet theResultSet = itsStatement.executeQuery();
		
		while (theResultSet.next())
		{
			byte theType = theResultSet.getByte(Arguments.TYPE.getIndex());
			long theObjectId = theResultSet.getLong(Arguments.OBJECT_ID.getIndex());
			
			Object theValue = getQueries().decodeValue(theType, theObjectId);
			theValues.add (theValue);
		}
		
		return theValues.toArray();
	}

	/**
	 * Loads exactly one argument. If there is not exactly one argument, an exception is thrown
	 */
	public Object loadArgument(long aId) throws SQLException
	{
		Object[] theArguments = loadArguments(aId);
		if (theArguments.length != 1) throw new RuntimeException(String.format (
				"Expected one argument for id %d, got %d",
				aId,
				theArguments.length));
		
		return theArguments[0];
	}
	
}
