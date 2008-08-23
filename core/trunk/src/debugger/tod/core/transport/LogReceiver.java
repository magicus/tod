/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.core.transport;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.agent.AgentConfig;
import tod.agent.AgentDebugFlags;
import tod.agent.transport.Commands;
import tod.agent.transport.LowLevelEventType;
import tod.core.DebugFlags;
import tod.impl.database.structure.standard.HostInfo;

/**
 * Receives (low-level) events from the debugged application through a socket.
 * @author gpothier
 */
public abstract class LogReceiver 
{
	public static final ReceiverThread DEFAULT_THREAD = new ReceiverThread();
	
	private ReceiverThread itsReceiverThread;
	
	private boolean itsStarted = false;
	
	/**
	 * Identification of the host that sends events
	 */
	private HostInfo itsHostInfo;
	
	private boolean itsEof = false;
	
	private ILogReceiverMonitor itsMonitor = null;
	
	/**
	 * Number of commands received.
	 */
	private long itsMessageCount = 0;
	
	private final InputStream itsInStream;
	private final OutputStream itsOutStream;

	private DataInputStream itsDataIn;
	private DataOutputStream itsDataOut;
	
	private final ByteBuffer itsHeaderBuffer;
	private final ByteBuffer itsDataBuffer;
	
	/**
	 * This map contains buffers that are used to reassemble long packets.
	 * It only contains the entry corresponding to a given thread if a long packet is currently being
	 * processed.
	 */
	private Map<Integer, ThreadPacketBuffer> itsThreadPacketBuffers = new HashMap<Integer, ThreadPacketBuffer>();
	
	public LogReceiver(
			HostInfo aHostInfo,
			InputStream aInStream, 
			OutputStream aOutStream, 
			boolean aStart)
	{
		this(DEFAULT_THREAD, aHostInfo, aInStream, aOutStream, aStart);
	}
	
	public LogReceiver(
			ReceiverThread aReceiverThread,
			HostInfo aHostInfo,
			InputStream aInStream, 
			OutputStream aOutStream, 
			boolean aStart)
	{
		itsReceiverThread = aReceiverThread;
		itsHostInfo = aHostInfo;
		itsInStream = aInStream;
		itsOutStream = aOutStream;
		
		itsDataIn = new DataInputStream(itsInStream);
		itsDataOut = new DataOutputStream(itsOutStream);
		
		itsHeaderBuffer = ByteBuffer.allocate(9);
		itsHeaderBuffer.order(ByteOrder.nativeOrder());
		
		itsDataBuffer = ByteBuffer.allocate(AgentConfig.COLLECTOR_BUFFER_SIZE);
		itsDataBuffer.order(ByteOrder.nativeOrder());
		
		itsReceiverThread.register(this);
		if (aStart) start();
	}
	
	public void start()
	{
		itsStarted = true;
		synchronized (itsReceiverThread)
		{
			itsReceiverThread.notifyAll();
		}
	}
	
