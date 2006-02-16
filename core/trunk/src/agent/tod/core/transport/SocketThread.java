package tod.core.transport;
/*
 * Created on Oct 25, 2004
 */

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ConcurrentModificationException;

/**
 * A thread that runs an infinite processing loop over a socket.
 * It can optionally be configured to accept incoming connections
 * @author gpothier
 */
public abstract class SocketThread extends Thread
{
	private ServerSocket itsServerSocket;
	private Socket itsSocket;
	
	/**
	 * Creates a socket thread that accepts incoming connections
	 */
	public SocketThread(ServerSocket aServerSocket)
	{
        this (aServerSocket, true);
	}
	
	/**
	 * Creates a socket thread that accepts incoming connections
     * @param aStart Whether to start the thread immediately.
	 */
	public SocketThread(ServerSocket aServerSocket, boolean aStart)
	{
	    itsServerSocket = aServerSocket;
	    if (aStart) start();
	}
	
	/**
	 * Creates a socket thread using an existing connection. 
	 */
	public SocketThread(Socket aSocket)
	{
		this (aSocket, true);
	}
	
	/**
	 * Creates a socket thread using an existing connection. 
	 */
	public SocketThread(Socket aSocket, boolean aStart)
	{
		itsSocket = aSocket;
	    if (aStart) start();
	}
	
	/**
	 * Returns a label for this socket thread.
	 */
	public String getLabel()
	{
		return ""+this+" ("+getClass()+")";
	}
	
	public final void run()
	{
		try
		{
			System.out.println(getLabel()+": started.");
			while (accept())
			{
				try
				{
					if (Thread.currentThread().isInterrupted())
					{
						processInterrupted();
						return;
					}
					if (itsSocket == null) waitForClient();
					loop ();
				}
				catch (EOFException e)
				{
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				
				disconnected();
				Thread.sleep (500);
				if (itsServerSocket != null) itsSocket = null;
			}
		}
		catch (InterruptedException e)
		{
			processInterrupted();
		}
		finally
		{
			System.out.println(getLabel()+": finished.");
			if (itsServerSocket != null)
			{
				try
				{
					itsServerSocket.close();
					System.out.println(getLabel()+": closed socket");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private void waitForClient() throws IOException
	{
		System.out.println(getLabel()+": waiting for client to conect...");
		accepting();
		itsSocket = itsServerSocket.accept();
		accepted();
		System.out.println(getLabel()+": accepted connection from "+itsSocket);
		
		synchronized (this)
		{
			notify();
		}
	}

	/**
	 * This method indicates whether to accept new connections.
	 */
	protected boolean accept()
	{
		return true;
	}
	
	/**
	 * This method is called when this socked starts waiting for incoming connections.
	 */
	protected void accepting()
	{
	}

	/**
	 * This method is called when this socked accepted a connection. 
	 */
	protected void accepted()
	{
	}

	/**
	 * This method is called when the socked looses connection
	 */
	protected void disconnected()
	{
	}
	
	private void processInterrupted()
	{
		try
		{
			processInterrupted(itsSocket.getOutputStream(), itsSocket.getInputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is called if the thread is interrupted.
	 * Subclasses can override it to perform some (fast) cleanup.
	 */
	protected void processInterrupted(
			OutputStream aOutputStream, 
			InputStream aInputStream) 
			throws IOException, InterruptedException
	{
	}
	
	protected void loop () throws IOException, InterruptedException
	{
		while (itsSocket.isConnected()) process();
	}
	
	/**
	 * Calls {@link #process(OutputStream, InputStream)} with the appropriate streams.
	 */
	protected final void process() throws IOException, InterruptedException
	{
		process(itsSocket.getOutputStream(), itsSocket.getInputStream());
	}
	
	/**
	 * This method is called repeatedly while the socket is connected
	 */
	protected abstract void process (
			OutputStream aOutputStream, 
			InputStream aInputStream) 
			throws IOException, InterruptedException; 
}