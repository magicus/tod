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
package tod.agent;

import static tod.agent.AgentDebugFlags.DISABLE_COLLECTOR;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import tod.agent.transport.LowLevelEventWriter;
import tod.agent.transport.NakedLinkedList;
import tod.agent.transport.NakedLinkedList.Entry;


/**
 * Instrumented code calls the log* methods of this class, which serializes the
 * event data and sends it through a socket.
 * @author gpothier
 */
public final class EventCollector 
{
	static
	{
//		System.out.println("EventInterpreter loaded.");
	}
	
	private static PrintStream itsPrintStream = AgentDebugFlags.EVENT_INTERPRETER_PRINT_STREAM;
	
	private ThreadLocal<ThreadData> itsThreadData = new ThreadLocal<ThreadData>() 
	{
		@Override
		protected ThreadData initialValue()
		{
			return createThreadData();
		}
	};
	
	private List<ThreadData> itsThreadDataList = new ArrayList<ThreadData>();
	private Sender itsSender;
	private SenderThread itsSenderThread = new SenderThread();

	
	private static int itsCurrentThreadId = 1;
	
	private int itsHostId;
	
	
	public EventCollector(String aHostname, int aPort) throws IOException 
	{
		this (new Socket(aHostname, aPort));
	}
	
	public EventCollector(Socket aSocket)
	{
		itsSender = new Sender(aSocket);
		itsSender.sendInit();
		Runtime.getRuntime().addShutdownHook(new MyShutdownHook());
		
		itsSenderThread.start();

		AgentReady.READY = true;

		try
		{
			itsHostId = getHostId();
			if ((itsHostId & ~AgentConfig.HOST_MASK) != 0) 
				throw new RuntimeException("Host id overflow");
		}
		catch (UnsatisfiedLinkError e)
		{
			System.err.println("ABORTING:");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Retrieves the host id that was sent to the native agent.
	 */
	public static native int getHostId ();

	private synchronized int getNextThreadId()
	{
		return itsCurrentThreadId++;
	}

	private ThreadData createThreadData()
	{
		Thread theCurrentThread = Thread.currentThread();
		long theJvmId = theCurrentThread.getId();
		int theId = (getNextThreadId() << AgentConfig.HOST_BITS) | itsHostId;
		ThreadData theThread = new ThreadData(theId);
		itsThreadData.set(theThread);
		
		assert ! theThread.isSending();
		
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(0);
        	theWriter.sendThread(theThread.getId(), theJvmId, theCurrentThread.getName());
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }

		return theThread;
	}
	
	private ThreadData getThreadData()
	{
		return itsThreadData.get();
	}

	public void logClInitEnter(
			int aBehaviorId, 
			BehaviorCallType aCallType,
			Object aObject, 
			Object[] aArguments)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendClInitEnter(
        			theThread.getId(), 
        			theTimestamp,
        			aBehaviorId,
        			aCallType);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }

		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logClInitEnter(th: %d, ts: %d, bid: %d, ct: %s)",
				theThread.getId(),
				theTimestamp,
				aBehaviorId,
				aCallType);
	}
	
	public void logBehaviorEnter(
			int aBehaviorId, 
			BehaviorCallType aCallType,
			Object aObject, 
			Object[] aArguments)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendBehaviorEnter(
        			theThread.getId(), 
        			theTimestamp,
        			aBehaviorId,
        			aCallType,
        			aObject,
        			aArguments);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        
		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logBehaviorEnter(th: %d, ts: %d, bid: %d, ct: %s, tgt: %s, args: %s)",
				theThread.getId(),
				theTimestamp,
				aBehaviorId,
				aCallType,
				aObject,
				formatArgs(aArguments));
	}
	
	private String formatArgs(Object[] aArgs)
	{
		return aArgs != null ? ""+Arrays.asList(aArgs) : "null";
	}
	

	public void logClInitExit(
			int aProbeId, 
			int aBehaviorId,
			Object aResult)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendClInitExit(
        			theThread.getId(), 
        			theTimestamp,
        			aProbeId, 
        			aBehaviorId);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        
		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logClInitExit(th: %d, ts: %d, pid: %d, bid: %d, res: %s)",
				theThread.getId(),
				theTimestamp,
				aProbeId,
				aBehaviorId,
				aResult);
	}
	
	public void logBehaviorExit(
			int aProbeId, 
			int aBehaviorId,
			Object aResult)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendBehaviorExit(
        			theThread.getId(), 
        			theTimestamp,
        			aProbeId,
        			aBehaviorId,
        			aResult);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }

		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logBehaviorExit(th: %d, ts: %d, pid: %d, bid: %d, res: %s)",
				theThread.getId(),
				theTimestamp,
				aProbeId,
				aBehaviorId,
				aResult);
	}
	
	
	public void logBehaviorExitWithException(
			int aBehaviorId, 
			Object aException)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendBehaviorExitWithException(
        			theThread.getId(), 
        			theTimestamp,
        			aBehaviorId,
        			aException);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        
		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logBehaviorExitWithException(th: %d, ts: %d, bid: %d, ex: %s)",
				theThread.getId(),
				theTimestamp,
				aBehaviorId,
				aException);
	}
	
	/**
	 * Sets the ignore next exception flag of the current thread.
	 * This is called by instrumented classes.
	 */
	public void ignoreNextException()
	{
		getThreadData().ignoreNextException();
	}
	
	public void logExceptionGenerated(
			String aMethodName,
			String aMethodSignature,
			String aMethodDeclaringClassSignature, 
			int aOperationBytecodeIndex,
			Object aException)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendExceptionGenerated(
        			theThread.getId(), 
        			theTimestamp,
        			aMethodName,
        			aMethodSignature,
        			aMethodDeclaringClassSignature,
        			aOperationBytecodeIndex,
        			aException);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        
		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logExceptionGenerated(th: %d, ts: %d, mn: %s, sig: %s, dt: %s, ex: %s)",
				theThread.getId(),
				theTimestamp,
				aMethodName,
				aMethodSignature,
				aMethodDeclaringClassSignature,
				aException);
        
	}
	
	public void logFieldWrite(
			int aProbeId, 
			int aFieldId,
			Object aTarget, 
			Object aValue)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendFieldWrite(
        			theThread.getId(), 
        			theTimestamp,
        			aProbeId, 
        			aFieldId,
        			aTarget, 
        			aValue);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        
		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logFieldWrite(th: %d, ts: %d, pid: %d, fid: %d, tgt: %s, val: %s)",
				theThread.getId(),
				theTimestamp,
				aProbeId,
				aFieldId,
				aTarget,
				aValue);
	}
	
	public void logNewArray(
			int aProbeId, 
			Object aTarget,
			int aBaseTypeId,
			int aSize)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendNewArray(
        			theThread.getId(), 
        			theTimestamp,
        			aProbeId, 
        			aTarget,
        			aBaseTypeId,
        			aSize);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        
		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logNewArray(th: %d, ts: %d, pid: %d, tgt: %s, btid: %d, sz: %d)",
				theThread.getId(),
				theTimestamp,
				aProbeId,
				aTarget,
				aBaseTypeId,
				aSize);
	}
	

	public void logArrayWrite(
			int aProbeId, 
			Object aTarget,
			int aIndex,
			Object aValue)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendArrayWrite(
        			theThread.getId(), 
        			theTimestamp,
        			aProbeId, 
        			aTarget,
        			aIndex,
        			aValue);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        
		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logArraye(th: %d, ts: %d, pid: %d, tgt: %s, i: %d, val: %s)",
				theThread.getId(),
				theTimestamp,
				aProbeId,
				aTarget,
				aIndex,
				aValue);
	}
	
	public void logLocalVariableWrite(
			int aProbeId, 
			int aVariableId,
			Object aValue)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendLocalVariableWrite(
        			theThread.getId(), 
        			theTimestamp,
        			aProbeId, 
        			aVariableId, 
        			aValue);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }

		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logLocalVariableWrite(th: %d, ts: %d, pid: %d, vid: %d, val: %s)",
				theThread.getId(),
				theTimestamp,
				aProbeId,
				aVariableId,
				aValue);
	}
	
	public void logBeforeBehaviorCallDry(
			int aProbeId, 
			int aBehaviorId,
			BehaviorCallType aCallType)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendBeforeBehaviorCallDry(
        			theThread.getId(), 
        			theTimestamp,
        			aProbeId, 
        			aBehaviorId,
        			aCallType);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        
		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logBeforeBehaviorCallDry(th: %d, ts: %d, pid: %d, bid: %d, ct: %s)",
				theThread.getId(),
				theTimestamp,
				aProbeId,
				aBehaviorId,
				aCallType);

	}
	
	public void logBeforeBehaviorCall(
			int aProbeId, 
			int aBehaviorId,
			BehaviorCallType aCallType,
			Object aTarget, 
			Object[] aArguments)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendBeforeBehaviorCall(
        			theThread.getId(), 
        			theTimestamp,
        			aProbeId, 
        			aBehaviorId,
        			aCallType,
        			aTarget,
        			aArguments);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        
		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logBeforeBehaviorCall(th: %d, ts: %d, pid: %d, bid: %d, ct: %s, tgt: %s, args: %s)",
				theThread.getId(),
				theTimestamp,
				aProbeId,
				aBehaviorId,
				aCallType,
				aTarget,
				formatArgs(aArguments));

	}

	public void logAfterBehaviorCallDry()
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendAfterBehaviorCallDry(
        			theThread.getId(), 
        			theTimestamp);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        
		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logAfterBehaviorCallDry(th: %d, ts: %d)",
				theThread.getId(),
				theTimestamp);
	}
	
	public void logAfterBehaviorCall(
			int aProbeId, 
			int aBehaviorId, 
			Object aTarget,
			Object aResult)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendAfterBehaviorCall(
        			theThread.getId(), 
        			theTimestamp,
        			aProbeId, 
        			aBehaviorId,
        			aTarget,
        			aResult);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        
		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logAfterBehaviorCall(th: %d, ts: %d, pid: %d, bid: %d, tgt: %s, res: %s)",
				theThread.getId(),
				theTimestamp,
				aProbeId,
				aBehaviorId,
				aTarget,
				aResult);

	}
	
	public void logAfterBehaviorCallWithException(
			int aProbeId, 
			int aBehaviorId, 
			Object aTarget, 
			Object aException)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendAfterBehaviorCallWithException(
        			theThread.getId(), 
        			theTimestamp,
        			aProbeId, 
        			aBehaviorId,
        			aTarget,
        			aException);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        
		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logAfterBehaviorCallWithException(th: %d, ts: %d, pid: %d, bid: %d, tgt: %s, ex: %s)",
				theThread.getId(),
				theTimestamp,
				aProbeId,
				aBehaviorId,
				aTarget,
				aException);
	}
	
	public void logOutput(
			Output aOutput, 
			byte[] aData)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();

		if (theThread.isSending()) return;
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
        	
        	theWriter.sendOutput(
        			theThread.getId(), 
        			theTimestamp,
        			aOutput,
        			aData);
        	
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}

	/**
	 * Sends a request to clear the database.
	 */
	public void clear()
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;

		ThreadData theThread = getThreadData();

		assert ! theThread.isSending();
        try
        {
        	LowLevelEventWriter theWriter = theThread.packetStart(0);
        	theWriter.sendClear();
            theThread.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
	}
	
	/**
	 * Sends a request to flush buffered events
	 */
	public void flush()
	{
		if (DISABLE_COLLECTOR) return;
		throw new UnsupportedOperationException();
	}
	
	private class ThreadData 
	{
		/**
		 * Internal thread id.
		 * These are different than JVM thread ids, which can potentially
		 * use the whole 64 bits range. Internal ids are sequential.
		 */
		private int itsId;
		
		private long itsTimestamp = 0;
		
		/**
		 * This flag permits to avoid reentrancy issues.
		 */
		private boolean itsInCFlow = false;
		
		/**
		 * When this flag is true the next exception generated event
		 * is ignored. This permits to avoid reporting EG events that
		 * are caused by the instrumentation.
		 */
		private boolean itsIgnoreNextException = false;

		private static final int BUFFER_SIZE = AgentConfig.COLLECTOR_BUFFER_SIZE;
		
		/**
		 * A wrapper around {@link #itsBuffer}
		 */
		private final DataOutputStream itsDataOutputStream;

		private final LowLevelEventWriter itsWriter;
		
		/**
		 * In construction packet buffer
		 */
		private final ByteArrayOutputStream itsBuffer;
		
		/**
		 * Full packets buffer
		 */
		private final ByteArrayOutputStream itsLog;
		
		private boolean itsSending = false;
		
		private boolean itsShutDown = false;
		
		private long itsFirstTimestamp;
		private long itsLastTimestamp;
		
		/**
		 * Our own entry in the LRU list
		 */
		private NakedLinkedList.Entry<ThreadData> itsEntry;
		
		public ThreadData(int aId)
		{
			itsId = aId;
			
			itsBuffer = new ByteArrayOutputStream();
			itsLog = new ByteArrayOutputStream(BUFFER_SIZE);
			itsDataOutputStream = new DataOutputStream(itsBuffer);
			itsWriter = new LowLevelEventWriter(itsDataOutputStream);
			
			// Add ourself to LRU list
			itsEntry = itsSenderThread.register(this);
		}
		
		public int getId()
		{
			return itsId;
		}

		/**
		 * This method is called at the beginning of each logXxxx method
		 * to check that we are not already inside event collection code.
		 * 
		 * @return False if we are not top-level
		 */
		public boolean enter()
		{
			if (itsInCFlow) return false;
			
			itsInCFlow = true;
			return true;
		}
		
		public void exit()
		{
			assert itsInCFlow == true;
			itsInCFlow = false;
		}

		/**
		 * Returns an approximate value of the current time.
		 * Ensures that no two successive calls return the same value, so that
		 * all the events of the thread have distinct timestamp values.
		 */
		public long timestamp()
		{
			long ts = Timestamper.t;
			if (ts > itsTimestamp) itsTimestamp = ts;
			else itsTimestamp++;
			
			return itsTimestamp;
		}
		
		/**
		 * Sets the ignore next exception flag.
		 */
		public void ignoreNextException()
		{
			itsIgnoreNextException = true;
		}
		
		/**
		 * Checks if the ignore next exception flag is set, and resets it.
		 */
		public boolean checkIgnoreNextException()
		{
			boolean theIgnoreNext = itsIgnoreNextException;
			itsIgnoreNextException = false;
			return theIgnoreNext;
		}

		public LowLevelEventWriter packetStart(long aTimestamp)
		{
			if (itsSending) throw new RuntimeException();
			itsSending = true;
			
			if (itsFirstTimestamp == 0) itsFirstTimestamp = aTimestamp;
			if (aTimestamp != 0) itsLastTimestamp = aTimestamp;
			
			return itsWriter;
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
				int theRequestedSize = itsBuffer.size();
//				System.out.println("[SocketCollector] Adding "+theRequestedSize+", total: "+itsLog.size());
				if (itsLog.size() + theRequestedSize > BUFFER_SIZE) send();
				if (! AgentDebugFlags.DISABLE_EVENT_SEND) itsBuffer.writeTo(itsLog);
				itsBuffer.reset();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		public synchronized void send()
		{
			if (! itsShutDown && itsLog.size() > 0)
			{
//				System.out.println("[SocketCollector] Sending " + itsLog.size() + " bytes for "+getId());
//				long theDeltaT = itsLastTimestamp-itsFirstTimestamp;
//				itsBiggestDeltaT = Math.max(itsBiggestDeltaT, theDeltaT);
//				System.out.println(String.format(
//						"Sending %d bytes, deltaT: %s, biggest: %s, id: %02d",
//						itsLog.size(),
//						AgentUtils.formatTimestamp(theDeltaT),
//						AgentUtils.formatTimestamp(itsBiggestDeltaT),
//						getId()));
				
				itsSender.sendLog(itsLog);
				itsLog.reset();
			}
			
			itsFirstTimestamp = itsLastTimestamp = 0;
		}
		
		public NakedLinkedList.Entry<ThreadData> getEntry()
		{
			return itsEntry;
		}

		public void shutDown() 
		{
			System.out.println("[TOD] Flushing events for thread " +
					getId() +
					", sending " +
					itsLog.size() +
					" bytes");
			send();
			itsShutDown = true;
		}
	}
	
	private static class Sender
	{
		private Socket itsSocket;
		private OutputStream itsOutputStream;

		public Sender(Socket aSocket)
		{
			itsSocket = aSocket;
			try
			{
				itsOutputStream = itsSocket.getOutputStream();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		/**
		 * Sends signature and client name
		 */
		public void sendInit()
		{
			try
			{
				DataOutputStream theStream = new DataOutputStream(itsOutputStream);
				theStream.writeInt(AgentConfig.CNX_JAVA);
				theStream.writeUTF(AgentConfig.getClientName());
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		public synchronized void sendLog(ByteArrayOutputStream aStream)
		{
			try
			{
				aStream.writeTo(itsOutputStream);
				itsOutputStream.flush();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
		
	private class MyShutdownHook extends Thread
	{
		public MyShutdownHook() 
		{
			super("Shutdown hook (SocketCollector)");
		}

		@Override
		public void run()
		{
			System.out.println("[TOD] Flushing events...");
			
			if (itsThreadDataList == null) return;
			for (ThreadData theData : itsThreadDataList) theData.shutDown();
			
			try
			{
				// Allow some time for buffers to be sent
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
			
			System.out.println("[TOD] Shutting down.");
		}
		
	}
	
	
	/**
	 * This thread periodically flushes the least recently flushed thread data.
	 * This permits to avoid data being cached for too long in waiting threads.
	 * @author gpothier
	 */
	private class SenderThread extends Thread
	{
		private NakedLinkedList<ThreadData> itsLRUList = 
			new NakedLinkedList<ThreadData>();
		
		public SenderThread()
		{
			super("SenderThread");
			setDaemon(true);
			setPriority(MAX_PRIORITY);
		}
		
		public synchronized Entry<ThreadData> register(ThreadData aData)
		{
			Entry<ThreadData> theEntry = itsLRUList.createEntry(aData);
			itsLRUList.addLast(theEntry);
			
			return theEntry;
		}
		
		@Override
		public void run()
		{
			try
			{
				while(true)
				{
					if (itsLRUList.size() > 0)
					{
						Entry<ThreadData> theFirstEntry;
						
						synchronized (this)
						{
							theFirstEntry = itsLRUList.getFirstEntry();
							itsLRUList.remove(theFirstEntry);
							itsLRUList.addLast(theFirstEntry);
						}
						
						ThreadData theFirst = theFirstEntry.getValue();
						
						theFirst.send();
					}

					Thread.sleep(10);
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
		
	}
	
	private static void printf(String aString, Object... aArgs)
	{
		print(String.format(aString, aArgs));
	}
	
	private static void print(String aString)
	{
		itsPrintStream.println(aString);
	}

	/**
	 * Taken from zz.utils - we can't depend on it.
	 */
	public static String indent(String aString, int aIndent, String aPattern)
	{
		StringBuilder theBuilder = new StringBuilder();
		StringTokenizer theTokenizer = new StringTokenizer(aString, "\n");
		while (theTokenizer.hasMoreTokens())
		{
			String theLine = theTokenizer.nextToken();
			for (int i=0;i<aIndent;i++) theBuilder.append(aPattern);
			theBuilder.append(theLine);
			theBuilder.append('\n');
		}
		
		return theBuilder.toString();
	}


}
