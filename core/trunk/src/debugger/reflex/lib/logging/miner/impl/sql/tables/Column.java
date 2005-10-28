/*
 * Created on Nov 25, 2004
 */
package reflex.lib.logging.miner.impl.sql.tables;

/**
 * Describes a table column.
 * @author gpothier
 */
public class Column
{
	private final Table itsTable;
	
	/**
	 * The 1-based index of the column within its table.
	 */
	private int itsIndex;
	
	private final String itsName;
	private final String itsTypeName;
	private final int itsType;
	
	public Column(Table aTable, String aName, String aTypeName, int aType)
	{
		itsTable = aTable;
		itsName = aTable.getName()+"-"+aName;
		itsTypeName = aTypeName;
		itsType = aType;
	}
	
	public String toString()
	{
		return getName();
	}
	
	public int getIndex()
	{
		return itsIndex;
	}
	
	void setIndex(int aIndex)
	{
		itsIndex = aIndex;
	}
	
	public String getName()
	{
		return itsName;
	}
	
	public Table getTable()
	{
		return itsTable;
	}
	
	public int getType()
	{
		return itsType;
	}
	
	public String getTypeName()
	{
		return itsTypeName;
	}
}
