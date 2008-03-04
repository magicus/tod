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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import tod.agent.transport.MessageType;
import tod.core.ILogCollector;
import tod.impl.database.structure.standard.HostInfo;

/**
 * A {@link LogReceiver} that reads packets through a 
 * {@link CollectorPacketReader} and forwards messages to a 
 * {@link ILogCollector}.
 * @author gpothier
 */
public abstract class CollectorLogReceiver extends LogReceiver
{
	private final ILogCollector itsCollector;
	
	/**
	 * Connects to an already running aplication through the specified socket.
	 * @param aSocket The socket used to connect.
	 * @param aCollector The collector to which the events are forwarded.
	 */
	public CollectorLogReceiver(
			HostInfo aHostInfo,
			ILogCollector aCollector,
			InputStream aInStream,
			OutputStream aOutStream)
	{
		this(aHostInfo, aCollector, aInStream, aOutStream, true);
	}
		
	public CollectorLogReceiver(
			HostInfo aHostInfo,
			ILogCollector aCollector,
			InputStream aInStream,
			OutputStream aOutStream,
			boolean aStart)
	{
		this(DEFAULT_THREAD, aHostInfo, aCollector, aInStream, aOutStream, aStart);
	}
	
	public CollectorLogReceiver(
			ReceiverThread aReceiverThread,
			HostInfo aHostInfo,
			ILogCollector aCollector,
			InputStream aInStream,
			OutputStream aOutStream,
			boolean aStart)
	{
		super(aReceiverThread, aHostInfo, aInStream, aOutStream, false);
		itsCollector = aCollector;
		if (aStart) start();
	}
	
	public ILogCollector getCollector()
	{
		return itsCollector;
	}
	
	protected void readPacket(DataInputStream aStream, MessageType aType) throws IOException
	{
		CollectorPacketReader.readPacket(
				aStream, 
				getCollector(),
				aType);		
	}

}
