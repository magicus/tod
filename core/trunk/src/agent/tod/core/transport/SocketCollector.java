/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.core.transport;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import tod.DebugFlags;
import tod.agent.AgentConfig;
import tod.agent.AgentReady;
import tod.agent.AgentUtils;
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
	private NakedLinkedList<SocketThreadData> itsLRUList = new NakedLinkedList<SocketThreadData>();
	private Sender itsSender;
	private SenderThread itsSenderThread = new SenderThread();
	
	public SocketCollector(String aHostname, int aPort) throws IOException 
	{
		this (new Socket(aHostname, aPort));
	}
	
	public SocketCollector(Socket aSocket)
	{
		itsSender = new Sender(aSocket);
		itsSender.sendHostName();
		Runtime.getRuntime().addShutdownHook(new MyShutdownHook());
		
		itsSenderThread.start();

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
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart(aTimestamp);
        	
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
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart(aTimestamp);
        	
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
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart(aTimestamp);
        	
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
	protected void arrayWrite(
			SocketThreadData aThread,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aOperationBytecodeIndex,
			Object aTarget,
			int aIndex, 
			Object aValue)
	{
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart(aTimestamp);
        	
        	CollectorPacketWriter.sendArrayWrite(
        			theStream, 
        			aThread.getId(),
        			aParentTimestamp,
        			aDepth,
        			aTimestamp,
        			aOperationBytecodeIndex,
        			aTarget,
        			aIndex,
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
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart(aTimestamp);
        	
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
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart(aTimestamp);
        	
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
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart(aTimestamp);
        	
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
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart(aTimestamp);
        	
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
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart(aTimestamp);
        	
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
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	DataOutputStream theStream = aThread.packetStart(0);
        	CollectorPacketWriter.sendThread(theStream, aThread.getId(), aJVMThreadId, aName);
            aThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}
	
	private long itsBiggestDeltaT;

	
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
		
		private long itsFirstTimestamp;
		private long itsLastTimestamp;
		
		/**
		 * Our own entry in the LRU list
		 */
		private NakedLinkedList.Entry<SocketThreadData> itsEntry;
		
		
		public SocketThreadData(int aId)
		{
			super(aId);
			itsBuffer = new ByteArrayOutputStream();
			itsLog = new ByteArrayOutputStream(BUFFER_SIZE);
			itsDataOutputStream = new DataOutputStream(itsBuffer);
			
			// Add ourself to LRU list
			itsEntry = itsLRUList.createEntry(this);
			itsLRUList.addLast(itsEntry);
		}

		public DataOutputStream packetStart(long aTimestamp)
		{
			if (itsSending) throw new RuntimeException();
			itsSending = true;
			
			if (itsFirstTimestamp == 0) itsFirstTimestamp = aTimestamp;
			if (aTimestamp != 0) itsLastTimestamp = aTimestamp;
			
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
				if (! DebugFlags.DISABLE_EVENT_SEND) itsBuffer.writeTo(itsLog);
				itsBuffer.reset();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		public synchronized void send()
		{
			if (! itsShutDown && itsLog.size() > 0)
			{
//				long theDeltaT = itsLastTimestamp-itsFirstTimestamp;
//				itsBiggestDeltaT = Math.max(itsBiggestDeltaT, theDeltaT);
//				System.out.println(String.format(
//						"Sending %d bytes, deltaT: %s, biggest: %s, id: %02d",
//						itsLog.size(),
//						AgentUtils.formatTimestamp(theDeltaT),
//						AgentUtils.formatTimestamp(itsBiggestDeltaT),
//						getId()));
				
				itsSender.sendLog(itsLog);
				itsLog.reset();
			}
			
			itsFirstTimestamp = itsLastTimestamp = 0;
			
			// Place ourself at the end of the LRU list
			itsLRUList.remove(itsEntry);
			itsLRUList.addLast(itsEntry);
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
	
	
	/**
	 * This thread periodically flushes the least recently flushed thread data.
	 * This permits to avoid data being cached for too long in waiting threads.
	 * @author gpothier
	 */
	private class SenderThread extends Thread
	{
		public SenderThread()
		{
			setDaemon(true);
			setPriority(MAX_PRIORITY);
		}
		
		@Override
		public void run()
		{
			try
			{
				while(true)
				{
					if (itsLRUList.size() > 0)
					{
						SocketThreadData theFirst = itsLRUList.getFirst();
//						System.out.println("Flushing "+theFirst.getId());
						theFirst.send();
					}
//					for(SocketThreadData theData : itsThreadDataList)
//					{
//						theData.send();
//					}
					Thread.sleep(10);
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
		
	}
	
}
