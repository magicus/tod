/*
 * Created on May 17, 2005
 */
package reflex.lib.logging.miner.impl.sql.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import reflex.lib.logging.miner.impl.sql.tables.Column;
import reflex.lib.logging.miner.impl.sql.tables.Table;


/**
 * Backend implementation for MySQL database.
 * @author gpothier
 */
public class PostgreSQLBackend extends AbstractBackend
{
	public PostgreSQLBackend() throws SQLException, ClassNotFoundException
	{
		this ("miner", "miner", "miner");
	}
	
	public PostgreSQLBackend(String aDatabaseName, String aUsername, String aPassword) throws SQLException, ClassNotFoundException
	{
		super (createConnection(aDatabaseName, aUsername, aPassword));
	}
	
	private static Connection createConnection (String aDatabaseName, String aUsername, String aPassword) throws SQLException, ClassNotFoundException	
	{
		Class.forName("org.postgresql.Driver" );
		return DriverManager.getConnection("jdbc:postgresql:"+aDatabaseName, aUsername, aPassword);
	}

	public String requote(String aQuery)
	{
		StringBuilder theBuilder = new StringBuilder(aQuery.length());
		int theSize = aQuery.length();
		
		for (int i=0;i<theSize;i++)
		{
			char c = aQuery.charAt(i);
			if (c == '\'') c = '"';
			else if (c == '`') c = '\'';
			theBuilder.append(c);
		}
		
		return theBuilder.toString();
	} 
	
	/**
	 * We override this method in order to handle queries with no results
	 */
	public synchronized ResultSet executeQuery (Statement aStatement, String aQuery) throws SQLException
	{
		String theQuery = requote(aQuery);
		System.out.println("Executing query: "+theQuery);
		if (aStatement.execute(theQuery)) return aStatement.getResultSet();
		else return null;
	}


	
	private String getIdentitySequenceName (Table aTable)
	{
		Column theIdentityColumn = aTable.getIdentityColumn();
		if (theIdentityColumn != null) 
			return ""+aTable+"-seq-"+theIdentityColumn;
		else throw new RuntimeException("No identity column in table: "+aTable);
	}
	
	public synchronized void createTable(Table aTable) throws SQLException
	{
		dropTable(aTable);
		
		Column theIdentityColumn = aTable.getIdentityColumn();
		String theIdentitySequenceName = null;
		if (theIdentityColumn != null) 
		{
			theIdentitySequenceName = getIdentitySequenceName(aTable);
			createSequence(theIdentitySequenceName);
		}
		
		StringBuffer theBuffer = new StringBuffer("CREATE TABLE '");
		theBuffer.append(aTable);
		theBuffer.append("' (");
		
		boolean theFirst = true;
		for (Column theColumn : aTable.getColumns())
		{
			if (! theFirst) theBuffer.append(", ");
			else theFirst = false;
			
			theBuffer.append("'").append(theColumn).append("' ").append(theColumn.getTypeName());
			if (aTable.isIdentity(theColumn)) theBuffer.append(" DEFAULT "+getNextValueSyntax(theIdentitySequenceName));
		}
		
		if (aTable.getPrimaryKeys() != null)
		{
			theBuffer.append(", PRIMARY KEY (");
			theFirst = true;
			for (Column theColumn : aTable.getPrimaryKeys())
			{
				if (! theFirst) theBuffer.append(", ");
				else theFirst = false;
				
				theBuffer.append("'").append(theColumn).append("'");
			}
			
			theBuffer.append(')');
		}
		
		theBuffer.append(");");
		executeUpdate(theBuffer.toString());
	}
	
	public long getCurrentIdentityValue(Table aTable) throws SQLException
	{
		return getCurrentValue(getIdentitySequenceName(aTable));
	}

	public void dropTable(String aTableName) throws SQLException
	{
		try
		{
			executeUpdate("DROP TABLE '"+aTableName+"';");
		}
		catch (SQLException e)
		{
			if (e.getMessage().indexOf("does not exist") < 0) throw e;
		}
	}

	public synchronized void createSequence(String aName) throws SQLException
	{
		dropSequence(aName);
		executeUpdate("CREATE SEQUENCE '"+aName+"' MINVALUE 0");
	}

	public void dropSequence(String aName) throws SQLException
	{
		try
		{
			executeUpdate("DROP SEQUENCE '"+aName+"'");
		}
		catch (SQLException e)
		{
			if (e.getMessage().indexOf("does not exist") < 0) throw e;
		}
	}

	public synchronized long getNextValue(String aSequenceName) throws SQLException
	{
		ResultSet theResultSet = executeQuery("SELECT "+getNextValueSyntax(aSequenceName));
		theResultSet.next();
		return theResultSet.getLong(1);
	}
	
	public String getNextValueSyntax(String aSequenceName) 
	{
		return "nextval(`'"+aSequenceName+"'`)";
	}

	public synchronized long getCurrentValue(String aSequenceName) throws SQLException
	{
		ResultSet theResultSet = executeQuery("SELECT "+getCurrentValueSyntax(aSequenceName));
		theResultSet.next();
		return theResultSet.getLong(1);
	}
	
	public String getCurrentValueSyntax(String aSequenceName) 
	{
		return "currval(`'"+aSequenceName+"'`)";
	}
	
}
