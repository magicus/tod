/*
 * Created on May 17, 2005
 */
package reflex.lib.logging.miner.impl.sql.backend;

import java.sql.SQLException;
import java.sql.Statement;

import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.tables.Column;
import reflex.lib.logging.miner.impl.sql.tables.Table;

public class SQLUtils
{
	/**
	 * Gegneric method to create a table for backends that support
	 * auto-increment columns
	 */
	public static String createTable_autoincrement (
			Table aTable, 
			String aAutoIncrementSyntax) 
	{
		StringBuffer theBuffer = new StringBuffer("CREATE TABLE '");
		theBuffer.append(aTable);
		theBuffer.append("' (");
		
		boolean theFirst = true;
		for (Column theColumn : aTable.getColumns())
		{
			if (! theFirst) theBuffer.append(", ");
			else theFirst = false;
			
			theBuffer.append("'").append(theColumn).append("' ").append(theColumn.getTypeName());
			if (aTable.isIdentity(theColumn)) theBuffer.append(" "+aAutoIncrementSyntax);
		}
		
		Column[] thePrimaryKeys = aTable.getPrimaryKeys();
		if (thePrimaryKeys != null)
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
		
		return theBuffer.toString();
		
	}


}
