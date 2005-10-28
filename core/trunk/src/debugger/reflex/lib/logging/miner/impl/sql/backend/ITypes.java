/*
 * Created on May 18, 2005
 */
package reflex.lib.logging.miner.impl.sql.backend;

/**
 * Permits to obtain SQL types compatible with a given backend.
 * @author gpothier
 */
public interface ITypes
{
	public SQLType integer();
	public SQLType bigint();
	public SQLType smallint();
	public SQLType tinyint();
	
	public SQLType varchar(int aSize);
	public SQLType text();
	
	
	public static class SQLType
	{
		/**
		 * Name of the type in SQL syntax.
		 */
		public final String itsTypeName;
		
		/**
		 * SQL type id (from {@link java.sql.Types}
		 */
		public final int itsTypeId;

		public SQLType(String aName, int aId)
		{
			itsTypeName = aName;
			itsTypeId = aId;
		}
	}
}
