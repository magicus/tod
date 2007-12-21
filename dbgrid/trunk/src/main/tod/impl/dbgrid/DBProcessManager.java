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
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import tod.Util;
import tod.core.config.TODConfig;
import zz.utils.StreamPipe;

/**
 * Manages (launches, monitors and controls) an external database
 * process.
 * @author gpothier
 */
public class DBProcessManager
{
	private static DBProcessManager itsDefault;
	
	/**
	 * Returns a default instance of {@link DBProcessManager}.
	 */
	public static DBProcessManager getDefault()
	{
		if (itsDefault == null)
		{
			itsDefault = new DBProcessManager(new TODConfig());
		}
		return itsDefault;
	}
	
	public static String cp = ".";
	public static String lib = ".";

	private TODConfig itsConfig;
	private Process itsProcess;
	private RIGridMaster itsMaster;
	
	private List<IDBProcessListener> itsListeners = new ArrayList<IDBProcessListener>();
	
	private List<PrintStream> itsOutputPrintStreams = new ArrayList<PrintStream>();
	private List<PrintStream> itsErrorPrintStreams = new ArrayList<PrintStream>();
	private Map<OutputStream, PrintStream> itsOutputStreams = new HashMap<OutputStream, PrintStream>();
	private Map<OutputStream, PrintStream> itsErrorStreams = new HashMap<OutputStream, PrintStream>();
	
	private KeepAliveThread itsKeepAliveThread;
	private boolean itsAlive = false;
	
	private Thread itsShutdownHook = new Thread("Shutdown hook (DBProcessManager)")
	{
		@Override
		public void run()
		{
			if (itsProcess != null) itsProcess.destroy();
			itsProcess = null;
		}
	};

	public DBProcessManager(TODConfig aConfig)
	{
		itsConfig = aConfig;
		Runtime.getRuntime().addShutdownHook(itsShutdownHook);
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		Runtime.getRuntime().removeShutdownHook(itsShutdownHook);
		itsShutdownHook.run();
	}
	
	public void addListener(IDBProcessListener aListener) 
	{
		itsListeners.add(aListener);
	}
	
	public void removeListener(IDBProcessListener aListener) 
	{
		itsListeners.remove(aListener);
	}
	
	protected void fireStarted()
	{
		for (IDBProcessListener theListener : itsListeners)
		{
			theListener.started();
		}
	}

	protected void fireStopped()
	{
		for (IDBProcessListener theListener : itsListeners)
		{
			theListener.stopped();
		}
	}
	
	public TODConfig getConfig()
	{
		return itsConfig;
	}
	
	/**
	 * Sets the configuration for subsequent launches.
	 * @param aConfig
	 */
	public void setConfig(TODConfig aConfig)
	{
		itsConfig = aConfig;
	}

	/**
	 * Adds a stream that will receive the output from the database process.
	 * @see StreamPipe
	 */
	public void addOutputStream(OutputStream aStream)
	{
		PrintStream thePrintStream;
		if (aStream instanceof PrintStream) thePrintStream = (PrintStream) aStream;
		else
		{
			thePrintStream = new PrintStream(aStream);
			itsOutputStreams.put(aStream, thePrintStream);
		}
		itsOutputPrintStreams.add(thePrintStream);
	}
	
	/**
	 * Removes a stream previously added with {@link #addOutputStream(PrintStream)}
	 */
	public void removeOutputStream(OutputStream aStream)
	{
		PrintStream thePrintStream = itsOutputStreams.remove(aStream);
		itsOutputPrintStreams.remove(thePrintStream);
	}

	/**
	 * Adds a stream that will receive the error output from the database process.
	 * @see StreamPipe
	 */
	public void addErrorStream(OutputStream aStream)
	{
		PrintStream thePrintStream;
		if (aStream instanceof PrintStream) thePrintStream = (PrintStream) aStream;
		else
		{
			thePrintStream = new PrintStream(aStream);
			itsErrorStreams.put(aStream, thePrintStream);
		}
		itsErrorPrintStreams.add(thePrintStream);
	}
	
	/**
	 * Removes a stream previously added with {@link #addErrorStream(PrintStream)}
	 */
	public void removeErrorStream(OutputStream aStream)
	{
		PrintStream thePrintStream = itsErrorStreams.remove(aStream);
		itsErrorPrintStreams.remove(thePrintStream);
	}
	
