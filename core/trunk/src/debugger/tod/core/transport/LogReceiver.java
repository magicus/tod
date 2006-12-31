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

import tod.agent.DebugFlags;
import tod.core.bci.NativeAgentPeer;

/**
 * Receives log events from a logged application through a socket and
 * forwards them to a {@link tod.core.ILogCollector}.
 * It can be setup so that it connects to an already running application,
 * or to wait for incoming connections from applications
 * @author gpothier
 */
public abstract class LogReceiver extends Thread
{
	/**
	 * Name of the host that sends events
	 */
	private String itsHostName;
	
	private boolean itsEof = false;
	
	private ILogReceiverMonitor itsMonitor = null;
	
	/**
	 * Number of commands received.
	 */
	private long itsMessageCount = 0;
	
	private final InputStream itsInStream;
	private final OutputStream itsOutStream;
	
	public LogReceiver(InputStream aInStream, OutputStream aOutStream, boolean aStart)
	{
		itsInStream = aInStream;
		itsOutStream = aOutStream;
		if (aStart) start();
	}
	
	public void setMonitor(ILogReceiverMonitor aMonitor)
	{
		itsMonitor = aMonitor;
	}

	/**
	 * Returns the name of the currently connected host, or null
	 * if there is no connected host.
	 */
	public String getHostName()
	{
		return itsHostName;
	}
	
	private synchronized void setHostName(String aHostName)
	{
		itsHostName = aHostName;
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
			while (itsHostName == null) wait();
			return itsHostName;
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
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

	@Override
	public void run()
	{
		try
		{
			DataInputStream theStream = new DataInputStream(itsInStream);
			
			setHostName(theStream.readUTF());
			
			if (itsMonitor != null) itsMonitor.started();
			
			while (true)
			{
				try
				{
					byte theCommand = theStream.readByte();
					
					if (theCommand == NativeAgentPeer.INSTRUMENT_CLASS)
					{
						throw new RuntimeException();
//							RemoteInstrumenter.processInstrumentClassCommand(
//									itsInstrumenter,
//									itsStream,
//									theOutputStream,
//									null);
					}
					else
					{
						MessageType theType = MessageType.VALUES[theCommand];
						readPacket(theStream, theType);
					}
					
					itsMessageCount++;
					
					if (itsMonitor != null 
							&& DebugFlags.RECEIVER_PRINT_COUNTS > 0 
							&& itsMessageCount % 100000 == 0)
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
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected abstract void readPacket(DataInputStream aStream, MessageType aType) throws IOException;
	
	public interface ILogReceiverMonitor
	{
		public void started();
		public void processedMessages(long aCount);
	}
}