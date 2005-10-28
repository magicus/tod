/*
 * Created on May 15, 2005
 */
package reflex.lib.logging.miner.impl.sql.tables;

import java.sql.Types;

public class Values extends Table
{
	public static final Values TABLE = new Values();
	
	public static final Column ID = new Column (TABLE, "id", "BIGINT", Types.BIGINT);
	public static final Column VALUE = new Column (TABLE, "value", "TEXT", Types.BLOB);
		
	public static final Column[] COLUMNS = initColumns(ID, VALUE);
	public static final Column[] PRIMARY_KEYS = {ID};
	
	private Values ()
	{
		super ("objValues");
	}
	
	public Column[] getColumns()
	{
		return COLUMNS;
	}
	
	public Column[] getPrimaryKeys()
	{
		return PRIMARY_KEYS;
	}
	
	public Column getIdentityColumn()
	{
		return ID;
	}

}