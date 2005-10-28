/*
 * Created on Nov 25, 2004
 */
package reflex.lib.logging.miner.impl.sql.tables;

import java.util.ArrayList;
import java.util.List;


/**
 * @author gpothier
 */
public abstract class Table 
{
	private String itsName;
	
	public Table(String aName)
	{
		itsName = aName;
	}

	/**
	 * Setup of column indices.
	 */
	protected static Column[] initColumns(Column... aColumns)
	{
		int i=1;
		for (Column theColumn : aColumns) theColumn.setIndex(i++);
		return aColumns;
	}

	public final String getName()
	{
		return itsName;
	}
	
	public abstract Column[] getColumns();
	
	/**
	 * Returns all columns that are not identity.
	 */
	public Column[] getNonIdentityColumns()
	{
		Column[] theColumns = getColumns();
		Column theIdentityColumn = getIdentityColumn();
		List<Column> theResult = new ArrayList<Column>();
		
		for (int i = 0; i < theColumns.length; i++)
		{
			Column theColumn = theColumns[i];
			if (theColumn != theIdentityColumn) theResult.add (theColumn);
		}
		
		return theResult.toArray(new Column[0]);
	}
	
	public Column[] getPrimaryKeys()
	{
		return null;
	}
	
	public Column getIdentityColumn()
	{
		return null;
	}
	
	public final boolean isPrimaryKey (Column aColumn)
	{
		Column[] thePrimaryKeys = getPrimaryKeys();
		if (thePrimaryKeys != null) for (Column theColumn : thePrimaryKeys) 
		{
			if (theColumn == aColumn) return true;
		}
		return false;
	}
	
	public final boolean isIdentity (Column aColumn)
	{
		return getIdentityColumn() == aColumn;
	}
	
	public String toString()
	{
		return getName();
	}
	
}
