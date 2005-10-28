/*
 * Created on Nov 26, 2004
 */
package reflex.lib.logging.miner.impl.sql.filters;

import reflex.lib.logging.miner.impl.sql.tables.Column;

/**
 * A filter that poduces a condition on a single column
 * @author gpothier
 */
public class ColumnFilter extends SQLFilter
{
	private Column itsColumn;
	private String itsValueString;
	
	public ColumnFilter(Column aColumn, String aValueString)
	{
		itsColumn = aColumn;
		itsValueString = aValueString;
	}
	
	public String getSQLCondition()
	{
		return "('"+itsColumn+"'="+itsValueString+")";
	}
}
