/*
 * Created on Aug 29, 2006
 */
package tod.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import tod.core.LocationRegistrer;
import tod.core.bci.IInstrumenter;
import tod.core.server.ICollectorFactory;
import tod.core.server.TODServer;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import zz.utils.Utils;

/**
 * A tod server that stores all the events it receives from the java client
 * @author gpothier
 */
public class StoreTODServer extends TODServer
{
	private int itsConnectionNumber = 1;

	public StoreTODServer(ICollectorFactory aCollectorFactory, IInstrumenter aInstrumenter)
	{
		super(aCollectorFactory, aInstrumenter);
	}

	@Override
	protected void acceptJavaConnection(Socket aSocket)
	{
		String theFileName = ConfigUtils.readString("events-file", "events-raw.bin");
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
				
				FileOutputStream theStream = new FileOutputStream(itsFile);
				InputStream theInputStream = itsSocket.getInputStream();
				Utils.pipe(theInputStream, theStream);
				
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
	
	public static void main(String[] args)
	{
		LocationRegistrer theLocationRegistrer = new LocationRegistrer();
		
		ASMDebuggerConfig theConfig = new ASMDebuggerConfig(
				theLocationRegistrer,
				new File("/home/gpothier/tmp/tod"), 
				"[-tod.** -remotebci.** +tod.test.** +tod.demo.**]",
				"[-java.** -javax.** -sun.** -com.sun.**]");

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theConfig);
		
		new StoreTODServer(null, theInstrumenter);
	}

}
