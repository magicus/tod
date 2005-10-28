package reflex.lib.logging.miner.impl.sql.queries;

import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.tables.Locations;
import tod.core.BehaviourType;
import tod.core.ILocationRegistrer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This query permits to reload locations stored in the locations table into
 * a location registrer
 */
public class LoadLocations extends AbstractQuery
{
	public LoadLocations(Queries aQueries)
	{
		super(aQueries);
	}

	public void load(ILocationRegistrer aRegistrer) throws SQLException
	{
		ResultSet theResultSet = getBackend().executeQuery("SELECT * from '" + Locations.TABLE + "'");
		while (theResultSet.next())
		{
			int theId = theResultSet.getInt(Locations.ID.getIndex());
			byte theType = theResultSet.getByte(Locations.TYPE.getIndex());
			byte theSubType = theResultSet.getByte(Locations.SUBTYPE.getIndex());
			String theName = theResultSet.getString(Locations.LOCATION_NAME.getIndex());
			int theTypeId = theResultSet.getInt(Locations.TYPE_ID.getIndex());

			switch (theType)
			{
				case Queries.LOCATION_TYPE_BEHAVIOUR:
					loadBehaviour(aRegistrer, theId, theSubType, theName, theTypeId);
					break;

				case Queries.LOCATION_TYPE_FIELD:
					loadField(aRegistrer, theId, theName, theTypeId);
					break;

				case Queries.LOCATION_TYPE_TYPE:
					loadType(aRegistrer, theId, theName, -1, null);
					break;
			}
		}
	}

	private void loadBehaviour (ILocationRegistrer aRegistrer, int aId, byte aSubtype, String aName, int aTypeId)
	{
		BehaviourType theType = null;

		switch (aSubtype)
		{
			case Queries.BEHAVIOUR_SUBTYPE_CONSTRUCTOR:
				theType = BehaviourType.CONSTRUCTOR;
				break;

			case Queries.BEHAVIOUR_SUBTYPE_METHOD:
				theType = BehaviourType.METHOD;
				break;

			case Queries.BEHAVIOUR_SUBTYPE_STATICBLOCK:
				theType = BehaviourType.STATIC_BLOCK;
				break;

			case Queries.BEHAVIOUR_SUBTYPE_STATICMETHOD:
				theType = BehaviourType.STATIC_METHOD;
				break;
		}

		aRegistrer.registerBehavior(theType, aId, aTypeId, aName);
	}

	private void loadField (ILocationRegistrer aRegistrer, int aId, String aName, int aTypeId)
	{
		aRegistrer.registerField(aId, aTypeId, aName);
	}

	private void loadType (ILocationRegistrer aRegistrer, int aId, String aName, int aSupertypeId, int[] aInterfaceIds)
	{
		aRegistrer.registerType(aId, aName, aSupertypeId, aInterfaceIds);
	}
}
