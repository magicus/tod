/*
 * Created on Oct 13, 2004
 */
package tod.core.transport;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import remotebci.SocketThread;
import tod.agent.AgentReady;
import tod.core.BehaviourType;
import tod.core.ILogCollector;
import tod.core.Output;

/**
 * This collector sends the events to a socket.
 * The other end of the socket should be a {@link tod.core.transport.LogReceiver}.
 * @author gpothier
 */
public class SocketCollector implements ILogCollector
{
	private ThreadLocal<ThreadData> itsThreadInfos;
	private List<Reference<ThreadData>> itsThreadInfosList;
	private MyThread itsThread;
	
	public SocketCollector(String aHostname, int aPort) throws IOException 
	{
		this (new MyThread (new Socket(aHostname, aPort)));
	}
	
	public SocketCollector(int aPort) throws IOException 
	{
		this (new MyThread (new ServerSocket(aPort)));
	}
	
	public SocketCollector(MyThread aThread)
	{
		itsThread = aThread;
		itsThreadInfos = new ThreadLocal<ThreadData>();
		itsThreadInfosList = new LinkedList<Reference<ThreadData>>();
		itsThread.setThreadInfosList(itsThreadInfosList);
		AgentReady.READY = true;
	}
	
	/**
	 * Waits until a client connects.
	 */
	public void waitForClient()
	{
		try
		{
			synchronized (itsThread)
			{
				itsThread.wait();
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private ThreadData createThreadData()
	{
		Thread theCurrentThread = Thread.currentThread();
		long theId = theCurrentThread.getId();
		ThreadData theData = new ThreadData();
		itsThreadInfos.set(theData);
		itsThreadInfosList.add (new WeakReference<ThreadData>(theData));
		
		registerThread(theId, theCurrentThread.getName());
		
		return theData;
	}
	
	private ThreadData getThreadInfo()
	{
		ThreadData theData = itsThreadInfos.get();
		if (theData == null) theData = createThreadData();
		return theData;
	}
	
	private boolean shouldLog()
	{
		return false;
	}
	
	private void log (String aText)
	{
		if (shouldLog()) System.out.println(aText);
	}
	
	public void logBehaviorEnter(
			long aTimestamp, 
			long aThreadId, 
			int aLocationId)
	{
		ThreadData theData = getThreadInfo();
		if (theData.isSending()) return;
        try
        {
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendBehaviorEnter(theStream, aTimestamp, aThreadId, aLocationId);
            theData.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}
	
	public void logBehaviorExit(
			long aTimestamp,
			long aThreadId,
			int aLocationId)
	{
		ThreadData theData = getThreadInfo();
		if (theData.isSending()) return;
        try
        {
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendBehaviorExit(theStream, aTimestamp, aThreadId, aLocationId);
            theData.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}
	
    public void logBeforeMethodCall(
            long aTimestamp,
            long aThreadId, 
            int aOperationBytecodeIndex, 
            int aMethodLocationId,
            Object aTarget,
            Object[] aArguments)
    {
		ThreadData theData = getThreadInfo();
		if (theData.isSending()) return;
    	
//    	System.out.println(String.format(
//    			"Before method call: %d, %d, %d, %d, %s, %d",
//    			aTimestamp,
//    			aThreadId,
//    			aOperationBytecodeIndex,
//    			aMethodLocationId,
//    			aTarget,
//    			aArguments != null ? aArguments.length : 0));
//    	
//    	if (aArguments != null) for (Object theObject : aArguments)
//		{
//			System.out.println(theObject);
//		}
//    	
        try
        {
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendBeforeMethodCall(theStream, aTimestamp, aThreadId, aOperationBytecodeIndex, aMethodLocationId, aTarget, aArguments);
            theData.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
    }

	
	public void logAfterMethodCall(
			long aTimestamp, 
            long aThreadId, 
            int aOperationBytecodeIndex,
            int aMethodLocationId,
            Object aTarget, 
            Object aResult)
	{
		ThreadData theData = getThreadInfo();
		if (theData.isSending()) return;

//    	System.out.println(String.format(
//				"After method call: %d, %d, %d, %d, %s, %s",
//				aTimestamp,
//				aThreadId,
//				aOperationBytecodeIndex,
//				aMethodLocationId,
//				aTarget,
//				aResult));

        try
        {
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendAfterMethodCall(theStream, aTimestamp, aThreadId, aOperationBytecodeIndex, aMethodLocationId, aTarget, aResult);
            theData.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}

	public void logFieldWrite(
			long aTimestamp, 
            long aThreadId, 
            int aOperationBytecodeIndex,
            int aFieldLocationId, 
            Object aTarget, 
            Object aValue)
	{
		ThreadData theData = getThreadInfo();
		if (theData.isSending()) return;
        try
        {
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendFieldWrite(theStream, aTimestamp, aThreadId, aOperationBytecodeIndex, aFieldLocationId, aTarget, aValue);
            theData.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}

	public void logInstantiation(
			long aTimestamp, 
            long aThreadId,
            int aOperationBytecodeIndex, 
            int aTypeLocationId,
            Object aInstance)
	{
		ThreadData theData = getThreadInfo();
		if (theData.isSending()) return;
//    	System.out.println(String.format(
//				"Instantiation: %d, %d, %d, %d, %s",
//				aTimestamp,
//				aThreadId,
//				aOperationBytecodeIndex,
//				aTypeLocationId,
//				aInstance));
		
        try
        {
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendInstantiation(theStream, aTimestamp, aThreadId, aOperationBytecodeIndex, aTypeLocationId, aInstance);
            theData.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}

	public void logLocalVariableWrite(
			long aTimestamp,
            long aThreadId,
            int aOperationBytecodeIndex, 
            int aVariableId,
            Object aTarget,
            Object aValue)
	{
		ThreadData theData = getThreadInfo();
		if (theData.isSending()) return;
        try
        {
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendLocalVariableWrite(theStream, aTimestamp, aThreadId, aOperationBytecodeIndex, aVariableId, aTarget, aValue);
            theData.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}

	public void logOutput(
			long aTimestamp, 
            long aThreadId, 
            Output aOutput, 
            byte[] aData)
	{
		ThreadData theData = getThreadInfo();
		if (theData.isSending()) return;
        try
        {
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendOutput(theStream, aTimestamp, aThreadId, aOutput, aData);
            theData.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }

	}

	public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
	{
		log("SocketCollector.registerClass("+aTypeId+", "+aTypeName+")");
		ThreadData theData = getThreadInfo();
		
		try
		{
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendRegisterType(theStream, aTypeId, aTypeName, aSupertypeId, aInterfaceIds);
			theData.packetEnd();
		}
		catch (IOException e)
		{
        	throw new RuntimeException(e);
		}
	}
	
    
    public void registerBehavior(
    		BehaviourType aBehaviourType,
            int aBehaviourId, 
            int aTypeId,
            String aBehaviourName,
            String aSignature)
    {
    	ThreadData theData = getThreadInfo();
		try
		{
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendRegisterBehavior(theStream, aBehaviourType, aBehaviourId, aTypeId, aBehaviourName, aSignature);
			theData.packetEnd();
		}
		catch (IOException e)
		{
        	throw new RuntimeException(e);
		}
	}
	
    public void registerBehaviorAttributes(
    		int aBehaviourId, 
    		LineNumberInfo[] aLineNumberTable, 
    		LocalVariableInfo[] aLocalVariableTable)
    {
    	ThreadData theData = getThreadInfo();
    	try
    	{
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendRegisterBehaviorAttributes(theStream, aBehaviourId, aLineNumberTable, aLocalVariableTable);
    		theData.packetEnd();
    	}
    	catch (IOException e)
    	{
        	throw new RuntimeException(e);
    	}
    }
    
	public void registerField(int aFieldId, int aClassId, String aFieldName)
	{
		log("SocketCollector.registerField("+aFieldId+", "+aClassId+", "+aFieldName+")");
		ThreadData theData = getThreadInfo();
		
		try
		{
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendRegisterField(theStream, aFieldId, aClassId, aFieldName);
			theData.packetEnd();
		}
		catch (IOException e)
		{
        	throw new RuntimeException(e);
		}
	}
	
	public void registerFile(int aFileId, String aFileName)
	{
		log("SocketCollector.registerFile("+aFileId+", "+aFileName+")");
		ThreadData theData = getThreadInfo();
		
		try
		{
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendRegisterFile(theStream, aFileId, aFileName);
			theData.packetEnd();
		}
		catch (IOException e)
		{
        	throw new RuntimeException(e);
		}
	}
	
	public void registerThread (
			long aThreadId,
			String aName)
	{
		log("SocketCollector.registerThread("+aThreadId+", "+aName+")");
		ThreadData theData = getThreadInfo();
		
		try
		{
        	DataOutputStream theStream = theData.packetStart();
        	CollectorPacketWriter.sendRegisterThread(theStream, aThreadId, aName);
			theData.packetEnd();
		}
		catch (IOException e)
		{
        	throw new RuntimeException(e);
		}
		
	}

	
	/**
	 * Maintains the thread specific information that permits to construct
	 * log packets without blocking other threads.
	 * Log packets are constructed in a buffer. When the packet is ready,  
	 * the synchronized {@link #packetEnd()} method should be called, which
	 * copies the packet to a bigger buffer that stores full packets until it
	 * is requested to send them to a socket.
	 * @author gpothier
	 */
	private static class ThreadData
	{
		/**
		 * A wrapper around {@link #itsBuffer}
		 */
		private DataOutputStream itsDataOutputStream;
		
		/**
		 * In construction packet buffer
		 */
		private ByteArrayOutputStream itsBuffer;
		
		/**
		 * Full packets buffer
		 */
		private ByteArrayOutputStream itsLog;
		
		private boolean itsSending = false;
		
		
		public ThreadData()
		{
			itsBuffer = new ByteArrayOutputStream();
			itsLog = new ByteArrayOutputStream(32768);
			itsDataOutputStream = new DataOutputStream(itsBuffer);
		}

		public DataOutputStream packetStart()
		{
			if (itsSending) throw new RuntimeException();
			itsSending = true;
			return itsDataOutputStream;
		}
		
		public boolean isSending()
		{
			return itsSending;
		}
		
		public synchronized void packetEnd()
		{
			if (! itsSending) throw new RuntimeException();
			itsSending = false;
			try
			{
				itsDataOutputStream.flush();
//				System.out.println("Flushing "+itsBuffer.size()+" bytes.");
				itsBuffer.writeTo(itsLog);
				itsBuffer.reset();
//				itsLog.flush();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		public synchronized void sendLog (OutputStream aOutputStream) throws IOException
		{
			if (itsLog.size() > 0)
			{
//				System.out.println("Sending "+itsLog.size()+" bytes");
				itsLog.writeTo(aOutputStream);
				itsLog.reset();
			}
		}
	}
	
	private static class MyThread extends SocketThread
	{
		private List<Reference<ThreadData>> itsThreadInfosList;
		
		public MyThread(ServerSocket aServerSocket)
		{
			super(aServerSocket, false);
			init();
		}

		public MyThread(Socket aSocket)
		{
			super(aSocket, false);
			init();
		}
		
		private void init()
		{
			Runtime.getRuntime().addShutdownHook(new MyShutdownHook(this));
			
			setDaemon(true);
			start();
		}

		public void setThreadInfosList(List<Reference<ThreadData>> aThreadInfosList)
		{
			itsThreadInfosList = aThreadInfosList;
		}

		protected void process (
				OutputStream aOutputStream, 
				InputStream aInputStream) 
				throws IOException, InterruptedException
		{
			try
			{
				send(aOutputStream);
				Thread.sleep (500);
			}
			catch (ConcurrentModificationException e1)
			{
			}
		}
		
		@Override
		protected void processInterrupted(
				OutputStream aOutputStream, 
				InputStream aInputStream) 
				throws IOException, InterruptedException
		{
			send(aOutputStream);
		}

		private void send(OutputStream aOutputStream) throws IOException
		{
			if (itsThreadInfosList == null) return;
			
			for (Iterator<Reference<ThreadData>> theIterator = itsThreadInfosList.iterator(); theIterator.hasNext();)
			{
				Reference<ThreadData> theReference = theIterator.next();
				ThreadData theData = theReference.get();
				if (theData != null)
				{
					theData.sendLog(aOutputStream);
				}
				else
				{
					theIterator.remove();
					continue;
				}
			}
		}
	}
	
	private static class MyShutdownHook extends Thread
	{
		private MyThread itsThread;
		
		public MyShutdownHook(MyThread aThread)
		{
			itsThread = aThread;
		}

		@Override
		public void run()
		{
			itsThread.interrupt();
		}
	}
}
