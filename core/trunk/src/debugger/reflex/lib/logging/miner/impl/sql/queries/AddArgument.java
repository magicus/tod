/*
 * Created on Nov 24, 2004
 */
package reflex.lib.logging.miner.impl.sql.queries;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.backend.InsertHelper;
import reflex.lib.logging.miner.impl.sql.tables.Arguments;

/**
 * A request that adds arguments to the arguments table
 * @author gpothier
 */
public class AddArgument extends AbstractQuery
{
	private InsertHelper itsInsertHelper;
	
	public AddArgument(Queries aQueries) throws SQLException
	{
		super (aQueries);
		itsInsertHelper = getBackend().createInsertStatement(Arguments.TABLE, Arguments.TABLE.getNonIdentityColumns());
	}
	
	/**
	 * Inserts a new group of argument rows sharing the same id
	 * @return The assigned id.
	 */
	public long insert (Object[] aArguments) throws SQLException
	{
		if (aArguments.length == 0) return -1;
		
		long theId = getNextId();
		
		int theIndex = 0;
		for (Object theObject : aArguments)
			insertValue(theId, theIndex++, theObject);
		
		itsInsertHelper.executeBatch();
		return theId;
	}

	/**
	 * Inserts a new single argument rows
	 * @return The assigned id.
	 */
	public long insert (Object aArgument) throws SQLException
	{
		long theId = getNextId();
		
		insertValue(theId, -1, aArgument);
		
		return theId;
	}

	/**
	 * Determines the next id for the arguments table.
	 */
	private long getNextId() throws SQLException
	{
		return getBackend().getNextValue(Queries.ARGUMENT_IDS_SEQUENCE);
	}

	/**
	 * Creates a row for the specified value
	 */
	private void insertValue (long aId, int aIndex, Object aValue) throws SQLException
	{
		itsInsertHelper.setLong(Arguments.ID, aId);
		itsInsertHelper.setInt(Arguments.INDEX, aIndex);
		
		getQueries().insertValue(
				itsInsertHelper, 
				Arguments.TYPE, 
				Arguments.OBJECT_ID, 
				aValue);
		
		itsInsertHelper.addBatch();
	}
}
