/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.agent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import tod.agent.transport.Command;
import tod.agent.transport.LowLevelEventWriter;
import tod.agent.transport.NakedLinkedList;
import tod.agent.transport.PacketBufferSender;


/**
 * Instrumented code calls the log* methods of this class, which serializes the
 * event data and sends it through a socket.
 * @author gpothier
 */
public final class EventCollector 
{
	private static PrintStream itsPrintStream = AgentDebugFlags.EVENT_INTERPRETER_PRINT_STREAM;
	
	private ThreadLocal<ThreadData> itsThreadData = new ThreadLocal<ThreadData>() 
	{
		@Override
		protected ThreadData initialValue()
		{
			return createThreadData();
		}
	};
	
	private SocketChannel itsChannel;
	private ThreadData itsDefaultThreadData;
	
	private List<ThreadData> itsThreadDataList = new ArrayList<ThreadData>();
	private PacketBufferSender itsSender;

	
	private static int itsCurrentThreadId = 1;
	
	public EventCollector(String aHostname, int aPort) throws IOException 
	{
		this (SocketChannel.open(new InetSocketAddress(aHostname, aPort)));
	}
	
	public EventCollector(SocketChannel aChannel)
	{
		itsChannel = aChannel;
		
		// Send initialization
		try
		{
			DataOutputStream theStream = new DataOutputStream(itsChannel.socket().getOutputStream());
			theStream.writeInt(AgentConfig.CNX_JAVA);
			theStream.writeUTF(AgentConfig.getClientName());
			theStream.flush();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		
		itsSender = new PacketBufferSender(itsChannel);
		new CommandReceiver().start();
		
		AgentReady.COLLECTOR_READY = true;

		try
		{
			if ((_AgentConfig.HOST_ID & ~AgentConfig.HOST_MASK) != 0) 
				throw new RuntimeException("Host id overflow");
		}
		catch (UnsatisfiedLinkError e)
		{
			System.err.println("ABORTING:");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private synchronized int getNextThreadId()
	{
		return itsCurrentThreadId++;
	}

	private ThreadData createThreadData()
	{
		Thread theCurrentThread = Thread.currentThread();
		int theId = (getNextThreadId() << AgentConfig.HOST_BITS) | _AgentConfig.HOST_ID;
		long theJvmId = _AgentConfig.JAVA14 ? theId : theCurrentThread.getId();
		ThreadData theThreadData = new ThreadData(theId);
		itsThreadData.set(theThreadData);
		
		assert ! theThreadData.isSending();
		
        try
        {
        	LowLevelEventWriter theWriter = theThreadData.packetStart(0);
        	theWriter.sendThread(theJvmId, theCurrentThread.getName());
            theThreadData.packetEnd();
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        
        if (itsDefaultThreadData == null) itsDefaultThreadData = theThreadData;

		return theThreadData;
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
				formatObj(aObject),
				formatArgs(aArguments));
	}
	
	private String formatObj(Object aObject)
	{
		if (aObject == null) return "null";
		else return aObject.getClass().getName();
	}
	
	private String formatArgs(Object[] aArgs)
	{
		if (aArgs == null) return "[-]";
		StringBuilder theBuilder = new StringBuilder();
		theBuilder.append("[");
		for (Object theArg : aArgs)
		{
			theBuilder.append(formatObj(theArg));
			theBuilder.append(", ");
		}
		theBuilder.append("]");
		
		return theBuilder.toString();
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
				formatObj(aResult));
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
				formatObj(aResult));
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
				formatObj(aTarget),
				formatObj(aValue));
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
				formatObj(aTarget),
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
				"logArrayWrite(th: %d, ts: %d, pid: %d, tgt: %s, i: %d, val: %s)",
				theThread.getId(),
				theTimestamp,
				aProbeId,
				formatObj(aTarget),
				aIndex,
				formatObj(aValue));
	}
	
	public void logInstanceOf(
			int aProbeId, 
			Object aObject,
			int aTypeId,
			int aResult)
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;
		
		ThreadData theThread = getThreadData();
		long theTimestamp = theThread.timestamp();
		
		if (theThread.isSending()) return;
		try
		{
			LowLevelEventWriter theWriter = theThread.packetStart(theTimestamp);
			
			theWriter.sendInstanceOf(
					theTimestamp,
					aProbeId, 
					aObject,
					aTypeId,
					aResult != 0);
			
			theThread.packetEnd();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		
		if (AgentDebugFlags.COLLECTOR_LOG) printf(
				"logInstanceOf(th: %d, ts: %d, pid: %d, obj: %s, t: %d, r: %s)",
				theThread.getId(),
				theTimestamp,
				aProbeId,
				formatObj(aObject),
				aTypeId,
				aResult);
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
				formatObj(aValue));
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
				formatObj(aTarget),
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
        	
        	theWriter.sendAfterBehaviorCallDry(theTimestamp);
        	
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
				formatObj(aTarget),
				formatObj(aResult));

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
				formatObj(aTarget),
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
    	LowLevelEventWriter theWriter = theThread.packetStart(0);
    	theWriter.sendClear();
        theThread.packetEnd();
	}
	
	/**
	 * Sends a request to flush buffered events
	 */
	public void flush()
	{
		if (AgentDebugFlags.COLLECTOR_IGNORE_ALL) return;

		ThreadData theThread = getThreadData();

		assert ! theThread.isSending();
    	LowLevelEventWriter theWriter = theThread.packetStart(0);
    	theWriter.sendFlush();
        theThread.packetEnd();
	}
	
	/**
	 * Sends {@link Command#CMD_END}
	 */
	public void end()
	{
		ThreadData theThread = itsDefaultThreadData != null ? itsDefaultThreadData : getThreadData();
		
		assert ! theThread.isSending();
		LowLevelEventWriter theWriter = theThread.packetStart(0);
		theWriter.sendEnd();
		theThread.packetEnd();
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
		 * Number of events for which the timestamp was approximated.
		 */
		private int itsSeqCount = 0;
		
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

		private final LowLevelEventWriter itsWriter;
		
		private boolean itsSending = false;
		
		private long itsFirstTimestamp;
		private long itsLastTimestamp;
		
		/**
		 * Our own entry in the LRU list
		 */
		private NakedLinkedList.Entry<ThreadData> itsEntry;
		
		public ThreadData(int aId)
		{
			itsId = aId;
			itsWriter = new LowLevelEventWriter(itsSender.createBuffer(itsId));
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
			if (ts > itsTimestamp) 
			{
				itsTimestamp = ts;
				itsSeqCount = 0;
			}
			else 
			{
				itsTimestamp += 1;
				itsSeqCount++;
				if (itsSeqCount > 100)
				{
					ts = Timestamper.update();
					if (ts > itsTimestamp) 
					{
						itsTimestamp = ts;
						itsSeqCount = 0;
					}
				}
			}
			
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
		}
		
		public NakedLinkedList.Entry<ThreadData> getEntry()
		{
			return itsEntry;
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


	/**
	 * This thread reads incoming commands from the database.
	 * @author gpothier
	 */
	private class CommandReceiver extends Thread
	{
		public CommandReceiver()
		{
			super("[TOD] Command receiver");
			setDaemon(true);
		}

		@Override
		public void run()
		{
			try
			{
				DataInputStream theIn = new DataInputStream(itsChannel.socket().getInputStream());
				while(true)
				{
					byte theMessage = theIn.readByte();
					if (theMessage >= Command.BASE)
					{
						Command theCommand = Command.VALUES[theMessage-Command.BASE];
						switch (theCommand)
						{
						case CMD_ENABLECAPTURE:
							boolean theEnable = theIn.readBoolean();
							if (theEnable) 
							{
								System.out.println("[TOD] Enable capture request received.");
								TOD.enableCapture();
							}
							else 
							{
								System.out.println("[TOD] Disable capture request received.");
								TOD.disableCapture();
							}
							break;
							
						default: throw new RuntimeException("Not handled: "+theCommand); 
						}
					}
				}
			}
			catch (Exception e)
			{
				System.err.println("[TOD] FATAL:");
				e.printStackTrace();
				System.exit(1);
			}
			
		}
	}
}
