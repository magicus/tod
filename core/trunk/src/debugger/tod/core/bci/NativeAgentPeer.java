/*
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
package tod.core.bci;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import tod.agent.AgentConfig;
import tod.agent.transport.SocketThread;
import tod.core.bci.IInstrumenter.InstrumentedClass;
import tod.core.config.TODConfig;
import tod.core.server.TODServer;


/**
 * This class listens on a socket and respond to requests sent
 * by the native agent running in the target VM.
 * @author gpothier
 */
public abstract class NativeAgentPeer extends SocketThread
{
	public static final byte INSTRUMENT_CLASS = 50;
	public static final byte FLUSH = 99;
	public static final byte OBJECT_HASH = 1;
	public static final byte OBJECT_UID = 2;
	
	public static final byte SET_SKIP_CORE_CLASSES = 81;
	public static final byte SET_CAPTURE_EXCEPTIONS = 83;
	public static final byte SET_HOST_BITS = 84;
	public static final byte SET_WORKING_SET = 85;
	public static final byte SET_STRUCTDB_ID = 86;

	public static final byte CONFIG_DONE = 90;
	
	private final TODConfig itsConfig;
	
	private final IInstrumenter itsInstrumenter;
	
	/**
	 * This flags is set to true when the connection to the agent is entirely
	 * set up.
	 * @see #waitConfigured()
	 */
	private boolean itsConfigured = false;
	
	/**
	 * Name of the connected host
	 */
	private String itsHostName;
	
	/**
	 * An id for the connected host, assigned by
	 * the {@link TODServer}. It is not necessarily the same as the "official"
	 * host id; it is used only to differentiate object ids from several hosts. 
	 */
	private int itsHostId;
	
	/**
	 * Id of the structure database.
	 */
	private String itsStructureDatabaseId;
	
	/**
	 * Directory where instrumented classes are stored, or null if 
	 * classes should not be stored.
	 * Class storage is for debugging only.
	 */
	private File itsStoreClassesDir;
	

	/**
	 * Starts a peer that uses an already connected socket.
	 */
	public NativeAgentPeer(
			TODConfig aConfig,
			Socket aSocket,
			String aStructureDatabaseId,
			IInstrumenter aInstrumenter,
			int aHostId)
	{
		super (aSocket, false);
		assert aConfig != null;
		itsConfig = aConfig;
		itsStructureDatabaseId = aStructureDatabaseId;
		itsInstrumenter = aInstrumenter;
		itsHostId = aHostId;
		
		String theStoreClassesDir = itsConfig.get(TODConfig.INSTRUMENTER_CLASSES_DIR);
		itsStoreClassesDir = theStoreClassesDir != null && theStoreClassesDir.length() > 0 ?
				new File(theStoreClassesDir)
				: null;
		
		// Check that the cache path we pass to the agent exists.
		File theFile = new File(itsConfig.get(TODConfig.AGENT_CACHE_PATH));
		theFile.mkdirs();
		
		start();
	}
	
	/**
	 * Returns the name of the currently connected host, or null
	 * if there is no connected host.
	 * This method should be called only after the connection is set up
	 * (see {@link #waitConfigured()}).
	 */
	public String getHostName()
	{
		return itsHostName;
	}
	
	private synchronized void setConfigured()
	{
		itsConfigured = true;
		notifyAll();
	}
	
	/**
	 * Waits until the connection is completely set up.
	 * After this method returns the host name ({@link #getHostName()})
	 */
	public synchronized void waitConfigured()
	{
		try
		{
			while (! itsConfigured) wait();
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
			setConfigured();
		}
		
		int theCommand = theInputStream.readByte();
		processCommand(theCommand, theInputStream, theOutputStream);
	}
	
	@Override
	protected void disconnected()
	{
		itsHostName = null;
		setConfigured();
	}
	
