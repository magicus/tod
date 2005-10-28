/*
 * Created on Nov 25, 2004
 */
package reflex.lib.logging.miner.impl.sql.tables;

import java.sql.Types;

/**
 * Description of the events table.
 * @author gpothier
 */
public class Events extends Table
{
	public static final Events TABLE = new Events();
	
	public static final Column ID = new Column (TABLE, "id", "BIGINT", Types.BIGINT);
	
	public static final Column TYPE = new Column (TABLE, "type", "SMALLINT", Types.SMALLINT);
	
	public static final Column TIMESTAMP = new Column (TABLE, "timestamp", "BIGINT", Types.BIGINT) ;
	public static final Column SERIAL = new Column (TABLE, "serial", "BIGINT", Types.BIGINT);
	public static final Column THREAD_ID = new Column (TABLE, "threadId", "BIGINT", Types.BIGINT);
	
	public static final Column DEPTH = new Column (TABLE, "depth", "INTEGER", Types.INTEGER);
	
	public static final Column LOCATION_TYPE = new Column (TABLE, "locationType", "SMALLINT", Types.SMALLINT);
	public static final Column LOCATION_ID = new Column (TABLE, "locationId", "INTEGER", Types.INTEGER);
	
	public static final Column TARGET_TYPE = new Column (TABLE, "targetType", "SMALLINT", Types.SMALLINT);
	public static final Column TARGET_ID = new Column (TABLE, "targetId", "BIGINT", Types.BIGINT);
	
	public static final Column ARG_ID = new Column (TABLE, "argId", "BIGINT", Types.BIGINT);
	
	private static final Column[] COLUMNS = initColumns(
		ID, TYPE, TIMESTAMP, SERIAL, THREAD_ID, DEPTH, 
		LOCATION_TYPE, LOCATION_ID, 
		TARGET_TYPE, TARGET_ID,
		ARG_ID);
	
	private Events()
	{
		super ("events");
	}
	
	public Column[] getColumns()
	{
		return COLUMNS;
	}
	
	public Column getIdentityColumn()
	{
		return ID;
	}
}
