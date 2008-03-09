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

import static tod.agent.AgentDebugFlags.DISABLE_INTERPRETER;
import static tod.agent.AgentDebugFlags.EVENT_INTERPRETER_LOG;

import java.io.PrintStream;
import java.util.StringTokenizer;


/**
 * Interprets low-level events sent by the instrumentation code and
 * transforms them into higher-level events.
 * @author gpothier
 */
public final class EventInterpreter<T extends EventInterpreter.ThreadData>
{
	static
	{
//		System.out.println("EventInterpreter loaded.");
	}
	
	private static PrintStream itsPrintStream = AgentDebugFlags.EVENT_INTERPRETER_PRINT_STREAM;
	
	private ThreadLocal<T> itsThreadData = new ThreadLocal<T>() 
	{
		@Override
		protected T initialValue()
		{
			return createThreadData();
		}
	};
	private HighLevelCollector<T> itsCollector;
	
	private static int itsCurrentThreadId = 1;
	
	private int itsHostId;
	
	public EventInterpreter(HighLevelCollector<T> aCollector)
	{
		itsCollector = aCollector;
		
		try
		{
			itsHostId = getHostId();
			if ((itsHostId & ~AgentConfig.HOST_MASK) != 0) 
				throw new RuntimeException("Host id overflow");
		}
		catch (UnsatisfiedLinkError e)
		{
			itsHostId = -1;
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

	private T createThreadData()
	{
		Thread theCurrentThread = Thread.currentThread();
		long theJvmId = theCurrentThread.getId();
		int theId = (getNextThreadId() << AgentConfig.HOST_BITS) | itsHostId;
		T theData = itsCollector.createThreadData(theId);
		itsThreadData.set(theData);
		
		itsCollector.thread(theData, theJvmId, theCurrentThread.getName());
		
		return theData;
	}
	
	private T getThreadData()
	{
		return itsThreadData.get();
	}

	private String getObjectId(Object aObject)
	{
		if (aObject == null) return "null";
		else return ""+Math.abs(ObjectIdentity.get(aObject));
	}
	
	public void logClInitEnter(
			int aBehaviorId, 
			BehaviorCallType aCallType,
			Object aObject, 
			Object[] aArguments)
	{
		logBehaviorEnter(true, aBehaviorId, aCallType, aObject, aArguments);
	}
	
	public void logBehaviorEnter(
			int aBehaviorId, 
			BehaviorCallType aCallType,
			Object aObject, 
			Object[] aArguments)
	{
		logBehaviorEnter(false, aBehaviorId, aCallType, aObject, aArguments);
	}
	
	private void logBehaviorEnter(
			boolean aClInit,
			int aBehaviorId, 
			BehaviorCallType aCallType,
			Object aObject, 
			Object[] aArguments)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);
		
		FrameInfo theFrame = theThread.currentFrame();
		
		if (theFrame.entering && ! aClInit)
		{
			// We come from instrumented code, ie. before/enter scheme
			// Part of the event info is available in the frame, but the
			// event has not been sent yet.
			
			short theDepth = (short) (theThread.getCurrentDepth()-1);
			
			if (EVENT_INTERPRETER_LOG) print(theDepth, String.format(
					"logBehaviorEnter(%d, %d, %s, %s, %s)\n thread: %d, depth: %d\n frame: %s",
					theTimestamp,
					aBehaviorId,
					aCallType,
					getObjectId(aObject),
					aArguments,
					theThread.getId(),
					theDepth,
					theFrame));


			// Partial update of frame info. We must do it here to indicate
			// that we successfully entered the method, thus completing the
			// before/enter scheme. This is necessary for exception event
			// handling
			theFrame.entering = false;
			
			theFrame.callType.call(
					itsCollector,
					theThread,
					theFrame.parentTimestamp,
					theDepth,
					theTimestamp,
					theFrame.probeId,
					true,
					theFrame.behavior,
					aBehaviorId,
					aObject,
					aArguments);
			
			// Finish updating frame info
			theFrame.behavior = aBehaviorId;
			theFrame.probeId = -1;
			theFrame.callType = null;
			theFrame.parentTimestamp = theTimestamp;
		}
		else
		{
			// We come from non instrumented code
			// Or it is an implicit call (eg. static initializer) in the direct
			// control flow of an instrumented method.
			
			int theProbeId = -1;
			
			// If the current frame corresponds to a dry call, adjust the depth
			// (it means we enter an implicit clinit).
			if (theFrame.entering) 
			{
				theThread.incDepthAdjust();
				theProbeId = theFrame.probeId;
			}
			
			short theDepth = (short) theThread.getCurrentDepth();
			
			if (EVENT_INTERPRETER_LOG) print(theDepth, String.format(
					"logBehaviorEnter(%d, %d, %s, %s, %s)\n thread: %d, depth: %d\n frame: %s",
					theTimestamp,
					aBehaviorId,
					aCallType,
					getObjectId(aObject),
					aArguments,
					theThread.getId(),
					theDepth,
					theFrame));
			
			
			aCallType.call(
					itsCollector,
					theThread,
					theFrame.parentTimestamp,
					theDepth,
					theTimestamp,
					theProbeId,
					true,
					0,
					aBehaviorId,
					aObject,
					aArguments);
			
			theThread.pushFrame(false, aBehaviorId, true, theTimestamp, null, -1);
		}
	}

