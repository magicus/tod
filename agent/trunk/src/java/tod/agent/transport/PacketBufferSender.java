/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.agent.transport;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import tod.agent.AgentConfig;
import tod.agent.AgentDebugFlags;
import tod.agent.AgentUtils;
import tod.agent.EventCollector;
import tod.agent._AgentConfig;

/**
 * Sends the packets buffered by a set of {@link PacketBuffer} to a given
 * {@link ByteChannel}.
 * Packets of a given thread are sent together, prefixed by a header that indicates the 
 * size of the "meta-packet", the corresponding thread id, and whether the meta-packet contains
 * split packets.  
 * @author gpothier
 */
public class PacketBufferSender extends Thread
{
	private final ByteChannel itsOutputChannel;
	
	/**
	 * Used to send the header of each meta-packet.
	 */
	private final ByteBuffer itsHeaderBuffer;
	
	/**
	 * Buffers that are waiting to be sent.
	 */
	private final LinkedList<PacketBuffer> itsPendingBuffers = new LinkedList<PacketBuffer>();
	
	/**
	 * This list contains all the packet buffers created by this sender.
	 */
	private final List<PacketBuffer> itsBuffers = new ArrayList<PacketBuffer>();

	private final MyShutdownHook itsShutdownHook;

	public PacketBufferSender(ByteChannel aOutputChannel)
	{
		super("[TOD] Packet buffer sender");
		setDaemon(true);
		assert aOutputChannel != null;
		itsOutputChannel = aOutputChannel;
		
		itsHeaderBuffer = ByteBuffer.allocate(9);
		itsHeaderBuffer.order(ByteOrder.nativeOrder());
		
		itsShutdownHook = new MyShutdownHook();
		Runtime.getRuntime().addShutdownHook(itsShutdownHook);
		
		start();
	}
	
	/**
	 * Assertions don't seem to work in bootclasspath code...
	 */
	public static void _assert(boolean aValue)
	{
		if (! aValue) throw new AssertionError();
	}

	@Override
	public void run()
	{
		try
		{
			// Time at which last stale buffer check was performed
			long checkTime = System.currentTimeMillis();
			
			// Number of buffers that were sent since last timestamp was taken (taking timestamps is costly)
			int sentBuffers = 0;
			while(true)
			{
				PacketBuffer thePendingBuffer = popBuffer();
				
				if (thePendingBuffer != null)
				{
					sentBuffers++;
					
					ByteBuffer theByteBuffer = thePendingBuffer.getPendingBuffer();
					_assert (theByteBuffer != null);
					
					int theSize = theByteBuffer.position();
					int theId = thePendingBuffer.getThreadId();
	
					itsHeaderBuffer.clear();
					itsHeaderBuffer.putInt(theId);
					itsHeaderBuffer.putInt(theSize);
					
					int theFlags = 
						(thePendingBuffer.hasCleanStart() ? 2 : 0) 
						| (thePendingBuffer.hasCleanEnd() ? 1 : 0);
					
					System.out.println(String.format(
							"[TOD-PacketBufferSender] Sending packet (th: %d, sz: %d, cs: %s, ce: %s)",
							theId,
							theSize,
							thePendingBuffer.hasCleanStart(),
							thePendingBuffer.hasCleanEnd()));
					
					itsHeaderBuffer.put((byte) theFlags);
					
					itsHeaderBuffer.flip();
					itsOutputChannel.write(itsHeaderBuffer);
					
					theByteBuffer.flip();
					itsOutputChannel.write(theByteBuffer);
					
					theByteBuffer.clear();
					
					thePendingBuffer.sent();
				}
				
				if (thePendingBuffer == null || sentBuffers > 100)
				{
					// Check stale buffers at a regular interval
					long t = System.currentTimeMillis();
					long delta = t - checkTime;
					
					if (delta > 100)
					{
						checkTime = t;
						
						PacketBuffer[] theArray;
						synchronized(this)
						{
							// Might deadlock if we synchronize the pleaseSwaps, so copy the list
							theArray = itsBuffers.toArray(new PacketBuffer[itsBuffers.size()]);
						}
						
						for (PacketBuffer theBuffer : theArray) theBuffer.pleaseSwap();
					}
					
					sentBuffers = 0;
				}
			}
		}
		catch (Exception e)
		{
			System.err.println("[TOD] FATAL:");
			e.printStackTrace();
			Runtime.getRuntime().removeShutdownHook(itsShutdownHook);
			System.exit(1);
		}
	}
	
