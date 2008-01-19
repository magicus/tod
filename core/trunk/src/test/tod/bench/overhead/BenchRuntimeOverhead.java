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
package tod.bench.overhead;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import tod.agent.transport.MessageType;
import tod.core.bci.IInstrumenter;
import tod.core.config.TODConfig;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.server.TODServer;
import tod.core.transport.LogReceiver;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.database.structure.standard.HostInfo;
import tod.impl.database.structure.standard.StructureDatabase;

/**
 * Measures the runtime overhead caused by instrumentation.
 * @author gpothier
 */
public class BenchRuntimeOverhead
{
	public static void main(String[] args)
	{
		TODServer theServer = SinkTODServer.create(new TODConfig());

		long theTODTime = getTime(true, 1);
		long theNormalTime = getTime(false, 10);
		
		System.out.println("Overhead: "+(theTODTime/theNormalTime));
		System.exit(0);
	}
	
	private static long getTime(boolean aWithTOD, int aTries)
	{
		long theAvg = 0;
		for(int i=0;i<aTries;i++) theAvg += getTime(aWithTOD);
		return theAvg/aTries;
	}
	
	private static String getJavaExecutable()
	{
		String theOs = System.getProperty("os.name");
		
		String theJVM = System.getProperty("java.home")+"/bin/java";
		if (theOs.contains("Windows")) theJVM += "w.exe";
			
		return theJVM;
	}
	
	private static long getTime(boolean aWithTod)
	{
		try
		{
			String theAgentOption = aWithTod ? "-agentpath:./libbci-agent.so" : "-DX=X";
			String theBootOption = aWithTod ? "-Xbootclasspath/p:../TOD-agent/bin" : "-DY=Y";
			
			String theJVM = getJavaExecutable();
			ProcessBuilder theBuilder = new ProcessBuilder(
					theJVM,
					"-cp", "bin",
					theAgentOption,
					theBootOption,
					"-noverify",
					"-Dcollector-host=localhost",
					"-Dcollector-port=8058", 
					"-Dnative-port=8059",
					"-Dtod-host=tod-1",
					"-Dcollector-type=socket",
					"-server",
					"-Dagent-verbose=0",
					"-Dagent-cache-path=/home/gpothier/tmp/tod",
					"tod.bench.overhead.Dummy");
			
			theBuilder.redirectErrorStream(true);
			
			Process theProcess = theBuilder.start();
			ProcessOutWatcher theWatcher = new ProcessOutWatcher(theProcess.getInputStream());

			theProcess.waitFor();
			
			System.out.println("Done!");
			
			return theWatcher.getTime();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	

	
	public static class SinkTODServer extends TODServer
	{
		private SinkTODServer(TODConfig aConfig, IInstrumenter aInstrumenter, IStructureDatabase aStructureDatabase)
		{
			super(aConfig, aInstrumenter, aStructureDatabase);
		}
		
		public static SinkTODServer create(TODConfig aConfig)
		{
			IMutableStructureDatabase theStructureDatabase = StructureDatabase.create(aConfig);
			ASMDebuggerConfig theConfig = new ASMDebuggerConfig(aConfig);
			ASMInstrumenter theInstrumenter = new ASMInstrumenter(theStructureDatabase, theConfig);
			return new SinkTODServer(aConfig, theInstrumenter, theStructureDatabase);
		}

		@Override
		protected LogReceiver createReceiver(Socket aSocket)
		{
			try
			{
				return new SinkReceiver(
						new HostInfo(1),
						new BufferedInputStream(aSocket.getInputStream()), 
						new BufferedOutputStream(aSocket.getOutputStream()));
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		private static class SinkReceiver extends LogReceiver
		{
			public SinkReceiver(HostInfo aHostInfo, InputStream aInStream, OutputStream aOutStream)
			{
				super(aHostInfo, aInStream, aOutStream, true);
			}

			@Override
			protected boolean process(DataInputStream aDataIn, DataOutputStream aDataOut) 
			{
				byte[] theBuffer = new byte[4096];
				try
				{
					while(true)
					{
						aDataIn.readFully(theBuffer);
					}
				}
				catch (Exception e)
				{
				}
				eof();
				return false;
			}
			
			@Override
			protected void clear()
			{
			}

			@Override
			protected int flush()
			{
				return 0;
			}

			@Override
			protected void readPacket(DataInputStream aStream, MessageType aType) throws IOException
			{
				throw new UnsupportedOperationException();
			}
		}
	}
	
	/**
	 * A thread that monitors the JVM process' output stream
	 * @author gpothier
	 */
	private static class ProcessOutWatcher extends Thread
	{
		private InputStream itsStream;
		private long itsTime;
		
		public ProcessOutWatcher(InputStream aStream)
		{
			super("Output Watcher");
			itsStream = aStream;
			start();
		}

		@Override
		public void run()
		{
			try
			{
				BufferedReader theReader = new BufferedReader(new InputStreamReader(itsStream));
				while(true)
				{
					String theLine = theReader.readLine();
					if (theLine == null) break;
					
					System.out.println("[Dummy] "+theLine);

					if (theLine.startsWith(Dummy.RESULT_PREFIX))  
					{
						itsTime = Long.parseLong(theLine.substring(Dummy.RESULT_PREFIX.length()));
					}
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public long getTime()
		{
			return itsTime;
		}
	}
	

}
