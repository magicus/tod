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

import tod.agent.transport.LowLevelEventType;
import tod.core.ILogCollector;
import tod.core.bci.IInstrumenter;
import tod.core.config.TODConfig;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.server.JavaTODServer;
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
	

	
	public static class SinkTODServer extends JavaTODServer
	{
		private SinkTODServer(TODConfig aConfig, IInstrumenter aInstrumenter, IStructureDatabase aStructureDatabase)
		{
			super(aConfig, aInstrumenter, aStructureDatabase, null);
		}
		
		public static SinkTODServer create(TODConfig aConfig)
		{
			IMutableStructureDatabase theStructureDatabase = StructureDatabase.create(aConfig);
			ASMDebuggerConfig theConfig = new ASMDebuggerConfig(aConfig);
			ASMInstrumenter theInstrumenter = new ASMInstrumenter(theStructureDatabase, theConfig);
			return new SinkTODServer(aConfig, theInstrumenter, theStructureDatabase);
		}

		@Override
		protected LogReceiver createReceiver(
				HostInfo aHostInfo,
				InputStream aInStream,
				OutputStream aOutStream,
				boolean aStart, 
				IStructureDatabase aStructureDatabase, 
				ILogCollector aCollector)
		{
			return new SinkReceiver(aHostInfo, aInStream, aOutStream);
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
			protected void processClear()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected int processFlush()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected void processEvent(LowLevelEventType aType, DataInputStream aStream)
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
