/*
 * Created on May 24, 2005
 */
package reflex.lib.logging.miner.impl.sql.backend;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import reflex.lib.logging.miner.impl.sql.tables.Column;
import reflex.lib.logging.miner.impl.sql.tables.ColumnMapper;

/**
 * helper for insert statements: contains a prepared statement and a column mapper.
 * @author gpothier
 */
public class InsertHelper
{
	public final PreparedStatement itsStatement;
	public final ColumnMapper itsMapper;
	
	public InsertHelper(PreparedStatement aStatement, ColumnMapper aMapper)
	{
		itsStatement = aStatement;
		itsMapper = aMapper;
	}

	public ColumnMapper getMapper()
	{
		return itsMapper;
	}

	public PreparedStatement getStatement()
	{
		return itsStatement;
	}
	
	public boolean execute() throws SQLException
	{
		return itsStatement.execute();
	}
	
	public void addBatch() throws SQLException
	{
		itsStatement.addBatch();
	}
	
	public int[] executeBatch() throws SQLException 
	{
		return itsStatement.executeBatch();
	}
	
	public void setNull (Column aColumn) throws SQLException
	{
		itsStatement.setNull(itsMapper.indexOf(aColumn), aColumn.getType());
	}
	
	public void setString (Column aColumn, String aValue) throws SQLException
	{
		itsStatement.setString(itsMapper.indexOf(aColumn), aValue);
	}
		
	public void setByte (Column aColumn, byte aValue) throws SQLException
	{
		itsStatement.setByte(itsMapper.indexOf(aColumn), aValue);
	}
	
	public void setInt (Column aColumn, int aValue) throws SQLException
	{
		itsStatement.setInt(itsMapper.indexOf(aColumn), aValue);
	}
	
	public void setLong (Column aColumn, long aValue) throws SQLException
	{
		itsStatement.setLong(itsMapper.indexOf(aColumn), aValue);
	}
	
}
