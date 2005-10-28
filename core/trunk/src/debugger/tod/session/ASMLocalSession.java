/*
 * Created on Oct 26, 2005
 */
package tod.session;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;

import reflex.lib.logging.miner.impl.local.LocalCollector;
import remotebci.RemoteInstrumenter;
import tod.core.PrintThroughCollector;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.ILocationTrace;
import tod.core.transport.LogReceiver;

/**
 * A session that uses a {@link reflex.lib.logging.miner.impl.local.LocalCollector}
 * and {@link tod.bci.asm.ASMInstrumenter}.
 * @author gpothier
 */
public class ASMLocalSession extends AbstractSession
{
	private final ASMDebuggerConfig itsConfig;
	private final LocalCollector itsCollector;
	
	private MyLogReceiver itsLogReceiver;
	private MyInstrumenter itsInstrumenter;

	private String itsCachedClassesPath = "/home/gpothier/tmp/ASM";

	public ASMLocalSession(
			URI aUri, 
			String aGlobalWorkingSet,
			String aIdentificationWorkingSet,
			String aTraceWorkingSet)
	{
		super(aUri);
		itsCollector = new LocalCollector();
		itsConfig = new ASMDebuggerConfig(
				new PrintThroughCollector(itsCollector),
				new File("/home/gpothier/tmp/ASM/loc.dat"), // don't write instrumented classes
				"[-tod.** -remotebci.** +tod.test.** +tod.demo.**]",
				"[-java.lang.String]",
				"[-java.** -javax.** -sun.** -com.sun.**]");

		try
		{
			itsLogReceiver = new MyLogReceiver (itsConfig, new ServerSocket(8058));
			itsInstrumenter = new MyInstrumenter(8059);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public IEventTrace getEventTrace()
	{
		return itsCollector;
	}

	public ILocationTrace getLocationTrace()
	{
		return itsCollector;
	}
	
	public void disconnect()
	{
		itsInstrumenter.interrupt();
		itsLogReceiver.interrupt();
	}
	
	public String getCachedClassesPath()
	{
		return itsCachedClassesPath;
	}

	private class MyInstrumenter extends RemoteInstrumenter
	{
		private boolean itsFinished = false;
		
		public MyInstrumenter(int aPort) throws IOException
		{
			super (aPort, true, null);
		}

		@Override
		protected String getCachePath()
		{
			return getCachedClassesPath();
		}
		
		@Override
		protected boolean getSkipCoreClasses()
		{
			return false;
		}
		
		public byte[] instrumentClass(String aClassName, byte[] aBytecode)
		{
			return itsConfig.getInstrumenter().instrumentClass(aClassName, aBytecode);
		}
		
		@Override
		protected boolean accept()
		{
			return ! itsFinished;
		}
		
		@Override
		protected void disconnected()
		{
			itsFinished = true;
		}
	}
	
	private class MyLogReceiver extends LogReceiver
	{
		private boolean itsFinished = false;

		public MyLogReceiver(ASMDebuggerConfig aConfig, ServerSocket aServerSocket)
		{
			super (aConfig, aServerSocket);
		}
		
		@Override
		protected boolean accept()
		{
			return ! itsFinished;
		}
		
		@Override
		protected void disconnected()
		{
			itsFinished = true;
		}
		
	}


}
