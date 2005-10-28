/*
 * Created on May 17, 2005
 */
package reflex.lib.logging.miner.impl.sql.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import reflex.lib.logging.miner.impl.sql.tables.Column;
import reflex.lib.logging.miner.impl.sql.tables.Table;


/**
 * Backend implementation for MySQL database.
 * @author gpothier
 */
public class MySQLBackend extends AbstractBackend
{
	public MySQLBackend() throws SQLException, ClassNotFoundException
	{
		this ("miner", "miner", "miner");
	}
	
	public MySQLBackend(String aDatabaseName, String aUsername, String aPassword) throws SQLException, ClassNotFoundException
	{
		super (createConnection(aDatabaseName, aUsername, aPassword));
	}
	
	private static Connection createConnection (String aDatabaseName, String aUsername, String aPassword) throws SQLException, ClassNotFoundException	
	{
		Class.forName("com.mysql.jdbc.Driver" );
		return DriverManager.getConnection("jdbc:mysql://localhost/"+aDatabaseName, aUsername, aPassword);
	}

	public String requote(String aQuery)
	{
		StringBuilder theBuilder = new StringBuilder(aQuery.length());
		int theSize = aQuery.length();
		
		boolean theInQuote = false;
		for (int i=0;i<theSize;i++)
		{
			char c = aQuery.charAt(i);
			
			if (theInQuote)
			{
				if (c == '\'') theInQuote = false;
				else
				{
					if (c == '-') c = '_';
					c = Character.toUpperCase(c);
					theBuilder.append(c);
				}
			}
			else
			{
				if (c == '\'') theInQuote = true;
				else theBuilder.append(c);
			}
		}
		
		return theBuilder.toString();
	} 
	
	public synchronized void createTable(Table aTable) throws SQLException
	{
		dropTable(aTable);
		
		StringBuffer theBuffer = new StringBuffer("CREATE TABLE '");
		theBuffer.append(aTable);
		theBuffer.append("' (");
		
		boolean theFirst = true;
		for (Column theColumn : aTable.getColumns())
		{
			if (! theFirst) theBuffer.append(", ");
			else theFirst = false;
			
			theBuffer.append("'").append(theColumn).append("' ").append(theColumn.getTypeName());
			if (aTable.isIdentity(theColumn)) theBuffer.append(" AUTO_INCREMENT");
		}
		
		Set<Column> thePrimaryKeys = new HashSet<Column>();
		if (aTable.getPrimaryKeys() != null) thePrimaryKeys.addAll(Arrays.asList(aTable.getPrimaryKeys()));
		if (aTable.getIdentityColumn() != null) thePrimaryKeys.add(aTable.getIdentityColumn());
		
		if (! thePrimaryKeys.isEmpty())
		{
			theBuffer.append(", PRIMARY KEY (");
			theFirst = true;
			for (Column theColumn : thePrimaryKeys)
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

	public synchronized void createSequence(String aName) throws SQLException
	{
		dropSequence(aName);
		executeUpdate("CREATE TABLE '"+aName+"' (id BIGINT NOT NULL);");
		executeUpdate("INSERT INTO '"+aName+"' VALUES (0)");
	}

	public void dropSequence(String aName) throws SQLException
	{
		dropTable(aName);
	}

	public synchronized long getNextValue(String aSequenceName) throws SQLException
	{
		executeUpdate("UPDATE '"+aSequenceName+"' SET id=LAST_INSERT_ID(id+1);");
		ResultSet theResultSet = executeQuery("SELECT LAST_INSERT_ID();");
		theResultSet.next();
		return theResultSet.getLong(1);
	}
	
	public synchronized String getNextValueSyntax(String aSequenceName) throws SQLException
	{
		throw new UnsupportedOperationException();
	}
	
	public void dropTable(String aTableName) throws SQLException
	{
		executeUpdate("DROP TABLE IF EXISTS '"+aTableName+"';");
	}

	public long getCurrentValue(String aSequenceName) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public String getCurrentValueSyntax(String aSequenceName) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public long getCurrentIdentityValue(Table aTable) throws SQLException
	{
		throw new UnsupportedOperationException();
	}
	
	
}
