/*
 * Created on Mar 27, 2006
 */
package tod.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.bind.tuple.TupleTupleKeyCreator;
import com.sleepycat.je.Cursor;
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

public class DBBench
{
	private static final long N = 1*1000*1000;
	
	private Database itsDatabase;
	private List<SecondaryDatabase> itsIndices = new ArrayList<SecondaryDatabase>(); 
	
	public DBBench(int aIndicesCount) throws DatabaseException
	{
		itsDatabase = openDatabase(aIndicesCount);
	}

	public Database openDatabase(int aIndicesCount) throws DatabaseException
	{
		File theBaseDir = new File("/home/gpothier/tmp/dbbench");
		theBaseDir.mkdirs();
		
		EnvironmentConfig theConfig = new EnvironmentConfig();
		theConfig.setAllowCreate(true);
		theConfig.setReadOnly(false);
		theConfig.setTransactional(false);
		Environment theEnvironment = new Environment(theBaseDir, theConfig);
		
		DatabaseConfig theDBConfig = new DatabaseConfig();
		theDBConfig.setAllowCreate(true);
		theDBConfig.setReadOnly(false);
		theDBConfig.setTransactional(false);
		
		Database theDatabase = theEnvironment.openDatabase(null, "test", theDBConfig);
//		theEnvironment.truncateDatabase(null, "test", false);
		
		for (int i=0;i<aIndicesCount;i++)
		{
			SecondaryConfig theConfig2 = new SecondaryConfig();
			theConfig2.setAllowCreate(true);
			theConfig2.setImmutableSecondaryKey(true);
			theConfig2.setSortedDuplicates(true);
			theConfig2.setKeyCreator(new MyKeyCreator(i));
			
			SecondaryDatabase theIndex = theEnvironment.openSecondaryDatabase(null, "sec-"+i, theDatabase, theConfig2);
			itsIndices.add(theIndex);
		}
		
		return theDatabase;
	}
	
	public void bench(boolean aCursor, float aDevTreshold, float aDevAmount, int aDataSize) throws DatabaseException
	{
		MyBinding theBinding = new MyBinding(aDataSize);
		Cursor theCursor = itsDatabase.openCursor(null, null);
		
		DatabaseEntry theKey = new DatabaseEntry();
		DatabaseEntry theValue = new DatabaseEntry();
		
		long t0 = System.currentTimeMillis();
		
		long k0 = 0;
		
		long dev = 0;
		long tdev = 0;
		long i;
		for (i=0;i<N;i++)
		{
			long k = k0++;
			if (Math.random()<aDevTreshold) 
			{
				int dk = (int)(Math.random()*aDevAmount);
				k0 -= dk;  
				dev++;
				tdev += dk;
			}
			
			LongBinding.longToEntry(k, theKey);
			theBinding.objectToEntry(null, theValue);
			
			if (aCursor) theCursor.put(theKey, theValue);
			else itsDatabase.put(null, theKey, theValue);
			
			if (i % 10000 == 0) 
			{
				long t1 = System.currentTimeMillis();
				System.out.println(String.format("Wrote %d records, %.3frec/s, dev: %d, tdev: %d", i, 1000f*i/(t1-t0), dev, tdev));
			}
		}
		
		itsDatabase.getEnvironment().sync();
		long t1 = System.currentTimeMillis();
		System.out.println(String.format("Wrote %d records in %.3fs, %.3frec/s", i, 0.001f*(t1-t0), 1000f*i/(t1-t0)));
	}
	
	private static class MyBinding extends TupleBinding
	{
		private int itsSize;
		
		public MyBinding(int aSize)
		{
			itsSize = aSize;
		}

		@Override
		public Object entryToObject(TupleInput aInput)
		{
			return null;
		}

		@Override
		public void objectToEntry(Object aObject, TupleOutput aOutput)
		{
			for(int i=0;i<itsSize;i++) aOutput.writeByte(0);	
		}
	}
	
	private static class MyKeyCreator extends TupleTupleKeyCreator
	{
		private int itsSkip;
		
		public MyKeyCreator(int aSkip)
		{
			itsSkip = aSkip;
		}

		@Override
		public boolean createSecondaryKey(
				TupleInput aPrimaryKeyInput, 
				TupleInput aDataInput, 
				TupleOutput aResult)
		{
			aDataInput.skip(itsSkip);
			long l = aDataInput.readLong();
			aResult.writeLong(l);
			
			return true;
		}
	}
	
	public static void main(String[] args) throws DatabaseException
	{
		DBBench theBench = new DBBench(0);
		theBench.bench(true, 0.01f, 1000, 100);
	}
}