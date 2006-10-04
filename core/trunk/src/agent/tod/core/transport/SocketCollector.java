/*
 * Created on Oct 13, 2004
 */
package tod.core.transport;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import tod.agent.AgentConfig;
import tod.agent.AgentReady;
import tod.core.HighLevelCollector;
import tod.core.Output;
import tod.core.EventInterpreter.ThreadData;

/**
 * This collector sends the events to a socket.
 * The other end of the socket should be a {@link tod.core.transport.LogReceiver}.
 * @author gpothier
 */
public class SocketCollector extends HighLevelCollector<SocketCollector.SocketThreadData>
{
	private static final boolean IGNORE_ALL = false;
	private static final boolean DO_SEND = true;
	
	private List<SocketThreadData> itsThreadDataList = new ArrayList<SocketThreadData>();
	private Sender itsSender;
	
	public SocketCollector(String aHostname, int aPort) throws IOException 
	{
		this (new Socket(aHostname, aPort));
	}
	
	public SocketCollector(Socket aSocket)
	{
		itsSender = new Sender(aSocket);
		itsSender.sendHostName();
		Runtime.getRuntime().addShutdownHook(new MyShutdownHook());

		AgentReady.READY = true;
	}

	@Override
	public SocketThreadData createThreadData(int aId)
	{
		SocketThreadData theData = new SocketThreadData(aId);
		itsThreadDataList.add (theData);
		
		System.out.println("Created thread data ("+itsThreadDataList.size()+")");
		
		return theData;
	}
	
