/*
 * Created on Oct 26, 2005
 */
package tod.session;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;

import remotebci.RemoteInstrumenter;
import tod.core.PrintThroughCollector;
import tod.core.model.structure.ObjectId;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.ILocationTrace;
import tod.core.transport.LogReceiver;
import tod.impl.local.LocalCollector;

/**
 * A session that uses a {@link tod.impl.local.LocalCollector}
 * and {@link tod.bci.asm.ASMInstrumenter}.
 * @author gpothier
 */
public class ASMLocalSession extends AbstractSession
{
	private final ASMDebuggerConfig itsConfig;
	private final LocalCollector itsCollector;
	
	private MyLogReceiver itsLogReceiver;
	private MyInstrumenter itsInstrumenter;

	private String itsCachedClassesPath;
	private File itsCachedLocationsPath;

	public ASMLocalSession(
			URI aUri, 
			String aGlobalWorkingSet,
			String aIdentificationWorkingSet,
			String aTraceWorkingSet)
	{
		super(aUri);
		
		if (aUri != null)
		{
			File theCachePath = new File(aUri);
			itsCachedClassesPath = theCachePath.getPath();
			
			itsCachedLocationsPath = new File(theCachePath, "loc.dat");
		}
		
		
		itsCollector = new LocalCollector();
		
		itsConfig = new ASMDebuggerConfig(
				new PrintThroughCollector(itsCollector),
				itsCachedLocationsPath, 
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
		private static final int EXCEPTION_GENERATED = 20;
		private static final byte OBJECT_HASH = 1;
		private static final byte OBJECT_UID = 2;

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
		protected void processCommand(int aCommand, DataInputStream aInputStream, DataOutputStream aOutputStream) throws IOException
		{
			if (aCommand == EXCEPTION_GENERATED)
			{
				long theTimestamp = aInputStream.readLong();
				long theThreadId = aInputStream.readLong();
				String theMethodName = aInputStream.readUTF();
				String theMethodSignature = aInputStream.readUTF();
				String theMethodDeclaringClassSignature = aInputStream.readUTF();
				int theBytecodeIndex = aInputStream.readInt();
				byte theExceptionIdType = aInputStream.readByte();
				Object theException;
				
				switch (theExceptionIdType)
				{
				case OBJECT_UID:
					long theUid = aInputStream.readLong();
					theException = new ObjectId.ObjectUID(theUid);
					break;
					
				case OBJECT_HASH:
					int theHash = aInputStream.readInt();
					theException = new ObjectId.ObjectHash(theHash);
					break;
					
				default:
					throw new RuntimeException("Not handled: "+theExceptionIdType);
				}
				
				String theClassName = theMethodDeclaringClassSignature.substring(1, theMethodDeclaringClassSignature.length()-1);
				int theTypeId = itsConfig.getLocationPool().getTypeId(theClassName);
				int theBehaviorId = itsConfig.getLocationPool().getBehaviorId(theTypeId, theMethodName, theMethodSignature);
				
				itsConfig.getCollector().logExceptionGenerated(theTimestamp, theThreadId, theBehaviorId, theBytecodeIndex, theException);

			}
			else super.processCommand(aCommand, aInputStream, aOutputStream);
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
