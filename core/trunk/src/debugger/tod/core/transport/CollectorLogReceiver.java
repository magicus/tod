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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import tod.core.ILocationRegisterer;
import tod.core.ILogCollector;
import tod.core.database.structure.HostInfo;

/**
 * A {@link LogReceiver} that reads packets through a 
 * {@link CollectorPacketReader} and forwards messages to a 
 * {@link ILogCollector}.
 * @author gpothier
 */
public class CollectorLogReceiver extends LogReceiver
{
	private final ILogCollector itsCollector;
	private final ILocationRegisterer itsLocationRegistrer;
	
	/**
	 * Connects to an already running aplication through the specified socket.
	 * @param aSocket The socket used to connect.
	 * @param aCollector The collector to which the events are forwarded.
	 */
	public CollectorLogReceiver(
			HostInfo aHostInfo,
			ILogCollector aCollector,
			ILocationRegisterer aLocationRegistrer,
			InputStream aInStream,
			OutputStream aOutStream)
	{
		this(aHostInfo, aCollector, aLocationRegistrer, aInStream, aOutStream, true);
	}
		
	public CollectorLogReceiver(
			HostInfo aHostInfo,
			ILogCollector aCollector,
			ILocationRegisterer aLocationRegistrer,
			InputStream aInStream,
			OutputStream aOutStream,
			boolean aStart)
	{
		this(DEFAULT_THREAD, aHostInfo, aCollector, aLocationRegistrer, aInStream, aOutStream, aStart);
	}
	
	public CollectorLogReceiver(
			ReceiverThread aReceiverThread,
			HostInfo aHostInfo,
			ILogCollector aCollector,
			ILocationRegisterer aLocationRegistrer,
			InputStream aInStream,
			OutputStream aOutStream,
			boolean aStart)
	{
		super(aReceiverThread, aHostInfo, aInStream, aOutStream, false);
		itsCollector = aCollector;
		itsLocationRegistrer = aLocationRegistrer;
		if (aStart) start();
	}
	
	public ILogCollector getCollector()
	{
		return itsCollector;
	}
	
	public ILocationRegisterer getLocationRegistrer()
	{
		return itsLocationRegistrer;
	}
	
	protected void readPacket(DataInputStream aStream, MessageType aType) throws IOException
	{
		CollectorPacketReader.readPacket(
				aStream, 
				getCollector(),
				getLocationRegistrer(),
				aType);		
	}

}