	@Override
	protected void behaviorExit(
			SocketThreadData aThread,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aOperationBytecodeIndex, 
			int aBehaviorId,
			boolean aHasThrown,
			Object aResult)
	{
		if (IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart();
        	
        	CollectorPacketWriter.sendBehaviorExit(
        			theStream,
        			aThread.getId(),
        			aParentTimestamp,
        			aDepth,
        			aTimestamp,
        			aOperationBytecodeIndex, 
        			aBehaviorId,
        			aHasThrown,
        			aResult);
        	
            aThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}

	@Override
	protected void exception(
			SocketThreadData aThread, 
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp,
			String aMethodName,
			String aMethodSignature,
			String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex,
			Object aException)
	{
		if (IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart();
        	
        	CollectorPacketWriter.sendException(
        			theStream,
        			aThread.getId(),
        			aParentTimestamp,
        			aDepth,
        			aTimestamp,
        			aMethodName,
        			aMethodSignature,
        			aMethodDeclaringClassSignature,
        			aOperationBytecodeIndex, 
        			aException);
        	
            aThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}

	@Override
	protected void fieldWrite(
			SocketThreadData aThread,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aOperationBytecodeIndex,
			int aFieldLocationId,
			Object aTarget, 
			Object aValue)
	{
		if (IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart();
        	
        	CollectorPacketWriter.sendFieldWrite(
        			theStream, 
        			aThread.getId(),
        			aParentTimestamp,
        			aDepth,
        			aTimestamp,
        			aOperationBytecodeIndex,
        			aFieldLocationId,
        			aTarget,
        			aValue);
        	
            aThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}

	@Override
	protected void instantiation(
			SocketThreadData aThread,
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehavior, 
			int aExecutedBehavior, 
			Object aTarget,
			Object[] aArguments)
	{
		if (IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart();
        	
        	CollectorPacketWriter.sendInstantiation(
        			theStream,
        			aThread.getId(),
        			aParentTimestamp,
        			aDepth,
        			aTimestamp, 
        			aOperationBytecodeIndex, 
        			aDirectParent,
        			aCalledBehavior,
        			aExecutedBehavior,
        			aTarget,
        			aArguments);
        	
            aThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}

	@Override
	protected void localWrite(
			SocketThreadData aThread,
			long aParentTimestamp,
			short aDepth, 
			long aTimestamp,
			int aOperationBytecodeIndex,
			int aVariableId,
			Object aValue)
	{
		if (IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart();
        	
        	CollectorPacketWriter.sendLocalWrite(
        			theStream,
        			aThread.getId(), 
        			aParentTimestamp,
        			aDepth,
        			aTimestamp,
        			aOperationBytecodeIndex, 
        			aVariableId, 
        			aValue);
        	
            aThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}

	@Override
	protected void methodCall(
			SocketThreadData aThread,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget, 
			Object[] aArguments)
	{
		if (IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart();
        	
        	CollectorPacketWriter.sendMethodCall(
        			theStream,
        			aThread.getId(), 
        			aParentTimestamp,
        			aDepth,
        			aTimestamp,
        			aOperationBytecodeIndex,
        			aDirectParent, 
        			aCalledBehavior,
        			aExecutedBehavior,
        			aTarget, 
        			aArguments);
            aThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}

	@Override
	protected void output(
			SocketThreadData aThread,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			Output aOutput,
			byte[] aData)
	{
		if (IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart();
        	
        	CollectorPacketWriter.sendOutput(
        			theStream, 
        			aThread.getId(),
        			aParentTimestamp,
        			aDepth,
        			aTimestamp, 
        			aOutput,
        			aData);
        	
            aThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}

	@Override
	protected void superCall(
			SocketThreadData aThread,
			long aParentTimestamp,
			short aDepth, 
			long aTimestamp,
			int aOperationBytecodeIndex,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments)
	{
		if (IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart();
        	
        	CollectorPacketWriter.sendSuperCall(
        			theStream,
        			aThread.getId(),
        			aParentTimestamp,
        			aDepth,
        			aTimestamp, 
        			aOperationBytecodeIndex,
        			aDirectParent,
        			aCalledBehavior, 
        			aExecutedBehavior, 
        			aTarget, 
        			aArguments);
        	
            aThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}

	@Override
	protected void thread(SocketThreadData aThread, long aJVMThreadId, String aName)
	{
		if (IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart();
        	CollectorPacketWriter.sendThread(theStream, aThread.getId(), aJVMThreadId, aName);
            aThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}
	
	/**
	 * Maintains the thread specific information that permits to construct
	 * log packets without blocking other threads.
	 * Log packets are constructed in a buffer. When the packet is ready,  
	 * the synchronized {@link #packetEnd()} method should be called, which
	 * copies the packet to a bigger buffer that stores full packets until it
	 * is requested to send them to a socket.
	 * @author gpothier
	 */
	class SocketThreadData extends ThreadData
	{
		private static final int BUFFER_SIZE = 32768;
		
		/**
		 * A wrapper around {@link #itsBuffer}
		 */
		private DataOutputStream itsDataOutputStream;
		
		/**
		 * In construction packet buffer
		 */
		private ByteArrayOutputStream itsBuffer;
		
		/**
		 * Full packets buffer
		 */
		private ByteArrayOutputStream itsLog;
		
		private boolean itsSending = false;
		
		private boolean itsShutDown = false;
		
		
		public SocketThreadData(int aId)
		{
			super(aId);
			itsBuffer = new ByteArrayOutputStream();
			itsLog = new ByteArrayOutputStream(BUFFER_SIZE);
			itsDataOutputStream = new DataOutputStream(itsBuffer);
		}

		public DataOutputStream packetStart()
		{
			if (itsSending) throw new RuntimeException();
			itsSending = true;
			return itsDataOutputStream;
		}
		
		public boolean isSending()
		{
			return itsSending;
		}
		
		public synchronized void packetEnd()
		{
			if (! itsSending) throw new RuntimeException();
			itsSending = false;
			try
			{
				itsDataOutputStream.flush();
				int theRequestedSize = itsBuffer.size();
				if (itsLog.size() + theRequestedSize > BUFFER_SIZE) send();
				if (DO_SEND) itsBuffer.writeTo(itsLog);
				itsBuffer.reset();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		private void send()
		{
			if (! itsShutDown && itsLog.size() > 0)
			{
//				System.out.println("Sending "+itsLog.size()+" bytes");
				itsSender.sendLog(itsLog);
				itsLog.reset();
			}
		}
		
		public void shutDown() 
		{
			send();
			itsShutDown = true;
		}
	}
	
	private static class Sender
	{
		private Socket itsSocket;
		private OutputStream itsOutputStream;

		public Sender(Socket aSocket)
		{
			itsSocket = aSocket;
			try
			{
				itsOutputStream = itsSocket.getOutputStream();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		public void sendHostName()
		{
			try
			{
				DataOutputStream theStream = new DataOutputStream(itsOutputStream);
				theStream.writeUTF(AgentConfig.getHostName());
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		public synchronized void sendLog(ByteArrayOutputStream aStream)
		{
			try
			{
				aStream.writeTo(itsOutputStream);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
		
	private class MyShutdownHook extends Thread
	{
		@Override
		public void run()
		{
			System.out.println("SocketCollector: Shutting down");
			
			if (itsThreadDataList == null) return;
			
			for (SocketThreadData theData : itsThreadDataList) theData.shutDown();
			
			System.out.println("SocketLogCollector: flushed all threads.");
		}
	}
}
