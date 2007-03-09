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
package tod.core.transport;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import tod.agent.DebugFlags;
import tod.core.database.structure.HostInfo;
import tod.impl.dbgrid.DebuggerGridConfig;

/**
 * Receives log events from a logged application through a socket and
 * forwards them to a {@link tod.core.ILogCollector}.
 * It can be setup so that it connects to an already running application,
 * or to wait for incoming connections from applications
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

	private DataInputStream itsDataStream;
	
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
		
		itsDataStream = new DataInputStream(itsInStream);
		
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
		if (DebugFlags.SKIP_EVENTS)
		{
			byte[] theBuffer = new byte[4096];
			while(true)
			{
				itsDataStream.read(theBuffer);
			}
		}
		
		try
		{
			if (itsDataStream.available() == 0) return false;
		}
		catch (IOException e1)
		{
			eof();
		}
		
		if (getHostName() == null)
		{
			setHostName(itsDataStream.readUTF());
			if (itsMonitor != null) itsMonitor.started();
		}

		while(itsDataStream.available() != 0)
		{
			try
			{
				byte theCommand = itsDataStream.readByte();
				
				MessageType theType = MessageType.VALUES[theCommand];
				readPacket(itsDataStream, theType);
				
				itsMessageCount++;
				
				if (DebugFlags.MAX_EVENTS > 0 && itsMessageCount > DebugFlags.MAX_EVENTS)
				{
					eof();
					break;
				}
				
				if (itsMonitor != null 
						&& DebugFlags.RECEIVER_PRINT_COUNTS > 0 
						&& itsMessageCount % DebugFlags.RECEIVER_PRINT_COUNTS == 0)
				{
					itsMonitor.processedMessages(itsMessageCount);
				}
			}
			catch (EOFException e)
			{
				System.err.println("LogReceiver: EOF");
				eof();
				break;
			}
			catch (Exception e)
			{
				System.err.println("Exception in LogReceiver.process:");
				e.printStackTrace();
				eof();
				break;
			}
		}
		
		return true;
	}

	/**
	 * Read and interpret an incoming packet.
	 */
	protected abstract void readPacket(DataInputStream aStream, MessageType aType) throws IOException;
	
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
}