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
package tod.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import tod.core.ILogCollector;
import tod.core.bci.IInstrumenter;
import tod.core.config.TODConfig;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.server.CollectorTODServer;
import tod.core.server.ICollectorFactory;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.database.structure.standard.StructureDatabase;
import zz.utils.Utils;

/**
 * A tod server that stores all the events it receives from the java client
 * @author gpothier
 */
public class StoreTODServer extends CollectorTODServer
{
	public static final String STORE_EVENTS_FILE =
		ConfigUtils.readString("events-file", "events-raw.bin");
	
	private int itsConnectionNumber = 1;

	public StoreTODServer(
			TODConfig aConfig, 
			IInstrumenter aInstrumenter,
			IStructureDatabase aStructureDatabase,
			ICollectorFactory aCollectorFactory)
	{
		super(aConfig, aInstrumenter, aStructureDatabase, aCollectorFactory);
	}

	@Override
	protected void acceptJavaConnection(Socket aSocket)
	{
		String theFileName = STORE_EVENTS_FILE;
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
	}
	
	public static void main(String[] args)
	{
		TODConfig theConfig = new TODConfig();
		IMutableStructureDatabase theStructureDatabase = StructureDatabase.create(theConfig, "StoreTODServer");
		
		ASMDebuggerConfig theDebuggerConfig = new ASMDebuggerConfig(theConfig);
		
//		System.out.println(theStructureDatabase.getStats());

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theStructureDatabase, theDebuggerConfig);
		
		new StoreTODServer(
				theConfig, 
				theInstrumenter,
				theStructureDatabase,
				new DummyCollectorFactory()); 
	}

}
