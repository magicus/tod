/*
 * Created on Nov 25, 2004
 */
package reflex.lib.logging.miner.impl.sql;

import reflex.lib.logging.miner.api.PagedBrowser;
import reflex.lib.logging.miner.impl.common.event.*;
import reflex.lib.logging.miner.impl.sql.backend.ISQLBackend;
import reflex.lib.logging.miner.impl.sql.filters.SQLFilter;
import reflex.lib.logging.miner.impl.sql.tables.Events;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.LocationInfo;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.structure.TypeInfo;
import tod.core.model.trace.IEventBrowser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author gpothier
 */
public class SQLEventBrowser extends PagedBrowser
{
	private static final String SEQUENCE_COLUMN_NAME = "seq";
	private final SQLFilter itsFilter;
	
	private PreparedStatement itsLoadPageStatement;
	private PreparedStatement itsPrevTimestampStatement;
	private PreparedStatement itsNextTimestampStatement;
	
	
	/**
	 * Index for creating new view names.
	 */
	private static int itsViewNameIndex;
	
	private String itsTableName;
	private String itsSequenceName;

	private Statement itsStatement;
	private int itsSize;
	private SQLCollector itsCollector;

	public SQLEventBrowser(SQLCollector aCollector, SQLFilter aFilter) throws SQLException
	{
		itsCollector = aCollector;
		ISQLBackend theBackend = itsCollector.getQueries().getBackend();
		
		itsStatement = theBackend.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
		
		itsFilter = aFilter;
		itsTableName = "browser-"+itsViewNameIndex;
		itsSequenceName = "browserSequence-"+itsViewNameIndex;
		itsViewNameIndex++;
		
		theBackend.createSequence(itsSequenceName);
		theBackend.dropTable(itsTableName);
		
		theBackend.executeQuery(
				"SELECT "+theBackend.getNextValueSyntax(itsSequenceName)+" AS '"+SEQUENCE_COLUMN_NAME+"', * "
				+" INTO '"+itsTableName+"'"
				+" FROM '"+Events.TABLE+"'"
				+" WHERE "+aFilter.getSQLCondition()
				+" ORDER BY '"+Events.ID+"'");
		System.out.println("Created table "+itsTableName);
		
		theBackend.dropSequence(itsSequenceName);
		
		ResultSet theCount = theBackend.executeQuery(itsStatement, "SELECT COUNT(*) FROM '"+itsTableName+"'");
		theCount.next();
		itsSize = theCount.getInt(1);
		
		itsLoadPageStatement = theBackend.prepareStatement(
				"SELECT * FROM '"+itsTableName+"' "
				+" WHERE " 
				+"'"+SEQUENCE_COLUMN_NAME+"' >= ?"
				+" AND '"+SEQUENCE_COLUMN_NAME+"' < ?");
		
		itsNextTimestampStatement = theBackend.prepareStatement(
				"SELECT '"+SEQUENCE_COLUMN_NAME+"' FROM '"+itsTableName+"' "
				+" WHERE '"+Events.TIMESTAMP+"' >= ?"
				+" ORDER BY '"+Events.TIMESTAMP+"' ASC, '"+SEQUENCE_COLUMN_NAME+"' ASC "
				+" LIMIT 1");

		itsPrevTimestampStatement = theBackend.prepareStatement(
				"SELECT '"+SEQUENCE_COLUMN_NAME+"' FROM '"+itsTableName+"' "
				+" WHERE '"+Events.TIMESTAMP+"' <= ?"
				+" ORDER BY '"+Events.TIMESTAMP+"' DESC, '"+SEQUENCE_COLUMN_NAME+"' DESC "
				+" LIMIT 1");
		
	}
	
	public Queries getQueries()
	{
		return itsCollector.getQueries();
	}
	
	/**
	 * We must release the view
	 */
	protected void finalize() throws Throwable
	{
		ISQLBackend theBackend = itsCollector.getQueries().getBackend();
		theBackend.dropTable(itsTableName);
		System.out.println("Dropped table "+itsTableName);
		super.finalize();
	}
	
