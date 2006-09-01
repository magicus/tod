/*
 * Created on Aug 29, 2006
 */
package tod.impl.dbgrid.bench;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import tod.core.ILogCollector;
import tod.core.LocationRegistrer;
import tod.core.Output;
import tod.core.bci.NativeAgentPeer;
import tod.core.transport.CollectorPacketReader;
import tod.core.transport.MessageType;
import tod.impl.bci.asm.ASMLocationPool;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.utils.StoreTODServer;

/**
 * Reads events stored on the disk by a {@link StoreTODServer}
 * and sends it to a grid master.
 * @author gpothier
 */
public class GridReplay
{
	public static void main(String[] args) throws Exception
	{
		GridMaster theMaster = BenchBase.setupMaster(args);
		
//		ILogCollector theCollector = theMaster.createCollector(1);
		ILogCollector theCollector = new DummyCollector();
		
		long t0 = System.currentTimeMillis();
		File theFile = new File("events-1.bin");
		long theCount = process(theFile, theMaster, theCollector);
//		long theCount = readFile(new File("indexes2.bin"));
		long t1 = System.currentTimeMillis();
		float dt = (t1-t0)/1000f;
		float theEpS = theCount/dt;
		System.out.println("Events: "+theCount+" time: "+dt+"s rate: "+theEpS+"ev/s");
		System.exit(0);
	}
	
	private static long readFile(File aFile) throws IOException
	{
		FileInputStream theStream = new FileInputStream(aFile);
		int c;
		long sz = 0;
		byte[] b = new byte[4096];
		while ((c = theStream.read(b)) > 0) sz += c;
		
		return sz;
	}
	
	private static long process(
			File aFile,
			GridMaster aMaster,
			ILogCollector aCollector) 
			throws IOException
	{
		DataInputStream theStream = new DataInputStream(new FileInputStream(aFile));
		
		String theHostName = theStream.readUTF();
		System.out.println("Reading events of "+theHostName);

		long theCount = 0;
		
		while (true)
		{
			byte theCommand;
			try
			{
				theCommand = theStream.readByte();
			}
			catch (EOFException e)
			{
				break;
			}
			
			try
			{
				if (theCommand == NativeAgentPeer.INSTRUMENT_CLASS)
				{
					throw new RuntimeException();
				}
				else
				{
					MessageType theType = MessageType.values()[theCommand];
					CollectorPacketReader.readPacket(
							theStream, 
							aCollector,
							null,
							theType);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
			
			theCount++;
		}
		
		aMaster.flush();
		System.out.println("Done");
		
		return theCount;
	}
	
	private static class DummyCollector implements ILogCollector
	{

		public void logAfterBehaviorCall(long aTimestamp, long aThreadId, int aOperationBytecodeIndex,
				int aBehaviorLocationId, Object aTarget, Object aResult)
		{
		}

		public void logAfterBehaviorCall(long aThreadId)
		{
		}

		public void logAfterBehaviorCallWithException(long aTimestamp, long aThreadId, int aOperationBytecodeIndex,
				int aBehaviorLocationId, Object aTarget, Object aException)
		{
		}

		public void logBeforeBehaviorCall(long aThreadId, int aOperationBytecodeIndex, int aBehaviorLocationId)
		{
		}

		public void logBeforeBehaviorCall(long aTimestamp, long aThreadId, int aOperationBytecodeIndex,
				int aBehaviorLocationId, Object aTarget, Object[] aArguments)
		{
		}

		public void logBehaviorEnter(long aTimestamp, long aThreadId, int aBehaviorLocationId, Object aObject,
				Object[] aArguments)
		{
		}

		public void logBehaviorExit(long aTimestamp, long aThreadId, int aBehaviorLocationId, Object aResult)
		{
		}

		public void logBehaviorExitWithException(long aTimestamp, long aThreadId, int aBehaviorLocationId,
				Object aException)
		{
		}

		public void logConstructorChaining(long aThreadId)
		{
		}

		public void logExceptionGenerated(long aTimestamp, long aThreadId, int aBehaviorLocationId,
				int aOperationBytecodeIndex, Object aException)
		{
		}

		public void logExceptionGenerated(long aTimestamp, long aThreadId, String aMethodName, String aMethodSignature,
				String aMethodDeclaringClassSignature, int aOperationBytecodeIndex, Object aException)
		{
		}

		public void logFieldWrite(long aTimestamp, long aThreadId, int aOperationBytecodeIndex, int aFieldLocationId,
				Object aTarget, Object aValue)
		{
		}

		public void logInstantiation(long aThreadId)
		{
		}

		public void logLocalVariableWrite(long aTimestamp, long aThreadId, int aOperationBytecodeIndex,
				int aVariableId, Object aValue)
		{
		}

		public void logOutput(long aTimestamp, long aThreadId, Output aOutput, byte[] aData)
		{
		}

		public void registerThread(long aThreadId, String aName)
		{
		}
		
	}

}
