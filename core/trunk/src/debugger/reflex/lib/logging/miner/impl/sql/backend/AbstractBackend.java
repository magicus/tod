/*
 * Created on May 17, 2005
 */
package reflex.lib.logging.miner.impl.sql.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import reflex.lib.logging.miner.impl.sql.tables.Column;
import reflex.lib.logging.miner.impl.sql.tables.ColumnMapper;
import reflex.lib.logging.miner.impl.sql.tables.Table;
import reflex.lib.pciobject.IPCIObject;

public abstract class AbstractBackend implements ISQLBackend
{
	private final Connection itsConnection;
	
	/**
	 * A statement that can be used to perform arbitrary requests.
	 */
	private Statement itsStatement;
	
	public AbstractBackend(Connection aConnection)
	{
		itsConnection = aConnection;
		
		try
		{
			itsStatement = getConnection().createStatement();
		}
		catch (SQLException e)
		{
			throw new RuntimeException("SQL Exception catched while initializing statements", e);
		}
	}

	protected Connection getConnection()
	{
		return itsConnection;
	}
	
	public Statement createStatement() throws SQLException
	{
		return itsConnection.createStatement();
	}

	public Statement createStatement(int aResultSetType, int aResultSetConcurrency) throws SQLException
	{
		return itsConnection.createStatement(aResultSetType, aResultSetConcurrency);
	}

	public PreparedStatement prepareStatement (String aQuery) throws SQLException
	{
		return getConnection().prepareStatement(requote(aQuery));
	}

	public void dropTable(Table aTable) throws SQLException
	{
		dropTable(aTable.getName());
	}
	
	public ResultSet executeQuery (Statement aStatement, String aQuery) throws SQLException
	{
		String theQuery = requote(aQuery);
		System.out.println("Executing query: "+theQuery);
		return aStatement.executeQuery(theQuery);
	}

	public int executeUpdate (Statement aStatement, String aQuery) throws SQLException
	{
		String theQuery = requote(aQuery);
		System.out.println("Executing update: "+theQuery);
		return aStatement.executeUpdate(theQuery);
	}
	
	public ResultSet executeQuery (String aQuery) throws SQLException
	{
		synchronized (itsStatement)
		{
			return executeQuery(itsStatement, aQuery);
		}
	}
	
	public int executeUpdate (String aQuery) throws SQLException
	{
		synchronized (itsStatement)
		{
			return executeUpdate(itsStatement, aQuery);
		}
	}

	public InsertHelper createInsertStatement(Table aTable, Column[] aColumns) throws SQLException
	{
		StringBuilder theBuilder1 = new StringBuilder();
		StringBuilder theBuilder2 = new StringBuilder();
		
		for (int i=0;i<aColumns.length;i++) 
		{
			if (i != 0) 
			{
				theBuilder1.append(", ");
				theBuilder2.append(", ");
			}
			theBuilder1.append("'"+aColumns[i].getName()+"'");
			theBuilder2.append("?");
		}

		
		String theQuery = String.format (
				"INSERT INTO '"+aTable+"' (%s) VALUES (%s)",
				theBuilder1,
				theBuilder2);
		
		return new InsertHelper(
				prepareStatement(theQuery),
				new ColumnMapper(aColumns));
	}
}
