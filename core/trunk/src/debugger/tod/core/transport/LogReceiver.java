/*
 * Created on Oct 18, 2004
 */
package tod.core.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import tod.bci.RemoteInstrumenter;
import tod.core.ILogCollector;
import tod.session.ASMDebuggerConfig;

/**
 * Receives log events from a logged application through a socket and
 * forwards them to a {@link tod.core.ILogCollector}.
 * It can be setup so that it connects to an already running application,
 * or to wait for incoming connections from applications
 * @author gpothier
 */
public class LogReceiver extends SocketThread
{
	private final ASMDebuggerConfig itsConfig;
	private DataInputStream itsStream;
	
	/**
	 * Waits for incoming connections on the specified socket
	 * @param aCollector The collector to which the events are forwarded.
	 */
	public static void server (
			ASMDebuggerConfig aConfig,
			int aPort) throws IOException
	{
		new LogReceiver(aConfig, new ServerSocket(aPort));
	}
	
	/**
	 * Waits for incoming connections on the specified socket
	 * @param aCollector The collector to which the events are forwarded.
	 */
	public LogReceiver(
			ASMDebuggerConfig aConfig,
			ServerSocket aServerSocket)
	{
		super(aServerSocket);
		itsConfig = aConfig;
	}

	/**
	 * Connects to an already running aplication through the specified socket.
	 * @param aSocket The socket used to connect.
	 * @param aCollector The collector to which the events are forwarded.
	 */
	public LogReceiver(
			ASMDebuggerConfig aConfig,
			Socket aSocket)
	{
		super(aSocket);
		itsConfig = aConfig;
	}
	
	private ILogCollector getCollector()
	{
		return itsConfig.getCollector();
	}

	protected void process(
			OutputStream aOutputStream, 
			InputStream aInputStream) 
			throws IOException
	{
		itsStream = new DataInputStream(aInputStream);
		DataOutputStream theOutputStream = new DataOutputStream(aOutputStream);

		while (true)
		{
			byte theCommand = itsStream.readByte();
			
			if (theCommand == RemoteInstrumenter.INSTRUMENT_CLASS)
			{
				RemoteInstrumenter.processInstrumentClassCommand(
						itsConfig.getInstrumenter(),
						itsStream,
						theOutputStream,
						null);
			}
			else
			{
				MessageType theType = MessageType.values()[theCommand];
				CollectorPacketReader.readPacket(itsStream, getCollector(), theType);
			}
		}
	}
	
	
}