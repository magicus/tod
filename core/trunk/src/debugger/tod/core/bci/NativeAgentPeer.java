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

import sun.management.FileSystem;
import tod.agent.AgentConfig;
import tod.core.bci.IInstrumenter.InstrumentedClass;
import tod.core.config.TODConfig;
import tod.core.server.TODServer;
import tod.core.transport.SocketThread;


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
	
	public static final byte SET_CACHE_PATH = 80;
	public static final byte SET_SKIP_CORE_CLASSES = 81;
	public static final byte SET_VERBOSE = 82;
	public static final byte SET_CAPTURE_EXCEPTIONS = 83;
	public static final byte SET_HOST_BITS = 84;

	public static final byte CONFIG_DONE = 90;
	
	private final TODConfig itsConfig;
	
	private final File itsStoreClassesDir;
	private final IInstrumenter itsInstrumenter;
	
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
	 * Starts a peer that uses an already connected socket.
	 */
	public NativeAgentPeer(
			TODConfig aConfig,
			Socket aSocket,
			File aStoreClassesDir,
			IInstrumenter aInstrumenter,
			int aHostId)
	{
		super (aSocket);
		assert aConfig != null;
		itsConfig = aConfig;
		itsStoreClassesDir = aStoreClassesDir;
		itsInstrumenter = aInstrumenter;
		itsHostId = aHostId;
		
		// Check that the cache path we pass to the agent exists.
		File theFile = new File(itsConfig.get(TODConfig.AGENT_CACHE_PATH));
		theFile.mkdirs();
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
			processInstrumentClassCommand(
					itsInstrumenter, 
					aInputStream, 
					aOutputStream, 
					itsStoreClassesDir);
			
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
		DataOutputStream theOutStream = new DataOutputStream(aOutputStream);
		
		// Read host name
		String theHostName = theStream.readUTF();
		setHostName(theHostName);
		System.out.println("[NativeAgentPeer] Received host name: '"+theHostName+"'");
		
		// Send host id
		theOutStream.writeInt(itsHostId);

		// Send remaining config
		String theCachePath = itsConfig.get(TODConfig.AGENT_CACHE_PATH);
		if (theCachePath != null)
		{
			theOutStream.writeByte(SET_CACHE_PATH);
			theOutStream.writeUTF(theCachePath);
		}
		
		boolean theSkipCoreClasses = itsConfig.get(TODConfig.AGENT_SKIP_CORE_CLASSE);
		theOutStream.writeByte(SET_SKIP_CORE_CLASSES);
		theOutStream.writeByte(theSkipCoreClasses ? 1 : 0);
		
		int theVerbosity = itsConfig.get(TODConfig.AGENT_VERBOSE);
		theOutStream.writeByte(SET_VERBOSE);
		theOutStream.writeByte((byte) theVerbosity);
		
		boolean theCaptureExceptions = itsConfig.get(TODConfig.AGENT_CAPTURE_EXCEPTIONS);
		theOutStream.writeByte(SET_CAPTURE_EXCEPTIONS);
		theOutStream.writeByte(theCaptureExceptions ? 1 : 0);
		
		int theHostBits = AgentConfig.HOST_BITS;
		theOutStream.writeByte(SET_HOST_BITS);
		theOutStream.writeByte(theHostBits);
		
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
		InstrumentedClass theInstrumentedClass = aInstrumenter.instrumentClass(theClassName, theBytecode);
		if (theInstrumentedClass != null)
		{
			System.out.println("Instrumented");
			
			if (aStoreClassesDir != null)
			{
				File theFile = new File (aStoreClassesDir, theClassName+".class");
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
		else
		{
			System.out.println("Not instrumented");
			aOutputStream.writeInt(0);
		}
		aOutputStream.flush();
		
	}
	
}
