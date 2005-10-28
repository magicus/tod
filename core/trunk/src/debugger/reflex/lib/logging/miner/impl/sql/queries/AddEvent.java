/*
 * Created on Nov 24, 2004
 */
package reflex.lib.logging.miner.impl.sql.queries;

import reflex.lib.logging.miner.impl.sql.EventType;
import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.backend.ISQLBackend;
import reflex.lib.logging.miner.impl.sql.backend.InsertHelper;
import reflex.lib.logging.miner.impl.sql.tables.ColumnMapper;
import reflex.lib.logging.miner.impl.sql.tables.Events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A request that adds events to the events table.
 * @author gpothier
 */
public class AddEvent extends AbstractQuery
{
	private InsertHelper itsInsertHelper;
	
	public AddEvent(Queries aQueries) throws SQLException
	{
		super(aQueries);
		ISQLBackend theBackend = getBackend();
		itsInsertHelper = theBackend.createInsertStatement(Events.TABLE, Events.TABLE.getNonIdentityColumns());
	}
	
	public void insert (
			EventType aType,
			long aTimestamp, 
			long aSerial,
			long aThreadId,
			int aDepth,
			byte aLocationType,
			int aLocationId,
			Object aTarget,
			long aArgId) throws SQLException
	{
		itsInsertHelper.setByte(Events.TYPE, (byte) aType.ordinal());
		itsInsertHelper.setLong(Events.TIMESTAMP, aTimestamp);
		itsInsertHelper.setLong(Events.SERIAL, aSerial);
		itsInsertHelper.setLong(Events.THREAD_ID, aThreadId);
		itsInsertHelper.setInt(Events.DEPTH, aDepth);
		itsInsertHelper.setByte(Events.LOCATION_TYPE, aLocationType);
		itsInsertHelper.setInt(Events.LOCATION_ID, aLocationId);
		
		getQueries().insertValue(
				itsInsertHelper,
				Events.TARGET_TYPE, 
				Events.TARGET_ID,
				aTarget);
		
		itsInsertHelper.setLong(Events.ARG_ID, aArgId);
		
		itsInsertHelper.execute();
	}
}
