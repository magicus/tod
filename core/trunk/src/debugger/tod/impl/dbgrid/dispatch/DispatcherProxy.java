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
package tod.impl.dbgrid.dispatch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import tod.core.transport.LogReceiver;
import tod.core.transport.MessageType;
import tod.utils.ArrayCast;
import tod.utils.NativeStream;
import zz.utils.ArrayStack;
import zz.utils.Utils;

public class DispatcherProxy extends DispatchNodeProxy
{
	private BufferedSender itsSender;
	private BSOutputStream itsOut;
	private DataOutputStream itsDataOut;
	private DataInputStream itsDataIn;
	private byte[] itsBuffer = new byte[1024];
	private byte[] itsIBBuffer = new byte[4];
	
	public DispatcherProxy(
			RIDispatchNode aConnectable, 
			InputStream aInputStream,
			OutputStream aOutputStream,
			String aNodeId)
	{
		super(aConnectable, aNodeId);
		itsSender = new BufferedSender(aNodeId, aOutputStream);
		itsOut = new BSOutputStream(itsSender);
		itsDataOut = new DataOutputStream(itsOut);
		itsDataIn = new DataInputStream(aInputStream);
	}
	
	@Override
	protected DataInputStream getInStream()
	{
		return itsDataIn;
	}
	
	@Override
	protected DataOutputStream getOutStream()
	{
		return itsDataOut;
	}
	
	/**
	 * Returns the number of bytes queued in this proxy.
	 * This can be used for load balancing.
	 */
	public int getQueueSize()
	{
		return itsSender.getQueueSize();
	}
	
	/**
	 * Transfers a packet to the child node.
	 */
	public void forwardPacket(MessageType aType, DataInputStream aStream)
	{
		try
		{
			itsOut.write(aType.ordinal());
			
			// Read packet size
			aStream.readFully(itsIBBuffer);
			int theSize = ArrayCast.ba2i(itsIBBuffer);
			
			// Write packet size
			itsOut.write(itsIBBuffer);
			
//			int theSize = aStream.readInt();
//			getOutStream().writeInt(theSize);

			Utils.pipe(itsBuffer, aStream, itsOut, theSize);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void clear()
	{
		try
		{
			getOutStream().flush();
			getConnectable().clear();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public int flush()
	{
		try
		{
			System.out.println("[DispatcherProxy] Flushing "+getNodeId()+"...");
			getOutStream().writeByte(MessageType.CMD_FLUSH);
			getOutStream().flush();
			System.out.println("[DispatcherProxy] Waiting response for "+getNodeId()+"...");
			int theCount = getInStream().readInt();
			System.out.println("[DispatcherProxy] Flushed "+theCount+" events on "+getNodeId());
			
			return theCount;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Sends buffers of data to the remote end. Buffers are queued and send
	 * asynchronously.
	 * @author gpothier
	 */
	private static class BufferedSender extends Thread
	{
		private OutputStream itsStream;
		
		private LinkedList<Buffer> itsQueue = new LinkedList<Buffer>();
		private ArrayStack<Buffer> itsFreeBuffers = new ArrayStack<Buffer>();
		private int itsSize;
		
		private boolean itsFlushed = true;

		private final String itsNodeId;
		
		public BufferedSender(String aNodeId, OutputStream aStream)
		{
			itsNodeId = aNodeId;
			itsStream = aStream;
			for (int i=0;i<1024;i++) itsFreeBuffers.push(new Buffer(new byte[4096]));
			start();
		}

		public Buffer getFreeBuffer()
		{
			synchronized(itsFreeBuffers)
			{
				try
				{
					while (itsFreeBuffers.isEmpty()) itsFreeBuffers.wait();
					Buffer theBuffer = itsFreeBuffers.pop();
					theBuffer.reset();
					return theBuffer;
				}
				catch (InterruptedException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		
		private void addFreeBuffer(Buffer aBuffer)
		{
			synchronized (itsFreeBuffers)
			{
				itsFreeBuffers.push(aBuffer);
				itsFreeBuffers.notifyAll();
			}
		}
		
		public synchronized void pushBuffer(Buffer aBuffer)
		{
			itsQueue.addLast(aBuffer);
			itsSize += aBuffer.length;
			notifyAll();
		}
		
		private synchronized Buffer popBuffer()
		{
			try
			{
				while(itsQueue.isEmpty()) wait();
				Buffer theBuffer = itsQueue.removeFirst();
				itsSize -= theBuffer.length;
				notifyAll();
				return theBuffer;
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}

		/**
		 * Returns the number of bytes queued in this sender.
		 */
		protected int getQueueSize()
		{
			return itsSize;
		}
		
		public void waitFlushed()
		{
			try
			{
				synchronized (itsStream)
				{
					int s = getQueueSize();
					long t0 = System.currentTimeMillis();
					while(! itsFlushed) itsStream.wait();					
					long t1 = System.currentTimeMillis();
					float t = (t1-t0)/1000f;
					System.out.println(String.format(
							"[BufferedSender] Flushed %d bytes in %.2fs for node %s ",
							s,
							t,
							itsNodeId));
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public void run()
		{
			try
			{
				while(true)
				{
					Buffer theBuffer = popBuffer();
					itsStream.write(theBuffer.data, 0, theBuffer.length);
					if (theBuffer.flush)
					{
						synchronized (itsStream)
						{
							itsFlushed = false;
							itsStream.flush();
							itsFlushed = true;
							itsStream.notifyAll();
						}
					}
					else addFreeBuffer(theBuffer);
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	private static class Buffer
	{
		public final byte[] data;
		
		/**
		 * Amount of data used.
		 */
		public int length = 0;
		
		/**
		 * Whether this buffer is a flush marker
		 */
		public boolean flush = false; 
		
		public Buffer(byte[] aData)
		{
			data = aData;
		}
		
		public void reset()
		{
			length = 0;
			flush = false;
		}
	}
	
	/**
	 * An output stream that writes data to buffers of a {@link BufferedSender}.
	 * @author gpothier
	 */
	private static class BSOutputStream extends OutputStream
	{
		private BufferedSender itsSender;
		
		private Buffer itsCurrentBuffer;
		
		public BSOutputStream(BufferedSender aSender)
		{
			itsSender = aSender;
			newBuffer();
		}
		
		private void newBuffer()
		{
			if (itsCurrentBuffer != null) itsSender.pushBuffer(itsCurrentBuffer);
			itsCurrentBuffer = itsSender.getFreeBuffer();
		}

		@Override
		public void write(byte[] aB, int aOff, int aLen) 
		{
			int theRemaining = aLen;
			int theOffset = aOff;
			while(theRemaining > 0)
			{
				int theChunk = Math.min(theRemaining, itsCurrentBuffer.data.length-itsCurrentBuffer.length);
				System.arraycopy(aB, theOffset, itsCurrentBuffer.data, itsCurrentBuffer.length, theChunk);
				itsCurrentBuffer.length += theChunk;
				theOffset += theChunk;
				theRemaining -= theChunk;
				if (itsCurrentBuffer.length == itsCurrentBuffer.data.length) newBuffer();
			}
		}

		@Override
		public void write(int aB) 
		{
			if (itsCurrentBuffer.length == itsCurrentBuffer.data.length) newBuffer();
			itsCurrentBuffer.data[itsCurrentBuffer.length++] = (byte) aB;
		}
		
		@Override
		public void flush() 
		{
			itsCurrentBuffer.flush = true;
			itsSender.pushBuffer(itsCurrentBuffer);
			itsCurrentBuffer = null;
			itsSender.waitFlushed();
			newBuffer();
		}
	}
}
