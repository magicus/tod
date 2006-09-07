package tod.core.bci;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import tod.core.ILogCollector;
import tod.core.database.structure.ObjectId;
import tod.core.transport.SocketThread;


/**
 * This class listens on a socket and respond to requests sent
 * by the native agent running in the target VM.
 * @author gpothier
 */
public abstract class NativeAgentPeer extends SocketThread
{
	public static final byte EXCEPTION_GENERATED = 20;
	public static final byte INSTRUMENT_CLASS = 50;
	public static final byte FLUSH = 99;
	public static final byte OBJECT_HASH = 1;
	public static final byte OBJECT_UID = 2;
	
    public static final byte SET_CACHE_PATH = 80;
    public static final byte SET_SKIP_CORE_CLASSES = 81;
    public static final byte CONFIG_DONE = 90;
    
	private final File itsStoreClassesDir;
	private final IInstrumenter itsInstrumenter;
	
	private boolean itsConfigured = false;
	
	/**
	 * Name of the connected host
	 */
	private String itsHostName;


	/**
	 * Starts a peer that acts as a server, creating its own {@link ServerSocket}.
	 */
    public NativeAgentPeer(
    		int aPort, 
    		boolean aStartImmediately,
    		File aStoreClassesDir,
    		IInstrumenter aInstrumenter) throws IOException
    {
        super (new ServerSocket(aPort), aStartImmediately);
		itsStoreClassesDir = aStoreClassesDir;
		itsInstrumenter = aInstrumenter;
    }

    /**
     * Starts a peer that uses an already connected socket.
     */
    public NativeAgentPeer(
    		Socket aSocket,
    		File aStoreClassesDir,
    		IInstrumenter aInstrumenter)
    {
    	super (aSocket);
    	itsStoreClassesDir = aStoreClassesDir;
    	itsInstrumenter = aInstrumenter;
    }
    
	/**
	 * Returns the name of the currently connected host, or null
	 * if there is no connected host.
	 */
	public String getHostName()
	{
		return itsHostName;
	}
	
	private synchronized void setHostName(String aHostName)
	{
		itsHostName = aHostName;
		notifyAll();
	}
	
	/**
	 * Waits until the host name is available, and returns it.
	 * See {@link #getHostName()}
	 */
	public synchronized String waitHostName()
	{
		try
		{
			while (itsHostName == null) wait();
			return itsHostName;
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}

    
    @Override
    protected final void process(OutputStream aOutputStream, InputStream aInputStream) throws IOException
    {
        DataInputStream theInputStream = new DataInputStream(aInputStream);
        DataOutputStream theOutputStream = new DataOutputStream(aOutputStream);
        
        if (! itsConfigured)
        {
        	processConfig(theInputStream, theOutputStream);
        	itsConfigured = true;
        }
        
        int theCommand = theInputStream.readByte();
        processCommand(theCommand, theInputStream, theOutputStream);
    }
    
    @Override
    protected void disconnected()
    {
    	setHostName(null);
    }
    
    protected final void processCommand (
    		int aCommand,
    		DataInputStream aInputStream, 
            DataOutputStream aOutputStream) throws IOException
    {
        switch (aCommand)
        {
        case INSTRUMENT_CLASS:
            processInstrumentClassCommand(itsInstrumenter, aInputStream, aOutputStream, itsStoreClassesDir);
            break;
            
        case EXCEPTION_GENERATED:
        	processExceptionGenerated(aInputStream, aOutputStream);
        	break;
        	
        case FLUSH:
        	processFlush();
        	break;
            
        default:
            throw new RuntimeException("Command not handled: "+aCommand);
        }    	
    }
    
    /**
     * Sends configuration data to the agent. This method is called once at the beginning 
     * of the connection.
     */
    private void processConfig(
    		DataInputStream aInputStream, 
            DataOutputStream aOutputStream) throws IOException
    {
    	DataInputStream theStream = new DataInputStream(aInputStream);
		setHostName(theStream.readUTF());

    	String theCachePath = cfgCachePath();
    	if (theCachePath != null)
    	{
    		aOutputStream.writeByte(SET_CACHE_PATH);
    		aOutputStream.writeUTF(theCachePath);
    	}
    	
    	aOutputStream.writeByte(SET_SKIP_CORE_CLASSES);
    	aOutputStream.writeByte(cfgSkipCoreClasses() ? 1 : 0);
    	
    	aOutputStream.writeByte(CONFIG_DONE);
    }
    
	private void processExceptionGenerated(DataInputStream aInputStream, DataOutputStream aOutputStream) throws IOException
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
		
		processExceptionGenerated(
				theTimestamp, 
				theThreadId, 
				theClassName,
				theMethodName,
				theMethodSignature,
				theBytecodeIndex, 
				theException);
	}
	
	/**
	 * This method is called when an exception has been detected by the native agent.
	 * Subclasses should resolve the class and method and call one of the exception methods
	 * of {@link ILogCollector}
	 */
	protected abstract void processExceptionGenerated(
			long aTimestamp,
			long aThreadId, 
			String aClassName, 
			String aMethodName,
			String aMethodSignature,
			int aBytecodeIndex,
			Object aException);
	
	/**
	 * This method is called when the target vm is terminated.
	 */
	protected abstract void processFlush();


    /**
     * Returns the directory where instrumented classes should be cached, or
     * null if no caching should be performed.
     */
    protected String cfgCachePath()
    {
    	return null;
    }
    
    /**
     * Indicates if core JDK classes should be skipped.
     */
    protected boolean cfgSkipCoreClasses()
    {
    	return true;
    }

    /**
     * Processes an INSTRUMENT_CLASS command sent by the agent.
     * @param aInstrumenter The instrumenter that will do the BCI of the class
     * @param aInputStream Input stream connected to the agent
     * @param aOutputStream Output stream connected to the agent
     * @param aStoreClassesDir If not null, instrumented classes will be stored in
     * the directory denoted by this file.
     */
    public static void processInstrumentClassCommand(
            IInstrumenter aInstrumenter,
            DataInputStream aInputStream, 
            DataOutputStream aOutputStream,
            File aStoreClassesDir) throws IOException
    {
        
        String theClassName = aInputStream.readUTF();
        int theLength = aInputStream.readInt();
        byte[] theBytecode = new byte[theLength];
        aInputStream.readFully(theBytecode);
        
        System.out.print("Instrumenting "+theClassName+"... ");
        theBytecode = aInstrumenter.instrumentClass(theClassName, theBytecode);
        if (theBytecode != null)
        {
            System.out.println("Instrumented");
            
            if (aStoreClassesDir != null)
            {
                File theFile = new File (aStoreClassesDir, theClassName+".class");
                theFile.getParentFile().mkdirs();
                theFile.createNewFile();
                FileOutputStream theFileOutputStream = new FileOutputStream(theFile);
                theFileOutputStream.write(theBytecode);
                theFileOutputStream.flush();
                theFileOutputStream.close();
                
                System.out.println("Written class to "+theFile);
            }
            
            aOutputStream.writeInt(theBytecode.length);
            aOutputStream.write(theBytecode);
        }
        else
        {
            System.out.println("Not instrumented");
            aOutputStream.writeInt(0);
        }
        aOutputStream.flush();
        
    }
    
}