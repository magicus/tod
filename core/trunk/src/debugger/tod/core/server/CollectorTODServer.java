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
package tod.core.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import tod.core.bci.IInstrumenter;
import tod.core.config.TODConfig;
import tod.core.database.structure.IStructureDatabase;
import tod.core.transport.CollectorLogReceiver;
import tod.core.transport.LogReceiver;
import tod.impl.database.structure.standard.HostInfo;

/**
 * A TOD server that uses a {@link CollectorLogReceiver}
 * @author gpothier
 */
public class CollectorTODServer extends TODServer
{
	private int itsCurrentHostId = 1;

	private ICollectorFactory itsCollectorFactory;

	public CollectorTODServer(
			TODConfig aConfig, 
			IInstrumenter aInstrumenter, 
			IStructureDatabase aStructureDatabase,
			ICollectorFactory aCollectorFactory)
	{
		super(aConfig, aInstrumenter, aStructureDatabase);
		itsCollectorFactory = aCollectorFactory;
	}

	@Override
	protected LogReceiver createReceiver(Socket aSocket)
	{
		try
		{
			return new CollectorLogReceiver(
					new HostInfo(itsCurrentHostId++),
					itsCollectorFactory.create(),
					new BufferedInputStream(aSocket.getInputStream()), 
					new BufferedOutputStream(aSocket.getOutputStream()))
			{
				@Override
				protected synchronized void eof()
				{
					super.eof();
					CollectorTODServer.this.disconnected();
				}

				@Override
				protected int flush()
				{
					throw new UnsupportedOperationException();
				}
				
				@Override
				protected void clear()
				{
					throw new UnsupportedOperationException();
				}
			};
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
