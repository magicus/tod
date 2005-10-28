/*
 * Created on May 17, 2005
 */
package reflex.lib.logging.miner.impl.sql.backend;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import reflex.lib.logging.miner.impl.sql.tables.Column;
import reflex.lib.logging.miner.impl.sql.tables.Table;


/**
 * REpresents a SQL backend (MySQL, Postgres, etc)
 * @author gpothier
 */
public interface ISQLBackend
{
	/**
	 * Properly quotes the given query string for use by this backend.
	 * @param aQuery A query with single quotes.
	 */
	public String requote(String aQuery);
	
	/**
	 * Creates a new statement with default attributes.
	 */
	public Statement createStatement () throws SQLException;

	/**
	 * Creates a new statement with custom attributes.
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	public Statement createStatement (int resultSetType, int resultSetConcurrency) throws SQLException;
	
	/**
	 * Creates a prepared statement using this backend's current connection, and requoting the 
	 * given query.
	 */
	public PreparedStatement prepareStatement (String aQuery) throws SQLException;

	
	/**
	 * Executes the given query after requoting it.
	 * @see #requote(String)
	 */
	public ResultSet executeQuery (Statement aStatement, String aQuery) throws SQLException;
	
	/**
	 * Executes the given query after requoting it.
	 * @see #requote(String)
	 */
	public int executeUpdate (Statement aStatement, String aQuery) throws SQLException;
	
	/**
	 * Executes the given query after requoting it.
	 * @see #requote(String)
	 */
	public ResultSet executeQuery (String aQuery) throws SQLException;
	
	/**
	 * Executes the given query after requoting it.
	 * @see #requote(String)
	 */
	public int executeUpdate (String aQuery) throws SQLException;
	
	/**
	 * Creates the table described by the specified table object.
	 * If the table already exists, it is recreated.
	 */
	public void createTable (Table aTable) throws SQLException;
	
	/**
	 * Drops the specified table.
	 */
	public void dropTable (Table aTable) throws SQLException;
	
	/**
	 * Drops the specified table.
	 */
	public void dropTable (String aTableName) throws SQLException;
	
	/**
	 * Returns the current value of the identity column for the given table.
	 */
	public long getCurrentIdentityValue(Table aTable) throws SQLException;
	
	/**
	 * Creates a sequence with the given name. If thet sequence already exists, it is cleared.
	 */
	public void createSequence (String aName) throws SQLException;
	
	/**
	 * Drops the given sequence.
	 */
	public void dropSequence (String aName) throws SQLException;
	
	/**
	 * Returns the next value of the sequence.
	 */
	public long getNextValue (String aSequenceName) throws SQLException;
	
	/**
	 * Returns the syntax to use to obtain the next value of a sequence.
	 */
	public String getNextValueSyntax(String aSequenceName) throws SQLException;
	
	/**
	 * Returns the current value of the sequence.
	 */
	public long getCurrentValue (String aSequenceName) throws SQLException;
	
	/**
	 * Returns the syntax to use to obtain the current value of a sequence.
	 */
	public String getCurrentValueSyntax(String aSequenceName) throws SQLException;

	/**
	 * Creates a prepared insert statement for the specified columns of the specified table.
	 */
	public InsertHelper createInsertStatement (Table aTable, Column[] aColumns) throws SQLException;
	
	
}
