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
package tod.core.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import tod.core.ILocationRegisterer;
import tod.core.LocationRegisterer;
import tod.core.bci.IInstrumenter;
import tod.core.config.TODConfig;
import tod.core.database.structure.HostInfo;
import tod.core.transport.CollectorLogReceiver;
import tod.core.transport.LogReceiver;

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
			ILocationRegisterer aLocationRegistrer,
			ICollectorFactory aCollectorFactory)
	{
		super(aConfig, aInstrumenter, aLocationRegistrer);
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
					LocationRegisterer.createSynchronizedRegistrer(getLocationRegistrer()),
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
