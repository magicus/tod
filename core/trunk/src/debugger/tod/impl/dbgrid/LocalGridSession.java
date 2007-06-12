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
package tod.impl.dbgrid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import tod.core.config.TODConfig;
import tod.impl.dbgrid.aggregator.GridEventBrowser;

/**
 * A single-process grid session, running in a separate vm.
 * @author gpothier
 */
public class LocalGridSession extends RemoteGridSession
{
	public static String cp = ".";
	public static String lib = ".";

	private static Process itsProcess;
	
	static
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				if (itsProcess != null) itsProcess.destroy();
			}
		});
	}
	
	private KeepAliveThread itsKeepAliveThread = new KeepAliveThread();

	public LocalGridSession(URI aUri, TODConfig aConfig, boolean aUseExisting)
	{
		super(aUri, aConfig, aUseExisting);
	}

	public LocalGridSession(URI aUri, TODConfig aConfig)
	{
		super(aUri, aConfig);
	}

	@Override
	protected String getHost()
	{
		return "localhost";
	}
	
	private String getJavaExecutable()
	{
		String theOs = System.getProperty("os.name");
		
		String theJVM = System.getProperty("java.home")+"/bin/java";
		if (theOs.contains("Windows")) theJVM += "w.exe";
			
		return theJVM;
	}
	
	private void createProcess()
	{
		try
		{
//			Util.getRegistry();
			
			if (itsProcess != null) itsProcess.destroy();
			
			boolean theJDWP = false;
			
			Long theHeapSize = getConfig().get(TODConfig.LOCAL_SESSION_HEAP);
			String theJVM = getJavaExecutable();
			ProcessBuilder theBuilder = new ProcessBuilder(
					theJVM,
					"-Xmx"+theHeapSize,
					"-Djava.library.path="+lib,
					"-cp", cp,
					"-Dmaster-host=localhost",					
					"-Dpage-buffer-size="+(theHeapSize/2),
					TODConfig.MASTER_TIMEOUT.javaOpt(10),
					TODConfig.AGENT_VERBOSE.javaOpt(getConfig()),
					TODConfig.SCOPE_TRACE_FILTER.javaOpt(getConfig()),
//					theJDWP ? "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000" : "",
					"tod.impl.dbgrid.GridMaster",
					"0");
			
			StringBuilder theCommand = new StringBuilder();
			for (String theArg : theBuilder.command()) 
			{
				theCommand.append('"');
				theCommand.append(theArg);
				theCommand.append("\" ");
			}
			System.out.println("[LocalGridSession] Command: "+theCommand);
			
			theBuilder.redirectErrorStream(false);
			
			itsProcess = theBuilder.start();
			ProcessOutWatcher theWatcher = new ProcessOutWatcher(itsProcess.getInputStream());
			ProcessErrGrabber theGrabber = new ProcessErrGrabber(itsProcess.getErrorStream());

			boolean theReady = theWatcher.waitReady();
			
			if (! theReady)
			{
				throw new RuntimeException("Could not start event database:\n--\n"+theGrabber.getText()+"\n--");
			}

			theGrabber.stopCapture();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void init()
	{
		// Load POM-synced classes (hack to avoid timeout)
		System.out.println(GridLogBrowser.class);
		System.out.println(GridEventBrowser.class);
		
		createProcess();
		super.init();
	}
	
	@Override
	public void disconnect()
	{
		createProcess();
	}
	
	/**
	 * A thread that monitors the JVM process' output stream
	 * @author gpothier
	 */
	private static class ProcessOutWatcher extends Thread
	{
		private InputStream itsStream;
		private boolean itsReady = false;
		private CountDownLatch itsLatch = new CountDownLatch(1);
		
		public ProcessOutWatcher(InputStream aStream)
		{
			super("LocalGridSession - Output Watcher");
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
					System.out.println("[GridMaster process] "+theLine);
					if (theLine.startsWith(GridMaster.READY_STRING))  
					{
						itsReady = true;
						itsLatch.countDown();
						System.out.println("[LocalGridSession] GridMaster process ready.");
					}
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		/**
		 * Waits until the Grid master is ready, or a timeout occurs
		 * @return Whether the grid master is ready. 
		 */
		public boolean waitReady()
		{
			try
			{
				itsLatch.await(10, TimeUnit.SECONDS);
				interrupt();
				return itsReady;
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	private static class ProcessErrGrabber extends Thread
	{
		private InputStream itsStream;
		private StringBuilder itsBuilder = new StringBuilder();

		public ProcessErrGrabber(InputStream aStream)
		{
			super("LocalGridSession - Error grabber");
			itsStream = aStream;
			start();
		}
		
		/**
		 * Stops capturing output, and prints it instead.
		 */
		public void stopCapture()
		{
			itsBuilder = null;
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
					
					StringBuilder theBuilder = itsBuilder; // To avoid concurrency issues
					if (theBuilder != null) itsBuilder.append("> "+theLine+"\n");
					else System.out.println("[GridMaster process - err] "+theLine);
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		public String getText()
		{
			return itsBuilder.toString();
		}
	}
	
	private class KeepAliveThread extends Thread
	{
		public KeepAliveThread()
		{
			super("KeepAliveThread");
			start();
		}

		@Override
		public void run()
		{
			try
			{
				while(true)
				{
					RIGridMaster theMaster = getMaster();
					try
					{
						if (theMaster != null) theMaster.keepAlive();
					}
					catch (RemoteException e)
					{
						e.printStackTrace();
						reset();
						break;
					}
					Thread.sleep(2000);
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