	/**
	 * Retroweaver does not handle {@link Queue#offer(Object)}
	 */
	private static <T> boolean queueOffer(LinkedList<T> aList, T aElement)
	{
		if (_AgentConfig.JAVA14) return aList.add(aElement);
		else return aList.offer(aElement);
	}
	
	/**
	 * Retroweaver does not handle {@link Queue#poll()}
	 */
	private static <T> T queuePoll(LinkedList<T> aList)
	{
		if (_AgentConfig.JAVA14)
		{
	        if (aList.size()==0) return null;
	        return aList.removeFirst();

		}
		else return aList.poll();
	}
	
	/**
	 * Pushes the given packet buffer to the pending queue.
	 */
	synchronized void pushBuffer(PacketBuffer aBuffer) 
	{
		queueOffer(itsPendingBuffers, aBuffer);
		notifyAll();
	}
	
	/**
	 * Pops a buffer from the stack, waiting for up to 100ms if none is available.
	 */
	synchronized PacketBuffer popBuffer() throws InterruptedException
	{
		if (itsPendingBuffers.isEmpty()) wait(100);
		return queuePoll(itsPendingBuffers);
	}
	
	public synchronized PacketBuffer createBuffer(int aThreadId)
	{
		PacketBuffer thePacketBuffer = new PacketBuffer(aThreadId);
		itsBuffers.add(thePacketBuffer);
		return thePacketBuffer;
	}
	
	/**
	 * Emulates a {@link DataOutputStream} to which event packets can be sent.
	 * Uses double buffering to handle sending of buffers.
	 * @author gpothier
	 */
	public class PacketBuffer 
	{
		/**
		 * Id of the thread that uses this buffer.
		 */
		private final int itsThreadId;
		
		/**
		 * The buffer currently written to.
		 * When this buffer is full, buffers are swapped. See {@link #swapBuffers()}
		 */
		private ByteBuffer itsCurrentBuffer;
		
		/**
		 * The "reserve" buffer.
		 */
		private ByteBuffer itsOtherBuffer;
		
		/**
		 * The buffer that is pending to be sent, if any.
		 */
		private ByteBuffer itsPendingBuffer;
		
		/**
		 * True if the pending buffer starts with a new packet
		 */
		private boolean itsPendingCleanStart = true;
		
		/**
		 * True if the pending buffer ends at the end of a packet.
		 */
		private boolean itsPendingCleanEnd = true;
		
		private boolean itsCurrentCleanStart = true;
		private boolean itsCurrentCleanEnd = true;
		
		PacketBuffer(int aThreadId)
		{
			itsThreadId = aThreadId;
			
			itsCurrentBuffer = ByteBuffer.allocate(AgentConfig.COLLECTOR_BUFFER_SIZE);
			itsCurrentBuffer.order(ByteOrder.nativeOrder());
			
			itsOtherBuffer = ByteBuffer.allocate(AgentConfig.COLLECTOR_BUFFER_SIZE);
			itsOtherBuffer.order(ByteOrder.nativeOrder());
		}

		public int getThreadId()
		{
			return itsThreadId;
		}

		public ByteBuffer getPendingBuffer()
		{
			return itsPendingBuffer;
		}

		public boolean hasCleanStart()
		{
			return itsPendingCleanStart;
		}

		public boolean hasCleanEnd()
		{
			return itsPendingCleanEnd;
		}

