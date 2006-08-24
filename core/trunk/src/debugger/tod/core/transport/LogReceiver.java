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

import tod.core.ILogCollector;
import tod.core.bci.IInstrumenter;
import tod.core.bci.RemoteInstrumenter;

/**
 * Receives log events from a logged application through a socket and
 * forwards them to a {@link tod.core.ILogCollector}.
 * It can be setup so that it connects to an already running application,
 * or to wait for incoming connections from applications
 * @author gpothier
 */
public class LogReceiver extends SocketThread
{
	private final ILogCollector itsCollector;
	private final IInstrumenter itsInstrumenter;
	private DataInputStream itsStream;
	
	/**
	 * Waits for incoming connections on the specified socket
	 * @param aCollector The collector to which the events are forwarded.
	 */
	public static void server (
			ILogCollector aCollector,
			IInstrumenter aInstrumenter,
			int aPort) throws IOException
	{
		new LogReceiver(aCollector, aInstrumenter, new ServerSocket(aPort));
	}
	
	/**
	 * Waits for incoming connections on the specified socket
	 * @param aCollector The collector to which the events are forwarded.
	 */
	public LogReceiver(
			ILogCollector aCollector,
			IInstrumenter aInstrumenter,
			ServerSocket aServerSocket)
	{
		super(aServerSocket);
		itsCollector = aCollector;
		itsInstrumenter = aInstrumenter;
	}

	/**
	 * Connects to an already running aplication through the specified socket.
	 * @param aSocket The socket used to connect.
	 * @param aCollector The collector to which the events are forwarded.
	 */
	public LogReceiver(
			ILogCollector aCollector,
			IInstrumenter aInstrumenter,
			Socket aSocket)
	{
		super(aSocket);
		itsCollector = aCollector;
		itsInstrumenter = aInstrumenter;
	}
	
	private ILogCollector getCollector()
	{
		return itsCollector;
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
						itsInstrumenter,
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