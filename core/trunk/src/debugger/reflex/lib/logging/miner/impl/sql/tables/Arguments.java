/*
 * Created on Nov 25, 2004
 */
package reflex.lib.logging.miner.impl.sql.tables;

import java.sql.Types;

/**
 * @author gpothier
 */
public class Arguments extends Table
{
	public static final Arguments TABLE = new Arguments();
	
	public static final Column ID = new Column (TABLE, "id", "BIGINT", Types.BIGINT);
	public static final Column INDEX = new Column (TABLE, "index", "INTEGER", Types.INTEGER);
	
	public static final Column TYPE = new Column (TABLE, "type", "SMALLINT", Types.SMALLINT);
	public static final Column OBJECT_ID = new Column (TABLE, "objectId", "BIGINT", Types.BIGINT);
	
	public static final Column[] COLUMNS = initColumns(ID, INDEX, TYPE, OBJECT_ID);
	public static final Column[] PRIMARY_KEYS = {ID, INDEX};
	
	private Arguments ()
	{
		super ("arguments");
	}
	
	public Column[] getColumns()
	{
		return COLUMNS;
	}

	
	public Column[] getPrimaryKeys()
	{
		return PRIMARY_KEYS;
	}
}