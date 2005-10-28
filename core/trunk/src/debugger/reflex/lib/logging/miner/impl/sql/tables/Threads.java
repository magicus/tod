/*
 * Created on Nov 25, 2004
 */
package reflex.lib.logging.miner.impl.sql.tables;

import java.sql.Types;

/**
 * @author gpothier
 */
public class Threads extends Table
{
	public static final Threads TABLE = new Threads();
	
	public static final Column ID = new Column (TABLE, "id", "BIGINT", Types.BIGINT);
	public static final Column THREAD_NAME = new Column (TABLE, "name", "VARCHAR(50)", Types.VARCHAR);
	
	public static final Column[] COLUMNS = initColumns(ID, THREAD_NAME);
	public static final Column[] PRIMARY_KEYS = {ID};
	
	private Threads()
	{
		super ("threads");
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
