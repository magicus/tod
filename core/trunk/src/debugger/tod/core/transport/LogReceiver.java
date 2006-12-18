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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import tod.core.ILocationRegistrer;
import tod.core.ILogCollector;
import tod.core.bci.NativeAgentPeer;

/**
 * Receives log events from a logged application through a socket and
 * forwards them to a {@link tod.core.ILogCollector}.
 * It can be setup so that it connects to an already running application,
 * or to wait for incoming connections from applications
 * @author gpothier
 */
public class LogReceiver extends SocketThread
{
	private final ILogCollector itsCollector;
	private final ILocationRegistrer itsLocationRegistrer;
	private DataInputStream itsStream;
	
	/**
	 * Name of the host that sends events
	 */
	private String itsHostName;
	
	/**
	 * Waits for incoming connections on the specified socket
	 * @param aCollector The collector to which the events are forwarded.
	 */
	public static void server (
			ILogCollector aCollector,
			ILocationRegistrer aLocationRegistrer,
			int aPort) throws IOException
	{
		new LogReceiver(aCollector, aLocationRegistrer, new ServerSocket(aPort));
	}
	
	/**
	 * Waits for incoming connections on the specified socket
	 * @param aCollector The collector to which the events are forwarded.
	 */
	public LogReceiver(
			ILogCollector aCollector,
			ILocationRegistrer aLocationRegistrer,
			ServerSocket aServerSocket)
	{
		super(aServerSocket);
		itsCollector = aCollector;
		itsLocationRegistrer = aLocationRegistrer;
	}

	/**
	 * Connects to an already running aplication through the specified socket.
	 * @param aSocket The socket used to connect.
	 * @param aCollector The collector to which the events are forwarded.
	 */
	public LogReceiver(
			ILogCollector aCollector,
			ILocationRegistrer aLocationRegistrer,
			Socket aSocket)
	{
		super(aSocket);
		itsCollector = aCollector;
		itsLocationRegistrer = aLocationRegistrer;
	}
	
	public ILogCollector getCollector()
	{
		return itsCollector;
	}
	
	
	public ILocationRegistrer getLocationRegistrer()
	{
		return itsLocationRegistrer;
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

	@Override
	protected void disconnected()
	{
		setHostName(null);
	}

	protected void process(
			OutputStream aOutputStream, 
			InputStream aInputStream) 
			throws IOException
	{
		itsStream = new DataInputStream(new BufferedInputStream(aInputStream));
		
		if (itsHostName == null)
		{
			setHostName(itsStream.readUTF());
		}
		
		while (true)
		{
			byte theCommand = itsStream.readByte();
			
			if (theCommand == NativeAgentPeer.INSTRUMENT_CLASS)
			{
				throw new RuntimeException();
//				RemoteInstrumenter.processInstrumentClassCommand(
//						itsInstrumenter,
//						itsStream,
//						theOutputStream,
//						null);
			}
			else
			{
				MessageType theType = MessageType.values()[theCommand];
				CollectorPacketReader.readPacket(
						itsStream, 
						getCollector(),
						getLocationRegistrer(),
						theType);
			}
		}
	}
	
	
}