	public void disconnect()
	{
		try
		{
			itsInStream.close();
			itsOutStream.close();
			eof();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private boolean isStarted()
	{
		return itsStarted;
	}
	
	public void setMonitor(ILogReceiverMonitor aMonitor)
	{
		itsMonitor = aMonitor;
	}	
	
	/**
	 * Returns the identification of the currently connected host.
	 */
	public HostInfo getHostInfo()
	{
		return itsHostInfo;
	}

	/**
	 * Returns the name of the currently connected host, or null
	 * if there is no connected host.
	 */
	public String getHostName()
	{
		return itsHostInfo != null ? itsHostInfo.getName() : null;
	}
	
	private synchronized void setHostName(String aHostName)
	{
		itsHostInfo.setName(aHostName);
		notifyAll();
	}
	
	protected synchronized void eof()
	{
		itsEof = true;
		notifyAll();
	}
	
	/**
	 * Waits until the host name is available, and returns it.
	 * See {@link #getHostName()}
	 */
	public synchronized String waitHostName()
	{
		try
		{
			while (getHostName() == null) wait();
			return getHostName();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public boolean isEof()
	{
		return itsEof;
	}
	
	/**
	 * Waits until the input stream terminates
	 */
	public synchronized void waitEof()
	{
		try
		{
			while (! itsEof) wait();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the total number of messages received by this receiver.
	 */
	public long getMessageCount()
	{
		return itsMessageCount;
	}
	
	/**
	 * Processes currently pending data.
	 * @return Whether there was data to process.
	 */
	private boolean process() throws IOException
	{
		return process(itsDataIn, itsDataOut);
	}
	
	protected boolean process(DataInputStream aDataIn, DataOutputStream aDataOut) throws IOException
	{
		if (DebugFlags.SWALLOW)
		{
			byte[] theBuffer = new byte[4096];
			while(true)
			{
				int theRead = aDataIn.read(theBuffer);
				if (theRead == -1)
				{
					eof();
					return true;
				}
			}
		}
		
		try
		{
			if (aDataIn.available() == 0) 
			{
				if (DebugFlags.REPLAY_MODE) eof();
				return false;
			}
		}
		catch (IOException e1)
		{
			eof();
		}
		
		if (getHostName() == null)
		{
			setHostName(aDataIn.readUTF());
			if (itsMonitor != null) itsMonitor.started();
		}

		while(aDataIn.available() != 0)
		{
			try
			{
				// Read and decode meta-packet header 
				aDataIn.readFully(itsHeaderBuffer.array());
				itsHeaderBuffer.position(0);
				itsHeaderBuffer.limit(9);
				
				int theThreadId = itsHeaderBuffer.getInt(); 
				int theSize = itsHeaderBuffer.getInt(); 
				int theFlags = itsHeaderBuffer.get();
				
				// These flags indicate if the beginning (resp. end) of the metapacket
				// correspond to the beginning (resp. end) of a real packet.
				// (otherwise, it means the real packets span several metapackets).
				boolean theCleanStart = (theFlags & 2) != 0;
				boolean theCleanEnd = (theFlags & 1) != 0;
				
				aDataIn.readFully(itsDataBuffer.array(), 0, theSize);
				itsDataBuffer.position(0);
				itsDataBuffer.limit(theSize);
				
				if (! theCleanEnd)
				{
					ThreadPacketBuffer theBuffer = itsThreadPacketBuffers.get(theThreadId);
					if (theBuffer == null)
					{
						assert theCleanStart;
						
						theBuffer = new ThreadPacketBuffer();
						
						if (AgentDebugFlags.TRANSPORT_LONGPACKETS_LOG)
							System.out.println("[LogReceiver] Starting long packet for thread "+theThreadId);
						
						itsThreadPacketBuffers.put(theThreadId, theBuffer);
					}
					else
					{
						assert ! theCleanStart;
					}
					
					if (AgentDebugFlags.TRANSPORT_LONGPACKETS_LOG)
						System.out.println("[LogReceiver] Long packet for thread "+theThreadId+", appending "+theSize+" bytes");
					
					theBuffer.append(itsDataBuffer.array(), 0, itsDataBuffer.remaining());
				}
				else
				{
					ThreadPacketBuffer theBuffer = itsThreadPacketBuffers.remove(theThreadId);
					if (theBuffer != null)
					{
						// Process outstanding long packet.
						assert ! theCleanStart;
						
						if (AgentDebugFlags.TRANSPORT_LONGPACKETS_LOG)
							System.out.println("[LogReceiver] Long packet for thread "+theThreadId+", appending "+theSize+" bytes");
						
						theBuffer.append(itsDataBuffer.array(), 0, itsDataBuffer.remaining());
						
						BufferDataInput theStream = new BufferDataInput(theBuffer.toByteBuffer());
						
						if (AgentDebugFlags.TRANSPORT_LONGPACKETS_LOG)
							System.out.println("[LogReceiver] Starting to process long packet for thread "+theThreadId+": "+theBuffer);
						
						processThreadPackets(theThreadId, theStream, aDataOut, AgentDebugFlags.TRANSPORT_LONGPACKETS_LOG);
					}
					else
					{
						assert theCleanStart;
						
						BufferDataInput theStream = new BufferDataInput(itsDataBuffer);
						processThreadPackets(theThreadId, theStream, aDataOut, false);
					}
				}
			}
			catch (EOFException e)
			{
				System.err.println("LogReceiver: EOF (msg #"+itsMessageCount+")");
				eof();
				break;
			}
			catch (Exception e)
			{
				System.err.println("Exception in LogReceiver.process (msg #"+itsMessageCount+"):");
				e.printStackTrace();
				eof();
				break;
			}
		}
		
		return true;
	}

	protected void processThreadPackets(
			int aThreadId, 
			BufferDataInput aStream, 
			DataOutputStream aDataOut,
			boolean aLogPackets)
	throws IOException
	{
		while(aStream.hasMore())
		{
			int theMessage = aStream.readByte();
//			System.out.println("[LogReceiver] Command: "+theCommand);
			itsMessageCount++;

			if (DebugFlags.MAX_EVENTS > 0 && itsMessageCount > DebugFlags.MAX_EVENTS)
			{
				eof();
				break;
			}
			
			if (theMessage >= Commands.BASE)
			{
				Commands theCommand = Commands.VALUES[theMessage-Commands.BASE];
				switch (theCommand)
				{
				case CMD_FLUSH:
					System.out.println("[LogReceiver] Received flush request.");
					int theCount = processFlush();
					aDataOut.writeInt(theCount);
					aDataOut.flush();
					break;
					
				case CMD_CLEAR:
					System.out.println("[LogReceiver] Received clear request.");
					processFlush();
					processClear();
					break;
					
				default: throw new RuntimeException("Not handled: "+theCommand); 
				}

			}
			else
			{
				LowLevelEventType theType = LowLevelEventType.VALUES[theMessage];
				if (aLogPackets) System.out.println("[LogReceiver] Processing "+theType+" (remaining: "+aStream.remaining()+")");
				processEvent(aThreadId, theType, aStream);
				if (aLogPackets) System.out.println("[LogReceiver] Done processing "+theType+" (remaining: "+aStream.remaining()+")");
			}
			
			if (itsMonitor != null 
					&& DebugFlags.RECEIVER_PRINT_COUNTS > 0 
					&& itsMessageCount % DebugFlags.RECEIVER_PRINT_COUNTS == 0)
			{
				itsMonitor.processedMessages(itsMessageCount);
			}
		}
	}
	
	/**
	 * Reads and processes an incoming event packet for the given thread.
	 */
	protected abstract void processEvent(int aThreadId, LowLevelEventType aType, DataInput aStream) throws IOException;
	
	/**
	 * Flushes buffered events.
	 * @return Number of flushed events
	 */
	protected abstract int processFlush();
	
	/**
	 * Clears the database.
	 */
	protected abstract void processClear();
	
	
	/**
	 * This is a buffer for long packets, ie. packets that span more than
	 * one meta-packet.
	 * @author gpothier
	 */
	private static class ThreadPacketBuffer
	{
		private final ByteArrayOutputStream itsBuffer = new ByteArrayOutputStream();
		
		public void append(byte[] aBuffer, int aOffset, int aLength)
		{
			itsBuffer.write(aBuffer, aOffset, aLength);
		}
		
		public ByteBuffer toByteBuffer()
		{
			ByteBuffer theBuffer = ByteBuffer.wrap(itsBuffer.toByteArray());
			theBuffer.order(ByteOrder.nativeOrder());
			return theBuffer;
		}
		
		@Override
		public String toString()
		{
			return "ThreadPacketBuffer: "+itsBuffer.size()+" bytes";
		}
	}
	
	public interface ILogReceiverMonitor
	{
		public void started();
		public void processedMessages(long aCount);
	}
	
	/**
	 * This is the thread that actually processes the streams.
	 * Having a unique thread permits to avoid synchronization
	 * problems further in the stream.
	 * @author gpothier
	 */
	public static class ReceiverThread extends Thread
	{
		public ReceiverThread()
		{
			super("LogReceiver.ReceiverThread");
			start();
		}
		
		private List<LogReceiver> itsReceivers = new ArrayList<LogReceiver>();
		
		public synchronized void register(LogReceiver aReceiver)
		{
			itsReceivers.add(aReceiver);
			notifyAll();
		}
		
		@Override
		public void run()
		{
			try
			{
				int theWait = 1;
				while(true)
				{
					// We don't use an iterator so as to avoid concurrent modif. exceptions
					for(int i=0;i<itsReceivers.size();i++)
					{
						LogReceiver theReceiver = itsReceivers.get(i);
						if (! theReceiver.isStarted()) continue;
						
						try
						{
							if (theReceiver.process()) theWait = 1;
						}
						catch (IOException e)
						{	
							System.err.println("Exception while processing receiver of "+theReceiver.getHostName());
							e.printStackTrace();
							theReceiver.processFlush();
						}
						
						if (theReceiver.isEof()) itsReceivers.remove(i);
					}
					
					if (theWait > 1) 
					{
						synchronized (this)
						{
							wait(theWait);
						}
					}
					
					theWait *= 2;
					theWait = Math.min(theWait, 100);
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Wraps a {@link ByteBuffer} in a {@link DataInput}.
	 * @author gpothier
	 */
	private static class BufferDataInput implements DataInput
	{
		private final ByteBuffer itsBuffer;

		public BufferDataInput(ByteBuffer aBuffer)
		{
			itsBuffer = aBuffer;
		}

		public boolean hasMore()
		{
			return itsBuffer.remaining() > 0;
		}
		
		public int remaining()
		{
			return itsBuffer.remaining();
		}
		
		public boolean readBoolean()
		{
			return itsBuffer.get() != 0;
		}

		public byte readByte() 
		{
			return itsBuffer.get();
		}

		public char readChar() 
		{
			return itsBuffer.getChar();
		}

		public double readDouble() 
		{
			return itsBuffer.getDouble();
		}

		public float readFloat() 
		{
			return itsBuffer.getFloat();
		}

		public void readFully(byte[] aB, int aOff, int aLen) 
		{
			itsBuffer.get(aB, aOff, aLen);
		}

		public void readFully(byte[] aB) 
		{
			readFully(aB, 0, aB.length);
		}

		public int readInt() 
		{
			return itsBuffer.getInt();
		}

		public String readLine() 
		{
			throw new UnsupportedOperationException();
		}

		public long readLong() 
		{
			return itsBuffer.getLong();
		}

		public short readShort() 
		{
			return itsBuffer.getShort();
		}

		public int readUnsignedByte() 
		{
			throw new UnsupportedOperationException();
		}

		public int readUnsignedShort() 
		{
			throw new UnsupportedOperationException();
		}

		public String readUTF() 
		{
			int theSize = readInt();
			char[] theChars = new char[theSize];
			for(int i=0;i<theSize;i++) theChars[i] = readChar();
			return new String(theChars);
		}

		public int skipBytes(int aN) 
		{
			throw new UnsupportedOperationException();
		}
		
	}
}