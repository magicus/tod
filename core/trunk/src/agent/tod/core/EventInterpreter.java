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
package tod.core;

import tod.agent.AgentUtils;
import static tod.DebugFlags.*;

/**
 * Interprets low-level events sent by the instrumentation code and
 * transforms them into higher-level events.
 * @author gpothier
 */
public final class EventInterpreter<T extends EventInterpreter.ThreadData>
{
	static
	{
		System.out.println("EventInterpreter loaded.");
	}
	
	private ThreadLocal<T> itsThreadInfos = new ThreadLocal<T>();
	private HighLevelCollector<T> itsCollector;
	
	private static int itsCurrentThreadId = 1;
	
	public EventInterpreter(HighLevelCollector<T> aCollector)
	{
		itsCollector = aCollector;
	}

	private synchronized int getNextThreadId()
	{
		return itsCurrentThreadId++;
	}

	private T createThreadData()
	{
		Thread theCurrentThread = Thread.currentThread();
		long theId = theCurrentThread.getId();
		T theData = itsCollector.createThreadData(getNextThreadId());
		itsThreadInfos.set(theData);
		
		itsCollector.thread(theData, theId, theCurrentThread.getName());
		
		return theData;
	}
	
	private T getThreadData()
	{
		T theData = itsThreadInfos.get();
		if (theData == null) theData = createThreadData();
		return theData;
	}

	private String getObjectId(Object aObject)
	{
		if (aObject == null) return "null";
		else return ""+Math.abs(ObjectIdentity.get(aObject));
	}
	
	public void logBehaviorEnter(
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
		short theDepth = (short) (theThread.getCurrentDepth()-1);
		
		if (EVENT_INTERPRETER_LOG) System.out.println(String.format(
				"logBehaviorEnter(%d, %d, %s, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aBehaviorId,
				aCallType,
				getObjectId(aObject),
				aArguments,
				theThread.getId(),
				theDepth,
				theFrame));
		
		if (theFrame.directParent && theFrame.callType != null)
		{
			// We come from instrumented code, ie. before/enter scheme
			// Part of the event info is available in the frame, but the
			// event has not been sent yet.
			theFrame.callType.call(
					itsCollector,
					theThread,
					theFrame.parentTimestamp,
					theDepth,
					theTimestamp,
					theFrame.bytecodeIndex,
					true,
					theFrame.behavior,
					aBehaviorId,
					aObject,
					aArguments);
			
			// Update frame into
			theFrame.behavior = aBehaviorId;
			theFrame.bytecodeIndex = -1;
			theFrame.callType = null;
			theFrame.entering = false;
			theFrame.parentTimestamp = theTimestamp;
		}
		else
		{
			// We come from non instrumented code
			// Or it is an implicit call (eg. static initializer) in the direct
			// control flow of an instrumented method.
			
			aCallType.call(
					itsCollector,
					theThread,
					theFrame.parentTimestamp,
					theThread.getCurrentDepth(),
					theTimestamp,
					-1,
					true,
					0,
					aBehaviorId,
					aObject,
					aArguments);
			
			theThread.pushFrame(false, aBehaviorId, true, theTimestamp, null, -1);
		}
	}

