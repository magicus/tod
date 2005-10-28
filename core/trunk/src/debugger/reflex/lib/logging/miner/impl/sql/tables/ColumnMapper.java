/*
 * Created on May 24, 2005
 */
package reflex.lib.logging.miner.impl.sql.tables;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps columns to indices. This can be used to process queries whose column set is smaller than  the
 * table's full column set.
 * @author gpothier
 */
public class ColumnMapper
{
	private Map<Column, Integer> itsMap = new HashMap<Column, Integer>();
	
	/**
	 * Initializes the mapper.
	 */
	public ColumnMapper(Column[] aColumns)
	{
		for (int i = 0; i < aColumns.length; i++)
		{
			Column theColumn = aColumns[i];
			itsMap.put (theColumn, i+1);
		}
	}
	
	/**
	 * Returns the 1-based index of the given column in the intitial array.
	 * @return The column's index
	 */
	public int indexOf (Column aColumn)
	{
		Integer theIndex = itsMap.get(aColumn);
		if (theIndex == null) throw new IllegalArgumentException("Column not found: "+aColumn);
		return theIndex;
	}
}
