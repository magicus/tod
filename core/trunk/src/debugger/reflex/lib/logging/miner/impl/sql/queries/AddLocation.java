/*
 * Created on Nov 24, 2004
 */
package reflex.lib.logging.miner.impl.sql.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.tables.Locations;
import tod.core.BehaviourType;

/**
 * A request that adds locations to the locations table
 * @author gpothier
 */
public class AddLocation extends AbstractQuery
{
	private PreparedStatement itsInsertStatement;
	
	public AddLocation(Queries aQueries) throws SQLException
	{
		super(aQueries);
		itsInsertStatement = getBackend().prepareStatement(
				"INSERT INTO '"+Locations.TABLE+"' VALUES (?, ?, ?, ?, ?)");
	}
	

	/**
	 * Low-level insertion method.
	 */
	private void insertLocation (int aId, byte aType, byte aSubtype, String aName, int aTypeId) throws SQLException
	{
		int i=1;
		itsInsertStatement.setInt(i++, aId);
		itsInsertStatement.setByte(i++, aType);
		itsInsertStatement.setByte(i++, aSubtype);
		itsInsertStatement.setString(i++, aName);
		itsInsertStatement.setInt(i++, aTypeId);
		
		itsInsertStatement.execute();
	}
	
	public void addBehaviour(BehaviourType aBehaviourType, int aBehaviourId, int aTypeId, String aName) throws SQLException
	{
		byte theSubtype = -1;
		switch (aBehaviourType)
		{
			case METHOD:
				theSubtype = Queries.BEHAVIOUR_SUBTYPE_METHOD;
				break;
				
			case CONSTRUCTOR:
				theSubtype = Queries.BEHAVIOUR_SUBTYPE_CONSTRUCTOR;
				break;
				
			case STATIC_METHOD:
				theSubtype = Queries.BEHAVIOUR_SUBTYPE_STATICMETHOD;
				break;
				
			case STATIC_BLOCK:
				theSubtype = Queries.BEHAVIOUR_SUBTYPE_STATICBLOCK;
				break;
				
				
		}
		insertLocation(aBehaviourId, Queries.LOCATION_TYPE_BEHAVIOUR, theSubtype, aName, aTypeId);
	}
	
	public void addField(int aFieldId, int aTypeId, String aName) throws SQLException
	{
		insertLocation(aFieldId, Queries.LOCATION_TYPE_FIELD, (byte)-1, aName, aTypeId);
	}
	
	public void addType(int aTypeId, String aName) throws SQLException
	{
		insertLocation(aTypeId, Queries.LOCATION_TYPE_TYPE, (byte)-1, aName, 0);
	}
}