	public void logBehaviorExit(
			int aOperationBytecodeIndex, 
			int aBehaviorId,
			Object aResult)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);
		

		FrameInfo theFrame = theThread.popFrame();
		assert theFrame.behavior == aBehaviorId;
		assert theFrame.directParent;
		
		if (EVENT_INTERPRETER_LOG) System.out.println(String.format(
				"logBehaviorExit(%d, %d, %d, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aOperationBytecodeIndex,
				aBehaviorId,
				getObjectId(aResult),
				theThread.getId(),
				theThread.getCurrentDepth(),
				theFrame));
		
		itsCollector.behaviorExit(
				theThread,
				theFrame.parentTimestamp,
				(short) (theThread.getCurrentDepth()+1), // The exit event is at the same depths as other children
				theTimestamp,
				aOperationBytecodeIndex,
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
			
			System.out.println(String.format(
				"logBehaviorExitWithException(%d, %d, %s)",
				theTimestamp,
				aBehaviorId,
				aException));
		}


		
		FrameInfo theFrame = theThread.popFrame();
		assert theFrame.behavior == aBehaviorId;
		assert theFrame.directParent;
		
		if (EVENT_INTERPRETER_LOG) System.out.println(String.format(
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
	
	public void logExceptionGenerated(
			String aMethodName,
			String aMethodSignature,
			String aMethodDeclaringClassSignature, 
			int aOperationBytecodeIndex,
			Object aException)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);

		FrameInfo theFrame = theThread.currentFrame();

		if (EVENT_INTERPRETER_LOG) System.out.println(String.format(
				"logExceptionGenerated(%d, %%s, %s, %s, %d, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aMethodName,
				aMethodSignature,
				aMethodDeclaringClassSignature,
				aOperationBytecodeIndex,
				aException,
				theThread.getId(),
				theThread.getCurrentDepth(),
				theFrame));
		
		// We check if we really entered in the current parent, or
		// if some exception prevented the call from succeeding.
		if (theFrame.entering) theThread.popFrame();

		itsCollector.exception(
				theThread,
				theFrame.parentTimestamp,
				theThread.getCurrentDepth(),
				theTimestamp,
				aMethodName,
				aMethodSignature,
				aMethodDeclaringClassSignature,
				aOperationBytecodeIndex,
				aException);
	}
	
	public void logFieldWrite(
			int aOperationBytecodeIndex, 
			int aFieldId,
			Object aTarget, 
			Object aValue)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp_fast();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);

		FrameInfo theFrame = theThread.currentFrame();
		
		if (EVENT_INTERPRETER_LOG) System.out.println(String.format(
				"logFieldWrite(%d, %d, %d, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aOperationBytecodeIndex,
				aFieldId,
				getObjectId(aTarget),
				getObjectId(aValue),
				theThread.getId(),
				theThread.getCurrentDepth(),
				theFrame));

		itsCollector.fieldWrite(
				theThread,
				theFrame.parentTimestamp,
				theThread.getCurrentDepth(),
				theTimestamp,
				aOperationBytecodeIndex,
				aFieldId,
				aTarget,
				aValue);
	}
	
	public void logArrayWrite(
			int aOperationBytecodeIndex, 
			Object aTarget,
			int aIndex,
			Object aValue)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp_fast();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);
		
		FrameInfo theFrame = theThread.currentFrame();
		
		if (EVENT_INTERPRETER_LOG) System.out.println(String.format(
				"logArrayWrite(%d, %d, %s, %d, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aOperationBytecodeIndex,
				getObjectId(aTarget),
				aIndex,
				getObjectId(aValue),
				theThread.getId(),
				theThread.getCurrentDepth(),
				theFrame));
		
		itsCollector.arrayWrite(
				theThread,
				theFrame.parentTimestamp,
				theThread.getCurrentDepth(),
				theTimestamp,
				aOperationBytecodeIndex,
				aTarget,
				aIndex,
				aValue);
	}
	
	public void logLocalVariableWrite(
			int aOperationBytecodeIndex,
			int aVariableId,
			Object aValue)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp_fast();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);

		FrameInfo theFrame = theThread.currentFrame();
		
		if (EVENT_INTERPRETER_LOG) System.out.println(String.format(
				"logLocalVariableWrite(%d, %d, %d, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aOperationBytecodeIndex,
				aVariableId,
				getObjectId(aValue),
				theThread.getId(),
				theThread.getCurrentDepth(),
				theFrame));
		
		itsCollector.localWrite(
				theThread,
				theFrame.parentTimestamp,
				theThread.getCurrentDepth(),
				theTimestamp,
				aOperationBytecodeIndex,
				aVariableId,
				aValue);
	}
	
	public void logBeforeBehaviorCall(
			int aOperationBytecodeIndex, 
			int aBehaviorId,
			BehaviorCallType aCallType)
	{
		if (DISABLE_INTERPRETER) return;
		T theThread = getThreadData();

		FrameInfo theFrame = theThread.currentFrame();
		assert theFrame.directParent && theFrame.behavior > 0;
		
		if (EVENT_INTERPRETER_LOG) System.out.println(String.format(
				"logBeforeBehaviorCall(%d, %d, %s)\n thread: %d, depth: %d\n frame: %s",
				aOperationBytecodeIndex,
				aBehaviorId,
				aCallType,
				theThread.getId(),
				theThread.getCurrentDepth(),
				theFrame));

		theThread.pushFrame(true, aBehaviorId, true, theFrame.parentTimestamp, aCallType, aOperationBytecodeIndex);
	}
	
	public void logBeforeBehaviorCall(
			int aOperationBytecodeIndex,
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
		
		if (EVENT_INTERPRETER_LOG) System.out.println(String.format(
				"logBeforeBehaviorCall(%d, %d, %d, %s, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aOperationBytecodeIndex,
				aBehaviorId,
				aCallType,
				getObjectId(aTarget),
				aArguments,
				theThread.getId(),
				theThread.getCurrentDepth(),
				theFrame));
		
		aCallType.call(
				itsCollector,
				theThread,
				theFrame.parentTimestamp,
				theThread.getCurrentDepth(),
				theTimestamp,
				aOperationBytecodeIndex,
				false,
				aBehaviorId,
				0,
				aTarget,
				aArguments);
		
		theThread.pushFrame(false, aBehaviorId, false, theTimestamp, null, -1);
	}

	public void logAfterBehaviorCall()
	{
		if (DISABLE_INTERPRETER) return;
		// Nothing to do here, maybe we don't need this message
		T theThread = getThreadData();
		FrameInfo theFrame = theThread.currentFrame();
		
		if (EVENT_INTERPRETER_LOG) System.out.println(String.format(
				"logAfterBehaviorCall()\n thread: %d, depth: %d\n frame: %s",
				theThread.getId(),
				theThread.getCurrentDepth(),
				theFrame));
	}
	
	public void logAfterBehaviorCall(
			int aOperationBytecodeIndex, 
			int aBehaviorId, 
			Object aTarget,
			Object aResult)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);

		FrameInfo theFrame = theThread.popFrame();
		assert theFrame.behavior == aBehaviorId;
		
		if (EVENT_INTERPRETER_LOG) System.out.println(String.format(
				"logAfterBehaviorCall(%d, %d, %d, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aOperationBytecodeIndex,
				aBehaviorId,
				getObjectId(aTarget),
				getObjectId(aResult),
				theThread.getId(),
				theThread.getCurrentDepth(),
				theFrame));
		
		
		itsCollector.behaviorExit(
				theThread,
				theFrame.parentTimestamp,
				theThread.getCurrentDepth(),
				theTimestamp,
				aOperationBytecodeIndex,
				aBehaviorId,
				false,
				aResult);
	}
	
	public void logAfterBehaviorCallWithException(
			int aOperationBytecodeIndex,
			int aBehaviorId, 
			Object aTarget, 
			Object aException)
	{
		if (DISABLE_INTERPRETER) return;
		long theTimestamp = AgentUtils.timestamp();
		T theThread = getThreadData();
		theTimestamp = theThread.transformTimestamp(theTimestamp);

		FrameInfo theFrame = theThread.popFrame();
		assert theFrame.behavior == aBehaviorId;
		
		if (EVENT_INTERPRETER_LOG) System.out.println(String.format(
				"logAfterBehaviorCallWithException(%d, %d, %d, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aOperationBytecodeIndex,
				aBehaviorId,
				getObjectId(aTarget),
				aException,
				theThread.getId(),
				theThread.getCurrentDepth(),
				theFrame));

		itsCollector.behaviorExit(
				theThread,
				theFrame.parentTimestamp,
				theThread.getCurrentDepth(),
				theTimestamp,
				aOperationBytecodeIndex,
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
		
		if (EVENT_INTERPRETER_LOG) System.out.println(String.format(
				"logOutput(%d, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				theTimestamp,
				aOutput,
				aData,
				theThread.getId(),
				theThread.getCurrentDepth(),
				theFrame));

		itsCollector.output(
				theThread,
				theFrame.parentTimestamp,
				theThread.getCurrentDepth(),
				theTimestamp,
				aOutput,
				aData);
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
				int aBytecodeIndex)
		{
			assert aCallType != null;
			FrameInfo theFrame = getFreeFrame();
			theFrame.entering = aEntering;
			theFrame.behavior = aBehavior;
			theFrame.directParent = aDirectParent;
			theFrame.parentTimestamp = aParentTimestamp;
			theFrame.callType = aCallType;
			theFrame.bytecodeIndex = aBytecodeIndex;
			
			itsStack[itsStackSize++] = theFrame;
			
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
		
		public short getCurrentDepth()
		{
			return (short) itsStackSize;
		}
		
		private byte getNextSerial(long aTimestamp)
		{
			if (aTimestamp < itsLastTimestamp) 
			{
				System.err.println(String.format(
						"EventInterpreter.getNextSerial: Out of order on single thread, BUG! (current: %s, previous: %s) tid: %02d",
						AgentUtils.formatTimestamp(aTimestamp),
						AgentUtils.formatTimestamp(itsLastTimestamp),
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
		public int bytecodeIndex;
		
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

}
