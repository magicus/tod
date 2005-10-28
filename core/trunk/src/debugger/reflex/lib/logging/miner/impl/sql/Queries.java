/*
 * Created on Nov 24, 2004
 */
package reflex.lib.logging.miner.impl.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import reflex.lib.logging.miner.impl.sql.backend.ISQLBackend;
import reflex.lib.logging.miner.impl.sql.backend.InsertHelper;
import reflex.lib.logging.miner.impl.sql.queries.AddArgument;
import reflex.lib.logging.miner.impl.sql.queries.AddEvent;
import reflex.lib.logging.miner.impl.sql.queries.AddLocation;
import reflex.lib.logging.miner.impl.sql.queries.AddThread;
import reflex.lib.logging.miner.impl.sql.queries.AddValue;
import reflex.lib.logging.miner.impl.sql.queries.DatabaseInit;
import reflex.lib.logging.miner.impl.sql.queries.LoadArguments;
import reflex.lib.logging.miner.impl.sql.queries.LoadLocations;
import reflex.lib.logging.miner.impl.sql.queries.LoadThreads;
import reflex.lib.logging.miner.impl.sql.queries.LoadValue;
import reflex.lib.logging.miner.impl.sql.tables.Column;
import tod.core.model.structure.ObjectId;

/**
 * Contains all the available quiery wrappers as public fields.
 * Also provides some static utility methods and constants
 * @author gpothier
 */
public class Queries
{
	public final DatabaseInit dbInit;
	public final AddEvent addEvent;
	public final AddArgument addArgument;
	public final AddLocation addLocation;
	public final AddThread addThread;
	public final AddValue addValue;
	public final LoadLocations loadLocations;
	public final LoadThreads loadThreads;
	public final LoadArguments loadArguments;
	public final LoadValue loadValue;

	public static final byte OBJECT_TYPE_NULL = 0;
	public static final byte OBJECT_TYPE_UID = 1;
	public static final byte OBJECT_TYPE_HASH = 2;
	public static final byte OBJECT_TYPE_VALUE = 3;
	
	
	public static final byte BEHAVIOUR_SUBTYPE_METHOD = 1; // Those are for the locations table
	public static final byte BEHAVIOUR_SUBTYPE_CONSTRUCTOR = 2;
	public static final byte BEHAVIOUR_SUBTYPE_STATICMETHOD = 3;
	public static final byte BEHAVIOUR_SUBTYPE_STATICBLOCK = 4;
	
	public static final byte LOCATION_TYPE_BEHAVIOUR = 1; // This is for the events table
	public static final byte LOCATION_TYPE_FIELD = 2;
	public static final byte LOCATION_TYPE_TYPE = 3;
	
	public static final String ARGUMENT_IDS_SEQUENCE = "argumentIds";
	
	private final ISQLBackend itsBackend;
	
	public Queries(ISQLBackend aBackend) throws SQLException
	{
		itsBackend = aBackend;
		
		dbInit = new DatabaseInit(this);
		addEvent = new AddEvent(this);
		addArgument = new AddArgument(this);
		addLocation = new AddLocation(this);
		addThread = new AddThread(this);
		addValue = new AddValue(this);
		loadLocations = new LoadLocations(this);
		loadThreads = new LoadThreads(this);
		loadArguments = new LoadArguments(this);
		loadValue = new LoadValue(this);
	}
	
	public ISQLBackend getBackend()
	{
		return itsBackend;
	}
	
	/**
	 * Sets column values corresponding to the given object.
	 * The columns are expected to be:
	 * <pre>
	 * type: TINYINT
	 * id: BIGINT
	 * </pre>
	 * @return The index of the next column
	 */
	public void insertValue (
			InsertHelper aHelper, 
			Column aTypeColumn, 
			Column aObjectIdColumn, 
			Object aValue) throws SQLException
	{
		if (aValue == null)
		{
			aHelper.setByte(aTypeColumn, OBJECT_TYPE_NULL);
			aHelper.setNull(aObjectIdColumn);
		}
		else if (aValue instanceof ObjectId.ObjectUID)
		{
			ObjectId.ObjectUID theObjectUID = (ObjectId.ObjectUID) aValue;
			
			aHelper.setByte(aTypeColumn, OBJECT_TYPE_UID);
			aHelper.setLong(aObjectIdColumn, theObjectUID.getId());
		}
		else if (aValue instanceof ObjectId.ObjectHash)
		{
			ObjectId.ObjectHash theObjectHash = (ObjectId.ObjectHash) aValue;
			
			aHelper.setByte(aTypeColumn, OBJECT_TYPE_HASH);
			aHelper.setLong(aObjectIdColumn, theObjectHash.getHascode());
		}
		else
		{
			long theId = addValue.addValue(""+aValue);
			aHelper.setByte(aTypeColumn, OBJECT_TYPE_VALUE);
			aHelper.setLong(aObjectIdColumn, theId);
		}
	}

	public Object decodeValue (byte aObjectType, long aObjectId) throws SQLException
	{
		switch (aObjectType)
		{
			case OBJECT_TYPE_NULL:
				return null;

			case OBJECT_TYPE_UID:
				return new ObjectId.ObjectUID(aObjectId);

			case OBJECT_TYPE_HASH:
				return new ObjectId.ObjectHash((int) aObjectId);

			case OBJECT_TYPE_VALUE:
				return loadValue.loadValue(aObjectId);

			default:
				throw new RuntimeException("Unexpected object type: "+aObjectType);
		}
	}
}
