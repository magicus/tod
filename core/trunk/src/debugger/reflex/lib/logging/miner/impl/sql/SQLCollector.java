/*
 * Created on Nov 23, 2004
 */
package reflex.lib.logging.miner.impl.sql;

import reflex.lib.logging.miner.impl.common.AbstractMinerCollector;
import tod.core.BehaviourType;
import tod.core.ILocationRegistrer;
import tod.core.Output;
import tod.core.model.structure.ThreadInfo;

import java.sql.SQLException;

/**
 * A collector that stores events in SQL tables.
 * @author gpothier
 */
public class SQLCollector extends AbstractMinerCollector 
{
	private Queries itsQueries;
	
	public SQLCollector(Queries aQueries) throws SQLException
	{
		itsQueries = aQueries;
		aQueries.loadLocations.load(new LoadCollector());
		aQueries.loadThreads.load(new LoadCollector());
	}

	public Queries getQueries()
	{
		return itsQueries;
	}

	protected void logBehaviorEnter(
			long aTimestamp, 
			ThreadInfo aThreadInfo,
			long aSerial,
			int aDepth,
			int aBehaviourId)
	{
		try
		{
			
			itsQueries.addEvent.insert(
					EventType.BEHAVIOUR_ENTER, 
					aTimestamp, 
					aSerial, 
					aThreadInfo.getId(),
					aDepth, 
					Queries.LOCATION_TYPE_BEHAVIOUR,
					aBehaviourId, 
					null, 
					0);
		}
		catch (SQLException e)
		{
			while (e != null)
			{
				e.printStackTrace();
				e = e.getNextException();
			}
		}
	}

