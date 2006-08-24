package tod.core.bci;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;

import tod.core.transport.SocketThread;


/**
 * This class can listen on a socket and respond to instrumentation requests sent
 * by an agent running in another VM.
 * @author gpothier
 */
public abstract class RemoteInstrumenter extends SocketThread implements IInstrumenter
{
    public static final byte INSTRUMENT_CLASS = 50;
    public static final byte SET_CACHE_PATH = 80;
    public static final byte SET_SKIP_CORE_CLASSES = 81;
    public static final byte CONFIG_DONE = 90;
    
	private final File itsStoreClassesDir;
	private boolean itsConfigured = false;
    
    public RemoteInstrumenter(int aPort, boolean aStartImmediately, File aStoreClassesDir) throws IOException
    {
        super (new ServerSocket(aPort), aStartImmediately);
		itsStoreClassesDir = aStoreClassesDir;
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
    
    protected void processCommand (
    		int aCommand,
    		DataInputStream aInputStream, 
            DataOutputStream aOutputStream) throws IOException
    {
        switch (aCommand)
        {
        case INSTRUMENT_CLASS:
            processInstrumentClassCommand(this, aInputStream, aOutputStream, itsStoreClassesDir);
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
    	String theCachePath = getCachePath();
    	if (theCachePath != null)
    	{
    		aOutputStream.writeByte(SET_CACHE_PATH);
    		aOutputStream.writeUTF(theCachePath);
    	}
    	
    	aOutputStream.writeByte(SET_SKIP_CORE_CLASSES);
    	aOutputStream.writeByte(getSkipCoreClasses() ? 1 : 0);
    	
    	aOutputStream.writeByte(CONFIG_DONE);
    }

    /**
     * Returns the directory where instrumented classes should be cached, or
     * null if no caching should be performed.
     */
    protected String getCachePath()
    {
    	return null;
    }
    
    /**
     * Indicates if core JDK classes should be skipped.
     */
    protected boolean getSkipCoreClasses()
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
