/*
 * Created on Nov 25, 2004
 */
package reflex.lib.logging.miner.impl.sql.tables;

import java.sql.Types;

/**
 * @author gpothier
 */
public class Locations extends Table
{
	public static final Locations TABLE = new Locations();
	
	public static final Column ID = new Column (TABLE, "id", "INTEGER", Types.INTEGER);
	public static final Column TYPE = new Column (TABLE, "type", "SMALLINT", Types.SMALLINT);
	public static final Column SUBTYPE = new Column (TABLE, "subType", "SMALLINT", Types.SMALLINT);
	
	public static final Column LOCATION_NAME = new Column (TABLE, "name", "VARCHAR(200)", Types.VARCHAR);
	public static final Column TYPE_ID = new Column (TABLE, "typeId", "INTEGER", Types.INTEGER);
	
	public static final Column[] COLUMNS = initColumns(ID, TYPE, SUBTYPE, LOCATION_NAME, TYPE_ID);
	public static final Column[] PRIMARY_KEYS = {ID, TYPE};
	
	private Locations()
	{
		super ("locations");
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