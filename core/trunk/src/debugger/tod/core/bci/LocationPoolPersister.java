/*
 * Created on Oct 27, 2005
 */
package tod.core.bci;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import tod.core.BehaviourKind;
import tod.core.ILocationRegistrer;
import tod.core.transport.CollectorPacketReader;
import tod.core.transport.CollectorPacketWriter;

/**
 * Assists the persistance of location pools.
 * Permits to write all registered locations to a file, and replay
 * stored registrations.
 * @author gpothier
 */
public class LocationPoolPersister
{
	/**
	 * This location registerer stores all registration requests in a file
	 * and forwards them to another registrer
	 * @author gpothier
	 */
	public static class Store implements ILocationRegistrer
	{
		private ILocationRegistrer itsTargetRegistrer;
		private DataOutputStream itsStream;
		
		public Store(ILocationRegistrer aTargetRegistrer, DataOutputStream aStream)
		{
			itsTargetRegistrer = aTargetRegistrer;
			itsStream = aStream;
		}

		public void registerBehavior(BehaviourKind aBehaviourType, int aBehaviourId, int aTypeId, String aBehaviourName, String aSignature)
		{
			try
			{
				CollectorPacketWriter.sendRegisterBehavior(itsStream, aBehaviourType, aBehaviourId, aTypeId, aBehaviourName, aSignature);
				itsStream.flush();
				itsTargetRegistrer.registerBehavior(aBehaviourType, aBehaviourId, aTypeId, aBehaviourName, aSignature);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void registerBehaviorAttributes(int aBehaviourId, LineNumberInfo[] aLineNumberTable, LocalVariableInfo[] aLocalVariableTable)
		{
			try
			{
				CollectorPacketWriter.sendRegisterBehaviorAttributes(itsStream, aBehaviourId, aLineNumberTable, aLocalVariableTable);
				itsStream.flush();
				itsTargetRegistrer.registerBehaviorAttributes(aBehaviourId, aLineNumberTable, aLocalVariableTable);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void registerField(int aFieldId, int aTypeId, String aFieldName)
		{
			try
			{
				CollectorPacketWriter.sendRegisterField(itsStream, aFieldId, aTypeId, aFieldName);
				itsStream.flush();
				itsTargetRegistrer.registerField(aFieldId, aTypeId, aFieldName);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void registerFile(int aFileId, String aFileName)
		{
			try
			{
				CollectorPacketWriter.sendRegisterFile(itsStream, aFileId, aFileName);
				itsStream.flush();
				itsTargetRegistrer.registerFile(aFileId, aFileName);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void registerThread(long aThreadId, String aName)
		{
			throw new UnsupportedOperationException();
		}

		public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
		{
			try
			{
				CollectorPacketWriter.sendRegisterType(itsStream, aTypeId, aTypeName, aSupertypeId, aInterfaceIds);
				itsStream.flush();
				itsTargetRegistrer.registerType(aTypeId, aTypeName, aSupertypeId, aInterfaceIds);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Restores previously stored location infos.
	 */
	public static void read (File aFile, ILocationRegistrer aRegistrer) throws FileNotFoundException, IOException
	{
		System.out.println("Reading stored locations from "+aFile);
		DataInputStream theStream = new DataInputStream(new BufferedInputStream(new FileInputStream(aFile)));
		int i = 0;
		while (true)
		{
			try
			{
				CollectorPacketReader.readPacket(theStream, aRegistrer);
				if (i % 10000 == 0) System.out.println(i);
				i++;
			}
			catch (EOFException e)
			{
				break;
			}
		}
		System.out.println("Stored locations loaded");
	}
	
}
