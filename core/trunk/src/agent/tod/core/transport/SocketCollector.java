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
	private List<SocketThreadData> itsThreadDataList = new ArrayList<SocketThreadData>();
	private MyThread itsThread;
	
	public SocketCollector(String aHostname, int aPort) throws IOException 
	{
		this (new MyThread (new Socket(aHostname, aPort)));
	}
	
	public SocketCollector(int aPort) throws IOException 
	{
		this (new MyThread (new ServerSocket(aPort)));
	}
	
	public SocketCollector(MyThread aThread)
	{
		itsThread = aThread;
		itsThread.setThreadInfosList(itsThreadDataList);
		AgentReady.READY = true;
	}

	@Override
	public SocketThreadData createThreadData(int aId)
	{
		SocketThreadData theData = new SocketThreadData(aId);
		itsThreadDataList.add (theData);
		
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
	static class SocketThreadData extends ThreadData
	{
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
		
		
		public SocketThreadData(int aId)
		{
			super(aId);
			itsBuffer = new ByteArrayOutputStream();
			itsLog = new ByteArrayOutputStream(32768);
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
//				System.out.println("Flushing "+itsBuffer.size()+" bytes.");
				itsBuffer.writeTo(itsLog);
				itsBuffer.reset();
//				itsLog.flush();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		public synchronized void sendLog (OutputStream aOutputStream) throws IOException
		{
			if (itsLog.size() > 0)
			{
//				System.out.println("Sending "+itsLog.size()+" bytes");
				itsLog.writeTo(aOutputStream);
				itsLog.reset();
			}
		}
	}
	
	private static class MyThread extends SocketThread
	{
		private List<SocketThreadData> itsThreadInfosList;
		private boolean itsHostNameSent = false;
		
		public MyThread(ServerSocket aServerSocket)
		{
			super(aServerSocket, false);
			init();
		}

		public MyThread(Socket aSocket)
		{
			super(aSocket, false);
			init();
		}
		
		private void init()
		{
			Runtime.getRuntime().addShutdownHook(new MyShutdownHook(this));
			
			setDaemon(true);
			start();
			
			AgentReady.READY = true;			
		}

		public void setThreadInfosList(List<SocketThreadData> aThreadInfosList)
		{
			itsThreadInfosList = aThreadInfosList;
		}

		protected void process (
				OutputStream aOutputStream, 
				InputStream aInputStream) 
				throws IOException, InterruptedException
		{
			try
			{
				if (! itsHostNameSent)
				{
					DataOutputStream theStream = new DataOutputStream(aOutputStream);
					theStream.writeUTF(AgentConfig.getHostName());
					itsHostNameSent = true;
				}
				
				send(aOutputStream);
				Thread.sleep (500);
			}
			catch (ConcurrentModificationException e1)
			{
			}
		}
		
		@Override
		protected void disconnected()
		{
			itsHostNameSent = false;
		}
		
		@Override
		protected void processInterrupted(
				OutputStream aOutputStream, 
				InputStream aInputStream) 
				throws IOException, InterruptedException
		{
			send(aOutputStream);
		}

		private void send(OutputStream aOutputStream) throws IOException
		{
			if (itsThreadInfosList == null) return;
			
			for (Iterator<SocketThreadData> theIterator = itsThreadInfosList.iterator(); theIterator.hasNext();)
			{
				SocketThreadData aThread = theIterator.next();
				aThread.sendLog(aOutputStream);
			}
		}
	}
	
	private static class MyShutdownHook extends Thread
	{
		private MyThread itsThread;
		
		public MyShutdownHook(MyThread aThread)
		{
			itsThread = aThread;
		}

		@Override
		public void run()
		{
			itsThread.interrupt();
		}
	}
}
