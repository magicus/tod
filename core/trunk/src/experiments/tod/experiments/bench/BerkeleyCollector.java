/*
 * Created on Apr 16, 2006
 */
package tod.experiments.bench;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import zz.utils.Utils;

import com.sleepycat.bind.tuple.TupleBase;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.bind.tuple.TupleTupleKeyCreator;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.dbi.CursorImpl.KeyChangeStatus;

public class BerkeleyCollector extends ISimpleLogCollector
{
	private static final String DIR_NAME = "/home/gpothier/tmp/dbbench";
	
	private Database itsEvents;
	private SecondaryDatabase itsTimestampsIndex;
	private SecondaryDatabase itsFieldIndex;
	private SecondaryDatabase itsVarIndex;
	private SecondaryDatabase itsBehaviorIndex;
	
	private DatabaseEntry itsValueEntry = new DatabaseEntry();
	private DatabaseEntry itsKeyEntry = new DatabaseEntry();
	
	private TupleOutput itsValueOutput = new TupleOutput();
	private TupleOutput itsKeyOutput = new TupleOutput();
	private Environment itsEnvironment;

	public BerkeleyCollector()
	{
		try
		{
			Process theProcess = Runtime.getRuntime().exec("rm -rf "+DIR_NAME);
			theProcess.waitFor();

			File theBaseDir = new File(DIR_NAME);
			theBaseDir.mkdirs();
			
			EnvironmentConfig theConfig = new EnvironmentConfig();
			theConfig.setAllowCreate(true);
			theConfig.setReadOnly(false);
			theConfig.setTransactional(false);
			itsEnvironment = new Environment(theBaseDir, theConfig);
			DatabaseConfig theDBConfig = new DatabaseConfig();
			theDBConfig.setAllowCreate(true);
			theDBConfig.setReadOnly(false);
			theDBConfig.setTransactional(false);
			
			itsEvents = itsEnvironment.openDatabase(null, "events", theDBConfig);
			itsTimestampsIndex = createIndex("timestamps", new TimestampKeyCreator(), null);
			itsFieldIndex = createIndex("field", new IdKeyCreator(EventType.FIELD_WRITE), null);
			itsVarIndex = createIndex("var", new IdKeyCreator(EventType.VAR_WRITE), null);
			itsBehaviorIndex = createIndex("behavior", new IdKeyCreator(EventType.BEHAVIOR_ENTER), null);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private SecondaryDatabase createIndex(
			String aName, 
			SecondaryKeyCreator aKeyCreator, 
			Class aValueComparatorClass) throws DatabaseException
	{
		SecondaryConfig theConfig = new SecondaryConfig();
		theConfig.setAllowCreate(true);
		theConfig.setImmutableSecondaryKey(true);
		theConfig.setSortedDuplicates(true);
		theConfig.setKeyCreator(aKeyCreator);
		if (aValueComparatorClass != null) theConfig.setDuplicateComparator(aValueComparatorClass);
		
		return itsEnvironment.openSecondaryDatabase(null, aName, itsEvents, theConfig);
	}

	public long getStoredSize()
	{
		return InsertBench.getDirSize(DIR_NAME);
	}

	private void insert(Database aDatabase) 
	{
		try
		{
			TupleBase.outputToEntry(itsKeyOutput, itsKeyEntry);
			TupleBase.outputToEntry(itsValueOutput, itsValueEntry);
			aDatabase.put(null, itsKeyEntry, itsValueEntry);
			
			itsKeyOutput.reset();
			itsValueOutput.reset();
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public synchronized void logBehaviorEnter(long aTid, long aSeq, int aBehaviorId, long aTarget, long[] args)
	{
		itsKeyOutput.writeLong(aTid);
		itsKeyOutput.writeLong(aSeq);
		
		itsValueOutput.writeLong(time());
		itsValueOutput.writeByte(EventType.BEHAVIOR_ENTER.ordinal());
		itsValueOutput.writeInt(aBehaviorId);
		itsValueOutput.writeLong(aTarget);
		
		itsValueOutput.writeInt(args.length);
		for (long arg : args)
		{
			itsValueOutput.writeLong(arg);
		}
		
		insert(itsEvents);
	}

	public synchronized void logBehaviorExit(long aTid, long aSeq, long aRetValue)
	{
		itsKeyOutput.writeLong(aTid);
		itsKeyOutput.writeLong(aSeq);

		itsValueOutput.writeLong(time());
		itsValueOutput.writeByte(EventType.BEHAVIOR_EXIT.ordinal());
		itsValueOutput.writeLong(aRetValue);
		
		insert(itsEvents);
	}

	public synchronized void logFieldWrite(long aTid, long aSeq, int aFieldId, long aTarget, long aValue)
	{
		itsKeyOutput.writeLong(aTid);
		itsKeyOutput.writeLong(aSeq);
		
		itsValueOutput.writeLong(time());
		itsValueOutput.writeByte(EventType.FIELD_WRITE.ordinal());
		itsValueOutput.writeInt(aFieldId);
		itsValueOutput.writeLong(aTarget);
		itsValueOutput.writeLong(aValue);
		
		insert(itsEvents);
	}

	public synchronized void logVarWrite(long aTid, long aSeq, int aVarId, long aValue)
	{
		itsKeyOutput.writeLong(aTid);
		itsKeyOutput.writeLong(aSeq);
		
		itsValueOutput.writeLong(time());
		itsValueOutput.writeByte(EventType.VAR_WRITE.ordinal());
		itsValueOutput.writeInt(aVarId);
		itsValueOutput.writeLong(aValue);
		
		insert(itsEvents);
	}
	
	private static class TimestampKeyCreator extends TupleTupleKeyCreator
	{
		@Override
		public boolean createSecondaryKey(
				TupleInput aPrimaryKeyInput, 
				TupleInput aDataInput, 
				TupleOutput aIndexKeyOutput)
		{
			long t = aDataInput.readLong();
			aIndexKeyOutput.writeLong(t);
			return true;
		}
	}
	
	private static class IdKeyCreator extends TupleTupleKeyCreator
	{
		private EventType itsType;
		
		public IdKeyCreator(EventType aType)
		{
			itsType = aType;
		}

		@Override
		public boolean createSecondaryKey(
				TupleInput aPrimaryKeyInput, 
				TupleInput aDataInput, 
				TupleOutput aIndexKeyOutput)
		{
			aDataInput.readLong();
			EventType type = EventType.values()[aDataInput.readByte()];
			if (type == itsType)
			{
				int id = aDataInput.readInt();
				aIndexKeyOutput.writeInt(id);
				return true;
			}
			else return false;
		}
	}
	
//	private static class ThreadSeqComparator implements Comparator
//	{
//
//		public int compare(Object aO1, Object aO2)
//		{
//			byte[] b1 = (byte[]) aO1;
//			byte[] b2 = (byte[]) aO2;
//			
//			
//		}
//		
//	}
}
