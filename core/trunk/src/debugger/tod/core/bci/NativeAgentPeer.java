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

import tod.core.bci.IInstrumenter.InstrumentedClass;
import tod.core.config.TODConfig;
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
	 * Starts a peer that acts as a server, creating its own {@link ServerSocket}.
	 */
	public NativeAgentPeer(
			TODConfig aConfig,
			int aPort, 
			boolean aStartImmediately,
			File aStoreClassesDir,
			IInstrumenter aInstrumenter) throws IOException
	{
		super (new ServerSocket(aPort), aStartImmediately);
		itsConfig = aConfig;
		itsStoreClassesDir = aStoreClassesDir;
		itsInstrumenter = aInstrumenter;
	}

	/**
	 * Starts a peer that uses an already connected socket.
	 */
	public NativeAgentPeer(
			TODConfig aConfig,
			Socket aSocket,
			File aStoreClassesDir,
			IInstrumenter aInstrumenter)
	{
		super (aSocket);
		itsConfig = aConfig;
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
		setHostName(theStream.readUTF());

		String theCachePath = itsConfig.get(TODConfig.AGENT_CACHE_PATH);
		if (theCachePath != null)
		{
			aOutputStream.writeByte(SET_CACHE_PATH);
			aOutputStream.writeUTF(theCachePath);
		}
		
		boolean theSkipCoreClasses = itsConfig.get(TODConfig.AGENT_SKIP_CORE_CLASSE);
		aOutputStream.writeByte(SET_SKIP_CORE_CLASSES);
		aOutputStream.writeByte(theSkipCoreClasses ? 1 : 0);
		
		int theVerbosity = itsConfig.get(TODConfig.AGENT_VERBOSE);
		aOutputStream.writeByte(SET_VERBOSE);
		aOutputStream.writeByte((byte) theVerbosity);
		
		aOutputStream.writeByte(CONFIG_DONE);
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
