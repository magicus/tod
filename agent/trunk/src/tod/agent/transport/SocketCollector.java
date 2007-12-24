/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

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
*/
package tod.agent.transport;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import tod.agent.AgentConfig;
import tod.agent.AgentReady;
import tod.agent.DebugFlags;
import tod.agent.HighLevelCollector;
import tod.agent.Output;
import tod.agent.EventInterpreter.ThreadData;
import tod.agent.transport.NakedLinkedList.Entry;

/**
 * This collector sends the events to a socket.
 * The other end of the socket should be a {@link tod.agent.transport.LogReceiver}.
 * @author gpothier
 */
public class SocketCollector extends HighLevelCollector<SocketCollector.SocketThreadData>
{
	private List<SocketThreadData> itsThreadDataList = new ArrayList<SocketThreadData>();
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
		
		System.out.println("[TOD] New thread detected ("
				+itsThreadDataList.size()
				+" - '"
				+Thread.currentThread().getName()
				+"')");
		
		return theData;
	}
	
	@Override
	protected void behaviorExit(
			SocketThreadData aThread,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			long aOperationLocation, 
			int aBehaviorId,
			boolean aHasThrown,
			Object aResult)
	{
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	CollectorPacketWriter theWriter = aThread.packetStart(aTimestamp);
        	
        	theWriter.sendBehaviorExit(
        			aThread.getId(),
        			aParentTimestamp,
        			aDepth,
        			aTimestamp,
        			aOperationLocation, 
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
        	CollectorPacketWriter theWriter = aThread.packetStart(aTimestamp);
        	
        	theWriter.sendException(
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
			long aOperationLocation,
			int aFieldLocationId,
			Object aTarget, 
			Object aValue)
	{
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	CollectorPacketWriter theWriter = aThread.packetStart(aTimestamp);
        	
        	theWriter.sendFieldWrite(
        			aThread.getId(),
        			aParentTimestamp,
        			aDepth,
        			aTimestamp,
        			aOperationLocation,
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
	protected void newArray(
			SocketThreadData aThread,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			long aOperationLocation,
			Object aTarget,
			int aBaseTypeId, 
			int aSize)
	{
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	CollectorPacketWriter theWriter = aThread.packetStart(aTimestamp);
        	
        	theWriter.sendNewArray(
        			aThread.getId(),
        			aParentTimestamp,
        			aDepth,
        			aTimestamp,
        			aOperationLocation,
        			aTarget,
        			aBaseTypeId,
        			aSize);
        	
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
			long aOperationLocation,
			Object aTarget,
			int aIndex, 
			Object aValue)
	{
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
		try
		{
			CollectorPacketWriter theWriter = aThread.packetStart(aTimestamp);
			
			theWriter.sendArrayWrite(
					aThread.getId(),
					aParentTimestamp,
					aDepth,
					aTimestamp,
					aOperationLocation,
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
			long aOperationLocation,
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
        	CollectorPacketWriter theWriter = aThread.packetStart(aTimestamp);
        	
        	theWriter.sendInstantiation(
        			aThread.getId(),
        			aParentTimestamp,
        			aDepth,
        			aTimestamp, 
        			aOperationLocation, 
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
			long aOperationLocation,
			int aVariableId,
			Object aValue)
	{
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		if (aThread.isSending()) return;
        try
        {
        	CollectorPacketWriter theWriter = aThread.packetStart(aTimestamp);
        	
        	theWriter.sendLocalWrite(
        			aThread.getId(), 
        			aParentTimestamp,
        			aDepth,
        			aTimestamp,
        			aOperationLocation, 
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
			long aOperationLocation,
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
        	CollectorPacketWriter theWriter = aThread.packetStart(aTimestamp);
        	
        	theWriter.sendMethodCall(
        			aThread.getId(), 
        			aParentTimestamp,
        			aDepth,
        			aTimestamp,
        			aOperationLocation,
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
        	CollectorPacketWriter theWriter = aThread.packetStart(aTimestamp);
        	
        	theWriter.sendOutput(
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
			long aOperationLocation,
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
        	CollectorPacketWriter theWriter = aThread.packetStart(aTimestamp);
        	
        	theWriter.sendSuperCall(
        			aThread.getId(),
        			aParentTimestamp,
        			aDepth,
        			aTimestamp, 
        			aOperationLocation,
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
        	CollectorPacketWriter theWriter = aThread.packetStart(0);
        	theWriter.sendThread(aThread.getId(), aJVMThreadId, aName);
            aThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}
	
	@Override
	protected void clear(SocketThreadData aThread)
	{
		if (DebugFlags.COLLECTOR_IGNORE_ALL) return;
		assert ! aThread.isSending();
        try
        {
        	CollectorPacketWriter theWriter = aThread.packetStart(0);
        	theWriter.sendClear();
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
		private static final int BUFFER_SIZE = AgentConfig.COLLECTOR_BUFFER_SIZE;
		
		/**
		 * A wrapper around {@link #itsBuffer}
		 */
		private final DataOutputStream itsDataOutputStream;

		private final CollectorPacketWriter itsWriter;
		
		/**
		 * In construction packet buffer
		 */
		private final ByteArrayOutputStream itsBuffer;
		
		/**
		 * Full packets buffer
		 */
		private final ByteArrayOutputStream itsLog;
		
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
			itsWriter = new CollectorPacketWriter(itsDataOutputStream);
			
			// Add ourself to LRU list
			itsEntry = itsSenderThread.register(this);
		}

		public CollectorPacketWriter packetStart(long aTimestamp)
		{
			if (itsSending) throw new RuntimeException();
			itsSending = true;
			
			if (itsFirstTimestamp == 0) itsFirstTimestamp = aTimestamp;
			if (aTimestamp != 0) itsLastTimestamp = aTimestamp;
			
			return itsWriter;
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
//				System.out.println("[SocketCollector] Adding "+theRequestedSize+", total: "+itsLog.size());
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
//				System.out.println("[SocketCollector] Sending " + itsLog.size() + " bytes for "+getId());
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
		}
		
		public NakedLinkedList.Entry<SocketThreadData> getEntry()
		{
			return itsEntry;
		}

		public void shutDown() 
		{
			System.out.println("[TOD] Flushing events for thread " +
					getId() +
					", sending " +
					itsLog.size() +
					" bytes");
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
				itsOutputStream.flush();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
		
	private class MyShutdownHook extends Thread
	{
		public MyShutdownHook() 
		{
			super("Shutdown hook (SocketCollector)");
		}

		@Override
		public void run()
		{
			System.out.println("[TOD] Flushing events...");
			
			if (itsThreadDataList == null) return;
			for (SocketThreadData theData : itsThreadDataList) theData.shutDown();
			
			try
			{
				// Allow some time for buffers to be sent
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
			
			System.out.println("[TOD] Shutting down.");
		}
		
	}
	
	
	/**
	 * This thread periodically flushes the least recently flushed thread data.
	 * This permits to avoid data being cached for too long in waiting threads.
	 * @author gpothier
	 */
	private class SenderThread extends Thread
	{
		private NakedLinkedList<SocketThreadData> itsLRUList = 
			new NakedLinkedList<SocketThreadData>();
		
		public SenderThread()
		{
			super("SenderThread");
			setDaemon(true);
			setPriority(MAX_PRIORITY);
		}
		
		public synchronized Entry<SocketThreadData> register(SocketThreadData aData)
		{
			Entry<SocketThreadData> theEntry = itsLRUList.createEntry(aData);
			itsLRUList.addLast(theEntry);
			
			return theEntry;
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
						Entry<SocketThreadData> theFirstEntry;
						
						synchronized (this)
						{
							theFirstEntry = itsLRUList.getFirstEntry();
							itsLRUList.remove(theFirstEntry);
							itsLRUList.addLast(theFirstEntry);
						}
						
						SocketThreadData theFirst = theFirstEntry.getValue();
						
						theFirst.send();
					}

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