	public int getEventCount()
	{
		return itsSize;
	}

	public int getEventCount(long aT1, long aT2)
	{
		throw new UnsupportedOperationException();
	}

	public int[] getEventCounts(long aT1, long aT2, int aSlotsCount)
	{
		throw new UnsupportedOperationException();
	}

	public List<ILogEvent> getEvents(long aT1, long aT2)
	{
		throw new UnsupportedOperationException();
	}

	public void setCursor(ILogEvent aEvent)
	{
		Event theEvent = (Event) aEvent;
		long theId = theEvent.getId();
		if (theId > Integer.MAX_VALUE) throw new RuntimeException("id too big: "+theId);
		else setCursor((int) theId);
	}
	
	protected ILogEvent[] loadPage(int aStartIndex, int aSize)
	{
		try
		{
			itsLoadPageStatement.setInt(1, aStartIndex);
			itsLoadPageStatement.setInt(2, aStartIndex+aSize);
			ResultSet theResultSet = itsLoadPageStatement.executeQuery();
			
			ILogEvent[] theEvents = new ILogEvent[aSize];
			for (int i = 0; i < aSize; i++)
			{
				if (! theResultSet.next()) break;
				theEvents[i] = decodeEvent(theResultSet);
			}
			
			return theEvents;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void setNextTimestamp(long aTimestamp)
	{
		try
		{
			itsNextTimestampStatement.setLong(1, aTimestamp);
			ResultSet theResultSet = itsNextTimestampStatement.executeQuery();
			
			int theCursor = theResultSet.next() ?
					theResultSet.getInt(1)
					: getEventCount();
					
			setCursor(theCursor);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void setPreviousTimestamp(long aTimestamp)
	{
		try
		{
			itsPrevTimestampStatement.setLong(1, aTimestamp);
			ResultSet theResultSet = itsPrevTimestampStatement.executeQuery();
			
			int theCursor = theResultSet.next() ?
					theResultSet.getInt(1) + 1
					: 0;
					
			setCursor(theCursor);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	
	private LocationInfo getLocationInfo (byte aLocationType, int aLocationId)
	{
		switch (aLocationType)
		{
			case Queries.LOCATION_TYPE_BEHAVIOUR:
				return itsCollector.getBehavior(aLocationId);
				
			case Queries.LOCATION_TYPE_FIELD:
				return itsCollector.getField(aLocationId);
				
			case Queries.LOCATION_TYPE_TYPE:
				return itsCollector.getType(aLocationId);

			default:
				throw new RuntimeException("Unexpected location type: "+aLocationType);
		}
	}
	
	/**
	 * Decodes the event currently pointed to by the result set.
	 */
	private ILogEvent decodeEvent (ResultSet aResultSet) throws SQLException
	{
		// Get raw data
		long theId = aResultSet.getLong(Events.ID.getIndex()+1);
		EventType theType = EventType.values()[aResultSet.getByte(Events.TYPE.getIndex()+1)];
		long theTimestamp = aResultSet.getLong(Events.TIMESTAMP.getIndex()+1);
		long theSerial = aResultSet.getLong(Events.SERIAL.getIndex()+1);
		long theThreadId = aResultSet.getLong(Events.THREAD_ID.getIndex()+1);
		int theDepth = aResultSet.getInt(Events.DEPTH.getIndex()+1);
		
		byte theLocationType = aResultSet.getByte(Events.LOCATION_TYPE.getIndex()+1);
		int theLocationId = aResultSet.getInt(Events.LOCATION_ID.getIndex()+1);
		
		byte theTargetType = aResultSet.getByte(Events.TARGET_TYPE.getIndex()+1);
		long theTargetId = aResultSet.getLong(Events.TARGET_ID.getIndex()+1);
		
		long theArgId = aResultSet.getLong(Events.ARG_ID.getIndex()+1);
		
		// Decode common data
		ThreadInfo theThread = itsCollector.getThread(theThreadId);
		Object theTarget = getQueries().decodeValue(theTargetType, theTargetId);
		LocationInfo theLocationInfo = getLocationInfo(theLocationType, theLocationId);

		// Create specific event
		Event theEvent;
		switch (theType)
		{
			case BEFORE_METHOD_CALL:
				theEvent = createBefereMethodCall((BehaviorInfo) theLocationInfo, theTarget, theArgId);
				break;

			case AFTER_METHOD_CALL:
				theEvent = createAfterMethodCall((BehaviorInfo) theLocationInfo, theTarget, theArgId);
				break;

			case BEHAVIOUR_ENTER:
				theEvent = createBehaviourEnter((BehaviorInfo) theLocationInfo);
				break;
				
			case BEHAVIOUR_EXIT:
				theEvent = createBehaviourExit((BehaviorInfo) theLocationInfo);
				break;
				
			case FIELD_WRITE:
				theEvent = createFieldWrite((FieldInfo) theLocationInfo, theTarget, theArgId);
				break;

			case INSTANTIATION:
				theEvent = createInstantiation((TypeInfo) theLocationInfo, theTarget);
				break;

			default: throw new RuntimeException("Unexpected event type: "+theType);
		}


		theEvent.setId(theId);
		theEvent.setTimestamp(theTimestamp);
		theEvent.setThread(theThread);
		theEvent.setSerial(theSerial);
		theEvent.setDepth(theDepth);
		return theEvent;
	}

	private BeforeMethodCall createBefereMethodCall(BehaviorInfo aBehaviourInfo, Object aTarget, long aArgId) throws SQLException
	{
		BeforeMethodCall theEvent = new BeforeMethodCall();
		theEvent.setBehavior(aBehaviourInfo);
		theEvent.setTarget(aTarget);

		if (aArgId != -1) theEvent.setArguments(itsCollector.getQueries().loadArguments.loadArguments(aArgId));
		
		return theEvent;
	}

	private AfterMethodCall createAfterMethodCall(BehaviorInfo aBehaviourInfo, Object aTarget, long aArgId) throws SQLException
	{
		AfterMethodCall theEvent = new AfterMethodCall();
		theEvent.setBehavior(aBehaviourInfo);
		theEvent.setTarget(aTarget);
		
		if (aArgId != -1) theEvent.setReturnValue(itsCollector.getQueries().loadArguments.loadArgument(aArgId));
		
		return theEvent;
	}

	private BehaviorEnter createBehaviourEnter(BehaviorInfo aBehaviourInfo) throws SQLException
	{
		BehaviorEnter theEvent = new BehaviorEnter();
		theEvent.setBehavior(aBehaviourInfo);
		
		return theEvent;
	}
	
	private BehaviourExit createBehaviourExit(BehaviorInfo aBehaviourInfo) throws SQLException
	{
		BehaviourExit theEvent = new BehaviourExit();
		theEvent.setBehavior(aBehaviourInfo);
		
		return theEvent;
	}
	
	private FieldWriteEvent createFieldWrite(FieldInfo aFieldInfo, Object aTarget, long aArgId) throws SQLException
	{
		FieldWriteEvent theEvent = new FieldWriteEvent();
		theEvent.setField(aFieldInfo);
		theEvent.setTarget(aTarget);

		if (aArgId != -1) theEvent.setValue(itsCollector.getQueries().loadArguments.loadArgument(aArgId));
		
		return theEvent;
	}

	private Instantiation createInstantiation(TypeInfo aTypeInfo, Object aInstance) throws SQLException
	{
		Instantiation theEvent = new Instantiation();
		theEvent.setType(aTypeInfo);
		theEvent.setInstance(aInstance);
		return theEvent;
	}

	private OutputEvent createOutput()
	{
		OutputEvent theEvent = new OutputEvent();
		return theEvent;
	}


}
