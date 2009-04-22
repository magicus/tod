/*
 * Created on Apr 15, 2006
 */
package tod.experiments.bench;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RawStorageCollector extends ISimpleLogCollector
{
	private DataOutputStream itsOutputStream;
	private File itsFile; 

	public RawStorageCollector()
	{
		try
		{
			itsFile = new File("/home/gpothier/tmp/tod-raw.bin");
			if (itsFile.exists()) itsFile.delete();
			itsOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(itsFile), 100000));
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public long getStoredSize()
	{
		try
		{
			itsOutputStream.flush();
			return itsFile.length();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public synchronized void logBehaviorEnter(long aTid, long aSeq, int aBehaviorId, long aTarget, long[] args)
	{
		try
		{
			itsOutputStream.writeByte(EventType.BEHAVIOR_ENTER.ordinal());
			itsOutputStream.writeLong(aTid);
			itsOutputStream.writeLong(aSeq);
			itsOutputStream.writeLong(time());
			itsOutputStream.writeInt(aBehaviorId);
			itsOutputStream.writeLong(aTarget);
			for (int i = 0; i < args.length; i++)
			{
				long arg = args[i];
				itsOutputStream.writeLong(arg);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public synchronized void logBehaviorExit(long aTid, long aSeq, long aRetValue)
	{
		try
		{
			itsOutputStream.writeByte(EventType.BEHAVIOR_EXIT.ordinal());
			itsOutputStream.writeLong(aTid);
			itsOutputStream.writeLong(aSeq);
			itsOutputStream.writeLong(time());
			itsOutputStream.writeLong(aRetValue);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public synchronized void logFieldWrite(long aTid, long aSeq, int aFieldId, long aTarget, long aValue)
	{
		try
		{
			itsOutputStream.writeByte(EventType.FIELD_WRITE.ordinal());
			itsOutputStream.writeLong(aTid);
			itsOutputStream.writeLong(aSeq);
			itsOutputStream.writeLong(time());
			itsOutputStream.writeInt(aFieldId);
			itsOutputStream.writeLong(aTarget);
			itsOutputStream.writeLong(aValue);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public synchronized void logVarWrite(long aTid, long aSeq, int aVarId, long aValue)
	{
		try
		{
			itsOutputStream.writeByte(EventType.VAR_WRITE.ordinal());
			itsOutputStream.writeLong(aTid);
			itsOutputStream.writeLong(aSeq);
			itsOutputStream.writeLong(time());
			itsOutputStream.writeInt(aVarId);
			itsOutputStream.writeLong(aValue);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
