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
package tod.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import tod.core.ILogCollector;
import tod.core.LocationRegisterer;
import tod.core.bci.IInstrumenter;
import tod.core.config.TODConfig;
import tod.core.server.CollectorTODServer;
import tod.core.server.ICollectorFactory;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.dbgrid.DebuggerGridConfig;
import zz.utils.Utils;

/**
 * A tod server that stores all the events it receives from the java client
 * @author gpothier
 */
public class StoreTODServer extends CollectorTODServer
{
	private int itsConnectionNumber = 1;

	public StoreTODServer(
			TODConfig aConfig, 
			IInstrumenter aInstrumenter,
			ICollectorFactory aCollectorFactory)
	{
		super(aConfig, aInstrumenter, new LocationRegisterer(), aCollectorFactory);
	}

	@Override
	protected void acceptJavaConnection(Socket aSocket)
	{
		String theFileName = DebuggerGridConfig.STORE_EVENTS_FILE;
		if (itsConnectionNumber > 1) theFileName += "."+itsConnectionNumber;

		new LogWriter(new File(theFileName), aSocket);
		itsConnectionNumber++;
	}
	
	private static class LogWriter extends Thread 
	{
		private File itsFile;
		private Socket itsSocket;
		
		public LogWriter(File aFile, Socket aSocket)
		{
			itsFile = aFile;
			itsSocket = aSocket;
			start();
		}

		@Override
		public void run()
		{
			try
			{
				while (itsSocket.getInputStream().available() == 0) sleep(500);
				
				System.out.println("Starting");
				long t0 = System.currentTimeMillis();
				
				OutputStream theStream = new BufferedOutputStream(new FileOutputStream(itsFile));
				InputStream theInputStream = itsSocket.getInputStream();
				Utils.pipe(theInputStream, theStream);
				
				theStream.flush();
				long t1 = System.currentTimeMillis();
				float dt = (t1-t0)/1000f;
				System.out.println("Writing finished ("+dt+"s)");
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	private static class DummyCollectorFactory implements ICollectorFactory
	{
		public ILogCollector create()
		{
			return null;
		}

		public void flushAll()
		{
		}
	}
	
	public static void main(String[] args)
	{
		TODConfig theConfig = new TODConfig();
		LocationRegisterer theLocationRegistrer = new LocationRegisterer();
		
		ASMDebuggerConfig theDebuggerConfig = new ASMDebuggerConfig(
				theConfig,
				theLocationRegistrer);
		
		System.out.println(theLocationRegistrer.getStats());

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theDebuggerConfig);
		
		new StoreTODServer(
				theConfig, 
				theInstrumenter,
				new DummyCollectorFactory()); 
	}

}