	protected final void processCommand (
			int aCommand,
			DataInputStream aInputStream, 
			DataOutputStream aOutputStream) throws IOException
	{
		switch (aCommand)
		{
		case INSTRUMENT_CLASS:
			processInstrumentClassCommand(
					itsInstrumenter, 
					aInputStream, 
					aOutputStream);
			
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
		DataInputStream theInStream = new DataInputStream(aInputStream);
		DataOutputStream theOutStream = new DataOutputStream(aOutputStream);
		
		// Read host name
		itsHostName = theInStream.readUTF();
		System.out.println("[NativeAgentPeer] Received host name: '"+itsHostName+"'");
		
		// Send host id
		theOutStream.writeInt(itsHostId);

		// Send remaining config
		boolean theSkipCoreClasses = itsConfig.get(TODConfig.AGENT_SKIP_CORE_CLASSE);
		theOutStream.writeByte(SET_SKIP_CORE_CLASSES);
		theOutStream.writeByte(theSkipCoreClasses ? 1 : 0);
		
		boolean theCaptureExceptions = itsConfig.get(TODConfig.AGENT_CAPTURE_EXCEPTIONS);
		theOutStream.writeByte(SET_CAPTURE_EXCEPTIONS);
		theOutStream.writeByte(theCaptureExceptions ? 1 : 0);
		
		int theHostBits = AgentConfig.HOST_BITS;
		theOutStream.writeByte(SET_HOST_BITS);
		theOutStream.writeByte(theHostBits);
		
		String theWorkingSet = itsConfig.get(TODConfig.SCOPE_TRACE_FILTER);
		theOutStream.writeByte(SET_WORKING_SET);
		theOutStream.writeUTF(theWorkingSet);
		
		theOutStream.writeByte(SET_STRUCTDB_ID);
		theOutStream.writeUTF(itsStructureDatabaseId);
		
		theOutStream.writeByte(CONFIG_DONE);
		theOutStream.flush();
	}
	
	/**
	 * This method is called when the target vm is terminated.
	 */
	protected abstract void processFlush();


	/**
	 * Processes an INSTRUMENT_CLASS command sent by the agent.
	 * @param aInstrumenter The instrumenter that will do the BCI of the class
	 * @param aInputStream Input stream connected to the agent
	 * @param aOutputStream Output stream connected to the agent
	 * @param aStoreClassesDir If not null, instrumented classes will be stored in
	 * the directory denoted by this file.
	 */
	public void processInstrumentClassCommand(
			IInstrumenter aInstrumenter,
			DataInputStream aInputStream, 
			DataOutputStream aOutputStream) throws IOException
	{
		String theClassName = aInputStream.readUTF();
		int theLength = aInputStream.readInt();
		byte[] theBytecode = new byte[theLength];
		aInputStream.readFully(theBytecode);
		
		System.out.println("Instrumenting "+theClassName+"... ");
		InstrumentedClass theInstrumentedClass = null;
		String theError = null;
		try
		{
			theInstrumentedClass = aInstrumenter.instrumentClass(theClassName, theBytecode);
		}
		catch (Exception e)
		{
			System.err.println("Error during instrumentation, reporting to client: ");
			e.printStackTrace();
			theError = e.getMessage();
		}

		if (theInstrumentedClass != null)
		{
			System.out.println("Instrumented (size: "+theInstrumentedClass.bytecode.length+")");
			
			if (itsStoreClassesDir != null)
			{
				File theFile = new File (itsStoreClassesDir, theClassName+".class");
				theFile.getParentFile().mkdirs();
				theFile.createNewFile();
				FileOutputStream theFileOutputStream = new FileOutputStream(theFile);
				theFileOutputStream.write(theInstrumentedClass.bytecode);
				theFileOutputStream.flush();
				theFileOutputStream.close();
				
				System.out.println("Written class to "+theFile);
			}
			
			// Write out instrumented bytecode
			aOutputStream.writeInt(theInstrumentedClass.bytecode.length);
			aOutputStream.write(theInstrumentedClass.bytecode);
			
			// Write out traced method ids
			System.out.println("Sending "+theInstrumentedClass.tracedMethods.size()+" traced methods.");
			aOutputStream.writeInt(theInstrumentedClass.tracedMethods.size());
			for(int theId : theInstrumentedClass.tracedMethods)
			{
				aOutputStream.writeInt(theId);
			}
		}
		else if (theError != null)
		{
			aOutputStream.writeInt(-1);
			aOutputStream.writeUTF(theError);
		}
		else
		{
			System.out.println("Not instrumented");
			aOutputStream.writeInt(0);
		}
		
		aOutputStream.flush();
	}
	
}