	/**
	 * Returns the {@link RIGridMaster} representing the database.
	 */
	public RIGridMaster getMaster()
	{
		return itsMaster;
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
			if (itsKeepAliveThread != null) itsKeepAliveThread.kill();

			if (itsProcess != null) itsProcess.destroy();
			printOutput("--- Preparing...");
			
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
					TODConfig.CLIENT_HOST_NAME.javaOpt(getConfig()),
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
			System.out.println("[DBProcessManager] Command: "+theCommand);
			
			theBuilder.redirectErrorStream(false);
			
			setAlive(true);
			printOutput("--- Starting process...");
			printOutput("Classpath: "+cp);
			itsProcess = theBuilder.start();
			ProcessOutWatcher theWatcher = new ProcessOutWatcher(itsProcess.getInputStream());
			ProcessErrGrabber theGrabber = new ProcessErrGrabber(itsProcess.getErrorStream());

			printOutput("--- Waiting grid master...");
			boolean theReady = theWatcher.waitReady();
			
			if (! theReady)
			{
				throw new RuntimeException("Could not start event database:\n--\n"+theGrabber.getText()+"\n--");
			}

			theGrabber.stopCapture();
			printOutput("--- Ready.");
			itsKeepAliveThread = new KeepAliveThread(this);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Starts the database process and creates the grid master.
	 * This method waits until the grid master is ready, or some failure occurs
	 * (in which case it throws a {@link RuntimeException}).
	 */
	public void start()
	{
		createProcess();
		
		try
		{
			Registry theRegistry = LocateRegistry.getRegistry("localhost", Util.TOD_REGISTRY_PORT);
			itsMaster = (RIGridMaster) theRegistry.lookup(GridMaster.getRMIId(getConfig()));
			itsMaster.setConfig(getConfig());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

	}
	
	/**
	 * Stops the database process.
	 */
	public void stop()
	{
		if (itsProcess != null) itsProcess.destroy();
		itsProcess = null;
	}
	
	/**
	 * Whether the process is alive.
	 */
	public boolean isAlive()
	{
		return itsAlive;
	}

	public void setAlive(boolean aAlive)
	{
		if (aAlive != itsAlive)
		{
			itsAlive = aAlive;
			if (itsAlive) fireStarted();
			else fireStopped();
		}
	}

	private void printOutput(String aString)
	{
		for (PrintStream theStream : itsOutputPrintStreams)
		{
			theStream.println(aString);
		}
	}
	
	private void printError(String aString)
	{
		for (PrintStream theStream : itsErrorPrintStreams)
		{
			theStream.println(aString);
		}
	}
	
	/**
	 * A thread that monitors the JVM process' output stream
	 * @author gpothier
	 */
	private class ProcessOutWatcher extends Thread
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

					printOutput(theLine);
					
					System.out.println("[GridMaster process] "+theLine);
					if (theLine.startsWith(GridMaster.READY_STRING))  
					{
						itsReady = true;
						itsLatch.countDown();
						System.out.println("[DBProcessManager] GridMaster process ready.");
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
				itsLatch.await(itsConfig.get(TODConfig.DB_PROCESS_TIMEOUT), TimeUnit.SECONDS);
				interrupt();
				return itsReady;
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	private class ProcessErrGrabber extends Thread
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

					printError(theLine);

					StringBuilder theBuilder = itsBuilder; // To avoid concurrency issues
					if (theBuilder != null) itsBuilder.append("> "+theLine+"\n");
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
	
	
	
	private static class KeepAliveThread extends Thread
	{
		private WeakReference<DBProcessManager> itsManager;
		
		public KeepAliveThread(DBProcessManager aManager)
		{
			super("KeepAliveThread");
			itsManager = new WeakReference<DBProcessManager>(aManager);
			setDaemon(true);
			start();
		}

		public synchronized void kill()
		{
			itsManager = null;
		}
		
		@Override
		public synchronized void run()
		{
			try
			{
				while(itsManager != null)
				{
					DBProcessManager theManager = itsManager.get();
					if (theManager == null) return;
					
					boolean theAlive = false;
					RIGridMaster theMaster = theManager.getMaster();
					try
					{
						if (theMaster != null) 
						{
							theMaster.keepAlive();
							theAlive = true;
						}
					}
					catch (RemoteException e)
					{
						e.printStackTrace();
						theManager.itsMaster = null;
					}

					theManager.setAlive(theAlive);
					
					theManager = null; // We don't want to prevent GC
					wait(2000);
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	
	
	/**
	 * A listener of the state of the process.
	 * @author gpothier
	 */
	public interface IDBProcessListener
	{
		public void started();
		public void stopped();
	}



}