	protected void logBehaviorExit(
			long aTimestamp,
			ThreadInfo aThreadInfo,
			long aSerial, 
			int aDepth,
			int aBehaviourId)
	{
		try
		{
			itsQueries.addEvent.insert(
					EventType.BEHAVIOUR_EXIT, 
					aTimestamp, 
					aSerial, 
					aThreadInfo.getId(),
					aDepth, 
					Queries.LOCATION_TYPE_BEHAVIOUR,
					aBehaviourId, 
					null, 
					0);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	protected void logFieldWrite(
			long aTimestamp, 
			ThreadInfo aThreadInfo, 
			int aOperationLocationId, 
			int aOperationLineNumber, 
			long aSerial, 
			int aDepth, 
			int aLocationId,
			Object aTarget,
			Object aArgument)
	{
		try
		{
			long theArgId = itsQueries.addArgument.insert(aArgument);
			itsQueries.addEvent.insert(
					EventType.FIELD_WRITE, 
					aTimestamp, 
					aSerial, 
					aThreadInfo.getId(),
					aDepth, 
					Queries.LOCATION_TYPE_FIELD,
					aLocationId, 
					aTarget, 
					theArgId);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected void logLocalVariableWrite(long aTimestamp, ThreadInfo aThreadInfo, int aOperationLocationId, int aOperationLineNumber, long aSerial, int aDepth, int aLocalVariableId, Object aTarget, Object aValue)
	{
		throw new UnsupportedOperationException();
	}
	
	protected void logInstantiation(
			long aTimestamp,
			ThreadInfo aThreadInfo, 
			int aOperationLocationId, 
			int aOperationLineNumber, 
			long aSerial, 
			int aDepth, 
			int aTypeId,
			Object aValue)
	{
		try
		{
			itsQueries.addEvent.insert(
					EventType.INSTANTIATION, 
					aTimestamp, 
					aSerial, 
					aThreadInfo.getId(),
					aDepth, 
					Queries.LOCATION_TYPE_TYPE,
					aTypeId, 
					aValue, 
					0);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	protected void logBeforeMethodCall(
			long aTimestamp,
			ThreadInfo aThreadInfo, 
			int aOperationLocationId, 
			int aOperationLineNumber, 
			long aSerial, 
			int aDepth, 
			int aTypeId,
			Object aValue,
			Object[] aArguments)
	{
		try
		{
			long theArgId = -1;
			if (aArguments != null) theArgId = itsQueries.addArgument.insert(aArguments);
			
			itsQueries.addEvent.insert(
					EventType.INSTANTIATION, 
					aTimestamp, 
					aSerial, 
					aThreadInfo.getId(),
					aDepth, 
					Queries.LOCATION_TYPE_TYPE,
					aTypeId, 
					aValue, 
					theArgId);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	protected void logAfterMethodCall(
			long aTimestamp,
			ThreadInfo aThreadInfo, 
			int aOperationLocationId, 
			int aOperationLineNumber, 
			long aSerial, 
			int aDepth, 
			int aTypeId,
			Object aValue,
			Object aResult)
	{
		try
		{
			long theArgId = itsQueries.addArgument.insert(aResult);
			
			itsQueries.addEvent.insert(
					EventType.INSTANTIATION, 
					aTimestamp, 
					aSerial, 
					aThreadInfo.getId(),
					aDepth, 
					Queries.LOCATION_TYPE_TYPE,
					aTypeId, 
					aValue, 
					theArgId);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	protected void logOutput(
			long aTimestamp, 
			ThreadInfo aThreadInfo, 
			long aSerial,
			int aDepth, 
			Output aOutput,
			byte[] aData)
	{
//		try
//		{
//			long theArgId = itsQueries.addArgument.insert(aData);
//			itsQueries.addEvent.insert(
//					EventType.CONSTRUCTOR_ENTER, 
//					aTimestamp, 
//					aSerial, 
//					aThreadInfo.getId(),
//					aDepth, 
//					Queries.LOCATION_TYPE_CONSTRUCTOR,
//					aLocationId, 
//					aTarget, 
//					theArgId);
//		}
//		catch (SQLException e)
//		{
//			e.printStackTrace();
//		}
	}


	/**
	 *
	 */

	public void registerBehaviour(BehaviourType aBehaviourType, int aBehaviourId, int aTypeId, String aBehaviourName)
	{
		try
		{
			itsQueries.addLocation.addBehaviour(aBehaviourType, aBehaviourId, aTypeId, aBehaviourName);
			super.registerBehaviour(aBehaviourType, aBehaviourId, aTypeId, aBehaviourName);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void registerField(int aFieldId, int aTypeId, String aFieldName)
	{
		try
		{
			itsQueries.addLocation.addField(aFieldId, aTypeId, aFieldName);
			super.registerField(aFieldId, aTypeId, aFieldName);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
	{
		try
		{
			itsQueries.addLocation.addType(aTypeId, aTypeName);
			super.registerType(aTypeId, aTypeName, aSupertypeId, aInterfaceIds);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void registerThread(long aThreadId, String aName)
	{
		try
		{
			itsQueries.addThread.addThread(aThreadId, aName);
			super.registerThread(aThreadId, aName);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * This location registrer simply passes all calls to super, so that
	 * existing locations can be reloaded without being added again to the database.
	 */
	private class LoadCollector implements ILocationRegistrer
	{
		public void registerFile(int aFileId, String aFileName)
		{
			SQLCollector.super.registerFile(aFileId, aFileName);
		}

		public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
		{
			SQLCollector.super.registerType(aTypeId, aTypeName, aSupertypeId, aInterfaceIds);
		}

		public void registerBehaviour(BehaviourType aBehaviourType, int aBehaviourId, int aTypeId, String aBehaviourName)
		{
			SQLCollector.super.registerBehaviour(aBehaviourType, aBehaviourId, aTypeId, aBehaviourName);
		}

		public void registerField(int aFieldId, int aTypeId, String aFieldName)
		{
			SQLCollector.super.registerField(aFieldId, aTypeId, aFieldName);
		}

		public void registerLocalVariable(int aLocalVariableId, int aBehaviorId, String aLocalVariableName)
		{
			throw new UnsupportedOperationException();
		}

		public void registerThread(long aThreadId, String aName)
		{
			SQLCollector.super.registerThread(aThreadId, aName);
		}
	}

}
