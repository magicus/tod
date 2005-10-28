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
		
		String ws = "["
			+"+java.lang.AbstractStringBuilder "
			+"+java.lang.Appendable "
			+"+java.lang.Boolean "
			+"+java.lang.Byte "
			+"+java.lang.Character "
			+"+java.lang.CharacterDataLatin1 "
			+"+java.lang.CharSequence "
			+"+java.lang.Class$1 "
			+"+java.lang.Class$3 "
			+"+java.lang.Class "
			+"+java.lang.ClassLoader$3 "
			+"+java.lang.ClassLoader "
			+"+java.lang.ClassLoader$NativeLibrary "
			+"+java.lang.Cloneable "
			+"+java.lang.Comparable "
			+"+java.lang.Compiler$1 "
			+"+java.lang.Compiler "
			+"+java.lang.Double "
			+"+java.lang.Error "
			+"+java.lang.Exception "
			+"+java.lang.Float "
			+"+java.lang.Integer "
			+"+java.lang.Iterable "
			+"+java.lang.Long "
			+"+java.lang.Number "
//			+"+java.lang.Object "
			+"+java.lang.Readable "
			+"+java.lang.Runnable "
			+"+java.lang.Runtime "
			+"+java.lang.RuntimePermission "
//			+"+java.lang.Short "
			+"+java.lang.Shutdown "
			+"+java.lang.Shutdown$Lock "
			+"+java.lang.StackTraceElement "
			+"+java.lang.StrictMath "
			+"+java.lang.StringBuffer "
			+"+java.lang.StringBuilder "
			+"+java.lang.String$CaseInsensitiveComparator "
//			+"+java.lang.String "
			+"+java.lang.StringCoding$CharsetSD "
			+"+java.lang.StringCoding$CharsetSE "
			+"+java.lang.StringCoding "
			+"+java.lang.StringCoding$StringDecoder "
			+"+java.lang.StringCoding$StringEncoder "
			+"+java.lang.System$2 "
			+"+java.lang.System "
			+"+java.lang.SystemClassLoaderAction "
			+"+java.lang.Terminator$1 "
			+"+java.lang.Terminator "
			+"+java.lang.Thread "
			+"+java.lang.ThreadDeath "
			+"+java.lang.ThreadGroup "
			+"+java.lang.ThreadLocal "
			+"+java.lang.ThreadLocal$ThreadLocalMap "
			+"+java.lang.ThreadLocal$ThreadLocalMap$Entry "
			+"+java.lang.Thread$UncaughtExceptionHandler "
			+"+java.lang.Throwable "
			+"]";
		
		itsConfig = new ASMDebuggerConfig(
				new PrintThroughCollector(itsCollector),
				new File("/home/gpothier/tmp/ASM/loc.dat"), 
				"[-tod.** -remotebci.** +tod.test.** +tod.demo.**]",
				"[-java.lang.String -java.lang.Number]",
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
			disconnect();
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
			disconnect();
		}
		
	}


}