	public void logClInitExit(
			int aProbeId, 
			int aBehaviorId,
			Object aResult)
	{
		logBehaviorExit(true, aProbeId, aBehaviorId, aResult);
	}
	
	public void logBehaviorExit(
			int aProbeId, 
			int aBehaviorId,
			Object aResult)
	{
		logBehaviorExit(false, aProbeId, aBehaviorId, aResult);		
	}
	
	private void logBehaviorExit(
			boolean aClInit,
			int aProbeId, 
			int aBehaviorId,
			Object aResult)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);

		FrameInfo theFrame = theThread.popFrame();
		short theDepth = (short) (theThread.getCurrentDepth()+1);
		assert theFrame.behavior == aBehaviorId;
		assert theFrame.directParent;
		
		if (aClInit && theThread.currentFrame().entering)
		{
			theThread.decDepthAdjust();
		}
		
		if (EVENT_INTERPRETER_LOG) print(theDepth, String.format(
				"logBehaviorExit(%d, %d, %d, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aProbeId,
				aBehaviorId,
				getObjectId(aResult),
				theThread.getId(),
				theDepth,
				theFrame));
		
		itsCollector.behaviorExit(
				theThread,
				theFrame.parentTimestamp,
				theDepth,
				theTimestamp,
				aProbeId,
				aBehaviorId,
				false,
				aResult);
	}
	
	public void logBehaviorExitWithException(
			int aBehaviorId, 
			Object aException)
	{
		if (DISABLE_INTERPRETER) return;
		
		long theTimestamp = AgentUtils.timestamp();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);
		
		if (EVENT_INTERPRETER_LOG)
		{
			System.err.println("Exit with exception:");
			((Throwable) aException).printStackTrace();
			
			itsPrintStream.println(String.format(
				"logBehaviorExitWithException(%d, %d, %s)",
				theTimestamp,
				aBehaviorId,
				aException));
		}


		
		FrameInfo theFrame = theThread.popFrame();
		assert theFrame.behavior == aBehaviorId;
		assert theFrame.directParent;
		
		if (EVENT_INTERPRETER_LOG) itsPrintStream.println(String.format(
				" thread: %d, depth: %d\n frame: %s",
				theThread.getId(),
				theThread.getCurrentDepth(),
				theFrame));
		

		itsCollector.behaviorExit(
				theThread,
				theFrame.parentTimestamp,
				(short) (theThread.getCurrentDepth()+1), // The exit event is at the same depths as other children
				theTimestamp,
				-1,
				aBehaviorId,
				true,
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
		if (DISABLE_INTERPRETER) return;
		T theThread = getThreadData();
		if (theThread.checkIgnoreNextException()) return;
		
		long theTimestamp = AgentUtils.timestamp();
		theTimestamp = theThread.transformTimestamp(theTimestamp);

		FrameInfo theFrame = theThread.currentFrame();
		short theDepth = theThread.getCurrentDepth();

		if (EVENT_INTERPRETER_LOG) print(theDepth, String.format(
				"logExceptionGenerated(%d, %s, %s, %s, %d, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aMethodName,
				aMethodSignature,
				aMethodDeclaringClassSignature,
				aOperationBytecodeIndex,
				aException,
				theThread.getId(),
				theDepth,
				theFrame));
		
		// We check if we really entered in the current parent, or
		// if some exception prevented the call from succeeding.
		if (theFrame.entering) 
		{
			theThread.popFrame();
			theDepth--;
		}

		itsCollector.exception(
				theThread,
				theFrame.parentTimestamp,
				theDepth,
				theTimestamp,
				aMethodName,
				aMethodSignature,
				aMethodDeclaringClassSignature,
				aOperationBytecodeIndex,
				aException);
	}
	
	public void logFieldWrite(
			int aProbeId, 
			int aFieldId,
			Object aTarget, 
			Object aValue)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp_fast();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);

		FrameInfo theFrame = theThread.currentFrame();
		short theDepth = theThread.getCurrentDepth();
		
		if (EVENT_INTERPRETER_LOG) print(theDepth, String.format(
				"logFieldWrite(%d, %d, %d, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aProbeId,
				aFieldId,
				getObjectId(aTarget),
				getObjectId(aValue),
				theThread.getId(),
				theDepth,
				theFrame));

		itsCollector.fieldWrite(
				theThread,
				theFrame.parentTimestamp,
				theDepth,
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
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp_fast();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);
		
		FrameInfo theFrame = theThread.currentFrame();
		
		short theDepth = theThread.getCurrentDepth();
		if (EVENT_INTERPRETER_LOG) print(theDepth, String.format(
				"logNewArray(%d, %d, %s, %d, %d)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aProbeId,
				getObjectId(aTarget),
				aBaseTypeId,
				aSize,
				theThread.getId(),
				theDepth,
				theFrame));
		
		itsCollector.newArray(
				theThread,
				theFrame.parentTimestamp,
				theDepth,
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
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp_fast();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);
		
		FrameInfo theFrame = theThread.currentFrame();
		
		short theDepth = theThread.getCurrentDepth();
		if (EVENT_INTERPRETER_LOG) print(theDepth, String.format(
				"logArrayWrite(%d, %d, %s, %d, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aProbeId,
				getObjectId(aTarget),
				aIndex,
				getObjectId(aValue),
				theThread.getId(),
				theDepth,
				theFrame));
		
		itsCollector.arrayWrite(
				theThread,
				theFrame.parentTimestamp,
				theDepth,
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
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp_fast();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);

		FrameInfo theFrame = theThread.currentFrame();
		short theDepth = theThread.getCurrentDepth();
		
		if (EVENT_INTERPRETER_LOG) print(theDepth, String.format(
				"logLocalVariableWrite(%d, %d, %d, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aProbeId,
				aVariableId,
				getObjectId(aValue),
				theThread.getId(),
				theDepth,
				theFrame));
		
		itsCollector.localWrite(
				theThread,
				theFrame.parentTimestamp,
				theDepth,
				theTimestamp,
				aProbeId,
				aVariableId,
				aValue);
	}
	
	/**
	 * Dry version
	 */
	public void logBeforeBehaviorCallDry(
			int aProbeId, 
			int aBehaviorId,
			BehaviorCallType aCallType)
	{
		if (DISABLE_INTERPRETER) return;
		assert aCallType != null;
		T theThread = getThreadData();

		FrameInfo theFrame = theThread.currentFrame();
		assert theFrame.directParent && theFrame.behavior > 0;
		
		if (EVENT_INTERPRETER_LOG)
		{
			short theDepth = theThread.getCurrentDepth();
			print(theDepth, String.format(
					"logBeforeBehaviorCallDry(%d, %d, %s)\n thread: %d, depth: %d\n frame: %s",
					aProbeId,
					aBehaviorId,
					aCallType,
					theThread.getId(),
					theDepth,
					theFrame));
		}

		theThread.pushFrame(
				true,
				aBehaviorId, 
				true, 
				theFrame.parentTimestamp, 
				aCallType, 
				aProbeId);
	}
	
	public void logBeforeBehaviorCall(
			int aProbeId, 
			int aBehaviorId,
			BehaviorCallType aCallType,
			Object aTarget, 
			Object[] aArguments)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);

		FrameInfo theFrame = theThread.currentFrame();
		short theDepth = theThread.getCurrentDepth();
		
		if (EVENT_INTERPRETER_LOG) print(theDepth, String.format(
				"logBeforeBehaviorCall(%d, %d, %d, %s, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aProbeId,
				aBehaviorId,
				aCallType,
				getObjectId(aTarget),
				aArguments,
				theThread.getId(),
				theDepth,
				theFrame));
		
		aCallType.call(
				itsCollector,
				theThread,
				theFrame.parentTimestamp,
				theDepth,
				theTimestamp,
				aProbeId,
				false,
				aBehaviorId,
				0,
				aTarget,
				aArguments);
		
		theThread.pushFrame(false, aBehaviorId, false, theTimestamp, null, -1);
	}

	/**
	 * Dry version
	 */
	public void logAfterBehaviorCallDry()
	{
		if (DISABLE_INTERPRETER) return;
		// Nothing to do here, maybe we don't need this message
		T theThread = getThreadData();
		FrameInfo theFrame = theThread.currentFrame();
		short theDepth = theThread.getCurrentDepth();
		
		if (EVENT_INTERPRETER_LOG)
		{
			print(theDepth, String.format(
					"logAfterBehaviorCallDry()\n thread: %d, depth: %d\n frame: %s",
					theThread.getId(),
					theDepth,
					theFrame));
		}
		
		if (theFrame.entering)
		{
			long theTimestamp = AgentUtils.timestamp();
			theTimestamp = theThread.transformTimestamp(theTimestamp);
			
			String theMessage = String.format(
					"[EventInterpreter] Unexpected dry after (check trace scope) - bid: %d, thread: %d, p.ts: %d, ts: %d",
					theFrame.behavior,
					theThread.getId(),
					theFrame.parentTimestamp,
					theTimestamp);
			
			itsPrintStream.println(theMessage);
			System.err.println(theMessage);

			theThread.popFrame();
			
			// Send message anyway, although there is a lot of missing info
			String s = new String("<?TOD?-dry>");
			logBeforeBehaviorCall(-1, theFrame.behavior, BehaviorCallType.METHOD_CALL, s, null);
			logAfterBehaviorCall(-1, theFrame.behavior, s, s);
		}
	}
	
	public void logAfterBehaviorCall(
			int aProbeId, 
			int aBehaviorId, 
			Object aTarget,
			Object aResult)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);

		FrameInfo theFrame = theThread.popFrame();
		short theDepth = (short) (theThread.getCurrentDepth()+1);
		assert theFrame.behavior == aBehaviorId;
		
		if (EVENT_INTERPRETER_LOG) print(theDepth, String.format(
				"logAfterBehaviorCall(%d, %d, %d, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aProbeId,
				aBehaviorId,
				getObjectId(aTarget),
				getObjectId(aResult),
				theThread.getId(),
				theDepth,
				theFrame));
		
		itsCollector.behaviorExit(
				theThread,
				theFrame.parentTimestamp,
				theDepth,
				theTimestamp,
				aProbeId,
				aBehaviorId,
				false,
				aResult);
	}
	
	public void logAfterBehaviorCallWithException(
			int aProbeId, 
			int aBehaviorId, 
			Object aTarget, 
			Object aException)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);

		FrameInfo theFrame = theThread.popFrame();
		short theDepth = (short) (theThread.getCurrentDepth()+1);
		assert theFrame.behavior == aBehaviorId;
		
		if (EVENT_INTERPRETER_LOG) print(theDepth, String.format(
				"logAfterBehaviorCallWithException(%d, %d, %d, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aProbeId,
				aBehaviorId,
				getObjectId(aTarget),
				aException,
				theThread.getId(),
				theDepth,
				theFrame));

		itsCollector.behaviorExit(
				theThread,
				theFrame.parentTimestamp,
				theDepth,
				theTimestamp,
				aProbeId,
				aBehaviorId,
				true,
				aException);
	}
	
	public void logOutput(
			Output aOutput, 
			byte[] aData)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);

		FrameInfo theFrame = theThread.currentFrame();
		short theDepth = theThread.getCurrentDepth();
		
		if (EVENT_INTERPRETER_LOG) print(theDepth, String.format(
				"logOutput(%d, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aOutput,
				aData,
				theThread.getId(),
				theDepth,
				theFrame));

		itsCollector.output(
				theThread,
				theFrame.parentTimestamp,
				theDepth,
				theTimestamp,
				aOutput,
				aData);
	}

	/**
	 * Sends a request to clear the database.
	 */
	public void clear()
	{
		if (DISABLE_INTERPRETER) return;
		T theThread = getThreadData();
		itsCollector.clear(theThread);
	}
	
	/**
	 * Sends a request to flush buffered events
	 */
	public void flush()
	{
		if (DISABLE_INTERPRETER) return;
		throw new UnsupportedOperationException();
	}
	
	public static class ThreadData 
	{
		/**
		 * Internal thread id.
		 * These are different than JVM thread ids, which can potentially
		 * use the whole 64 bits range. Internal ids are sequential.
		 */
		private int itsId;
		
		// We don't want to use ArrayList here, it might be instrumented.
		private FrameInfo[] itsStack = new FrameInfo[10000];
		private int itsStackSize;
		
		private FrameInfo[] itsFreeFrames = new FrameInfo[10000];
		private int itsFreeFramesSize;
		
		/**
		 * Current partial serial number. if two events of the same thread have the same
		 * timestamp, they are assigned sequential serial numbers.
		 */
		private byte itsSerial = 0;
		
		private long itsLastTimestamp = 0;
		
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
		
		/**
		 * We need to adjust the depth given to events for the case of a dry before
		 * followed by an implicit clinit execution.
		 */
		private int itsDepthAdjust = 0;

		
		public ThreadData(int aId)
		{
			itsId = aId;
			pushFrame(false, 0, false, 0, null, -1);
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

		private FrameInfo getFreeFrame()
		{
			if (itsFreeFramesSize == 0) return new FrameInfo();
			else 
			{
				return itsFreeFrames[--itsFreeFramesSize];
			}
		}
		
		public FrameInfo pushFrame(
				boolean aEntering,
				int aBehavior, 
				boolean aDirectParent,
				long aParentTimestamp,
				BehaviorCallType aCallType, 
				int aProbeId)
		{
//			assert aCallType != null;
			FrameInfo theFrame = getFreeFrame();
			theFrame.entering = aEntering;
			theFrame.behavior = aBehavior;
			theFrame.directParent = aDirectParent;
			theFrame.parentTimestamp = aParentTimestamp;
			theFrame.callType = aCallType;
			theFrame.probeId = aProbeId;
			
			itsStack[itsStackSize++] = theFrame;
//			if (itsStackSize % 10 == 0) System.out.println(itsStackSize);
			
			return theFrame;
		}
		
		public FrameInfo popFrame()
		{
			FrameInfo theFrame = itsStack[--itsStackSize];
			itsFreeFrames[itsFreeFramesSize++] = theFrame;

			return theFrame;
		}
		
		public FrameInfo currentFrame()
		{
			return itsStack[itsStackSize-1];
		}
		
		/**
		 * Returns the current depth of the call stack, minus the depth adjustment.
		 */
		public short getCurrentDepth()
		{
			return (short) (itsStackSize - itsDepthAdjust);
		}
		
		public void incDepthAdjust()
		{
			itsDepthAdjust++;
		}
		
		public void decDepthAdjust()
		{
			assert itsDepthAdjust > 0;
			itsDepthAdjust--;
		}
		
		
		
		private byte getNextSerial(long aTimestamp)
		{
			if (aTimestamp < itsLastTimestamp) 
			{
				System.err.println(String.format(
						"EventInterpreter.getNextSerial: Out of order on single thread, BUG! (current: %s, previous: %s) tid: %02d",
//						AgentUtils.formatTimestamp(aTimestamp),
//						AgentUtils.formatTimestamp(itsLastTimestamp),
						AgentUtils.transformTimestamp(aTimestamp, (byte) 0),
						AgentUtils.transformTimestamp(itsLastTimestamp, (byte) 0),
						itsId));
				
				itsSerial = 0;
				return 0;
			}
			else if (aTimestamp == itsLastTimestamp) 
			{
				itsLastTimestamp = aTimestamp;
				return ++itsSerial;
			}
			else
			{
				itsLastTimestamp = aTimestamp;
				itsSerial = 0;
				return 0;
			}
		}
		
		public long transformTimestamp(long aTimestamp)
		{
			return AgentUtils.transformTimestamp(aTimestamp, getNextSerial(aTimestamp));
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
		
	}

	private static class FrameInfo
	{
		/**
		 * This flag is set to true in the before phase of a before/enter scheme
		 */
		public boolean entering;
		
		/**
		 * Behavior currently executing in the current frame,
		 * or called behavior in the case of a before/enter scheme
		 */
		public int behavior;
		
		/**
		 * Whether future events will be direct children of the behavior 
		 * corresponding to this frame.
		 */
		public boolean directParent;
		
		/**
		 * Transformed timestamp of the event that started the current frame.
		 * see {@link AgentUtils#transformTimestamp(long, byte)}
		 */
		public long parentTimestamp;
		
		/**
		 * Type of behavior call, in the case of a before/enter scheme
		 */
		public BehaviorCallType callType;
		
		/**
		 * Operation bytecode index in the case of a before/enter scheme
		 */
		public int probeId;
		
		@Override
		public String toString()
		{
			return String.format(
					"beh.: %d, c.type: %s, d.p.: %s, ent.: %s, p.ts.: %d",
					behavior,
					callType,
					directParent,
					entering,
					parentTimestamp);
		}
	}

	private static void print(int aDepth, String aString)
	{
		itsPrintStream.println(indent(aString, aDepth, "  "));
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