		/**
		 * Remaining bytes in the current buffer.
		 */
		private int remaining()
		{
			return itsCurrentBuffer.remaining();
		}
		
		/**
		 * This method is called periodically to prevent data from being held in buffers
		 * for too long when a thread is inactive.
		 * If this buffer has not been swapped for a long time, this method swaps it.
		 */
		public void pleaseSwap()
		{
			if (itsCurrentBuffer.position() == 0) return;
			synchronized (this)
			{
				if (itsPendingBuffer == null) swapBuffers();
			}
		}
		
		/**
		 * Sends the content of the current buffer and swaps the buffers.
		 * This method might have to wait for the buffer to be sent.
		 */
		public synchronized void swapBuffers()
		{
			try
			{
				while (itsPendingBuffer != null) wait();
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
			
			// Another thread might have called swapBuffers 
			// during the above wait.
			if (itsCurrentBuffer.position() == 0) return;
			
			itsPendingBuffer = itsCurrentBuffer;
			itsPendingCleanStart = itsCurrentCleanStart;
			itsPendingCleanEnd = itsCurrentCleanEnd;
			
			itsCurrentBuffer = itsOtherBuffer;
			itsCurrentCleanStart = true;
			itsCurrentCleanEnd = true;
			
			itsOtherBuffer = null;
			PacketBufferSender.this.pushBuffer(this);
		}
		
		public synchronized void sent()
		{
			itsOtherBuffer = itsPendingBuffer;
			itsPendingBuffer = null;
			notifyAll();
		}

		public synchronized void write(byte[] aBuffer, int aLength, boolean aCanSplit)
		{
			int theOffset = 0;
			
			// Note: we don't want to split small packets, hence the second condition
			if (! aCanSplit || aLength <= AgentConfig.COLLECTOR_BUFFER_SIZE)
			{
				if (remaining() < aLength) swapBuffers();
				_assert (remaining() >= aLength);
			}
			
			if (remaining() >= aLength)
			{
				// The packet will not be split
				itsCurrentBuffer.put(aBuffer, theOffset, aLength);
				if (remaining() == 0) swapBuffers();
			}
			else
			{
				// The packet is split
				if (AgentDebugFlags.TRANSPORT_LONGPACKETS_LOG) 
					System.out.println("[TOD-PacketBufferSender] Starting long packet for thread "+itsThreadId+" ("+aLength+" bytes)");
				
				while (aLength > 0)
				{
					int theCount = Math.min(aLength, remaining());
					if (theCount > 0)
					{
						itsCurrentBuffer.put(aBuffer, theOffset, theCount);
						theOffset += theCount;
						aLength -= theCount;
					}

					if (AgentDebugFlags.TRANSPORT_LONGPACKETS_LOG) 
						System.out.println("[TOD-PacketBufferSender] Long packet for thread "+itsThreadId+": sent "+theCount+" bytes");

					if (aLength > 0) itsCurrentCleanEnd = false;
					swapBuffers(); // Swap anyway here - we want to start a fresh packet after the long one.
					if (aLength > 0) itsCurrentCleanStart = false; // This must be after the swap.
				}
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
			System.out.println("[TOD] Flushing buffers...");
			
			AgentConfig.getCollector().end();
			
			for (PacketBuffer theBuffer : itsBuffers) theBuffer.swapBuffers();
			
			try
			{
				int thePrevSize = itsPendingBuffers.size();
				while(thePrevSize > 0)
				{
					Thread.sleep(200);
					int theNewSize = itsPendingBuffers.size();
					if (theNewSize == thePrevSize)
					{
						System.err.println("[TOD] Buffers are not being sent, shutting down anyway ("+theNewSize+" buffers remaining).");
						return;
					}
					thePrevSize = theNewSize;
				}
				
				// Give some more time to allow for the buffers to be sent
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
			
			System.out.println("[TOD] Shutting down.");
		}
	}
	

}
