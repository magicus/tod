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
package tod.core.transport;

import static tod.agent.AgentDebugFlags.COLLECTOR_LOG;
import static tod.agent.AgentDebugFlags.DISABLE_COLLECTOR;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import tod.agent.AgentDebugFlags;
import tod.agent.AgentUtils;
import tod.agent.BehaviorCallType;
import tod.agent.ObjectIdentity;
import tod.agent.Output;
import tod.core.ILogCollector;
import zz.utils.Utils;


/**
 * Interprets low-level events sent by the instrumentation code and
 * transforms them into higher-level events.
 * @author gpothier
 */
public final class EventInterpreter implements ILowLevelCollector
{
	static
	{
//		System.out.println("EventInterpreter loaded.");
	}
	
	private static PrintStream itsPrintStream = AgentDebugFlags.EVENT_INTERPRETER_PRINT_STREAM;
	
	private List<ThreadData> itsThreads = new ArrayList<ThreadData>();
	
	private ILogCollector itsCollector;
	
	public EventInterpreter(ILogCollector aCollector)
	{
		itsCollector = aCollector;
	}
	
	private ThreadData getThreadData(int aThreadId)
	{
		return Utils.listGet(itsThreads, aThreadId);
	}
	
	private String getObjectId(Object aObject)
	{
		return ""+aObject;
	}

	private void call(
			BehaviorCallType aCallType,
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aProbeId,
			boolean aDirectParent,
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget,
			Object[] aArguments)
	{
		switch(aCallType)
		{
		case INSTANTIATION:
			itsCollector.instantiation(aThreadId, aParentTimestamp, aDepth, aTimestamp, aProbeId, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
			break;
			
		case METHOD_CALL:
			itsCollector.methodCall(aThreadId, aParentTimestamp, aDepth, aTimestamp, aProbeId, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
			break;
			
		case SUPER_CALL:
			itsCollector.superCall(aThreadId, aParentTimestamp, aDepth, aTimestamp, aProbeId, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
			break;
			
		default: throw new RuntimeException("Not handled: "+aCallType);
		}
	}

	
	public void registerThread(int aThreadId, long aJVMThreadId, String aName)
	{
		Utils.listSet(itsThreads, aThreadId, new ThreadData(aThreadId));
		itsCollector.thread(aThreadId, aJVMThreadId, aName);
	}

	public void logClInitEnter(
			int aThreadId, 
			long aTimestamp, 
			int aBehaviorId, 
			BehaviorCallType aCallType)
	{
		logBehaviorEnter(aThreadId, aTimestamp, true, aBehaviorId, aCallType, null, null);
	}
	
	public void logBehaviorEnter(
			int aThreadId, 
			long aTimestamp, 
			int aBehaviorId, 
			BehaviorCallType aCallType,
			Object aObject, 
			Object[] aArguments)
	{
		logBehaviorEnter(aThreadId, aTimestamp, false, aBehaviorId, aCallType, aObject, aArguments);
	}
	
	private void logBehaviorEnter(
			int aThreadId, 
			long aTimestamp, 
			boolean aClInit,
			int aBehaviorId, 
			BehaviorCallType aCallType,
			Object aObject, 
			Object[] aArguments)
	{
		if (DISABLE_COLLECTOR) return;
		ThreadData theThread = getThreadData(aThreadId);
		
		FrameInfo theFrame = theThread.currentFrame();
		
		if (theFrame.entering && ! aClInit)
		{
			// We come from instrumented code, ie. before/enter scheme
			// Part of the event info is available in the frame, but the
			// event has not been sent yet.
			
			short theDepth = (short) (theThread.getCurrentDepth()-1);
			
			if (COLLECTOR_LOG) print(theDepth, String.format(
					"logBehaviorEnter(%d, %d, %s, %s, %s)\n thread: %d, depth: %d\n frame: %s",
					aTimestamp,
					aBehaviorId,
					aCallType,
					getObjectId(aObject),
					Arrays.asList(aArguments),
					theThread.getId(),
					theDepth,
					theFrame));


			// Partial update of frame info. We must do it here to indicate
			// that we successfully entered the method, thus completing the
			// before/enter scheme. This is necessary for exception event
			// handling
			theFrame.entering = false;
			
			call(
					theFrame.callType,
					theThread.getId(),
					theFrame.parentTimestamp,
					theDepth,
					aTimestamp,
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
			theFrame.parentTimestamp = aTimestamp;
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
			
			if (COLLECTOR_LOG) print(theDepth, String.format(
					"logBehaviorEnter(%d, %d, %s, %s, %s)\n thread: %d, depth: %d\n frame: %s",
					aTimestamp,
					aBehaviorId,
					aCallType,
					getObjectId(aObject),
					aArguments,
					theThread.getId(),
					theDepth,
					theFrame));
			
			
			call(
					aCallType,
					theThread.getId(),
					theFrame.parentTimestamp,
					theDepth,
					aTimestamp,
					theProbeId,
					true,
					0,
					aBehaviorId,
					aObject,
					aArguments);
			
			theThread.pushFrame(false, aBehaviorId, true, aTimestamp, null, -1);
		}
	}

	public void logClInitExit(
			int aThreadId, 
			long aTimestamp, 
			int aProbeId, 
			int aBehaviorId)
	{
		logBehaviorExit(aThreadId, aTimestamp, true, aProbeId, aBehaviorId, null);
	}
	
	public void logBehaviorExit(
			int aThreadId, 
			long aTimestamp, 
			int aProbeId, 
			int aBehaviorId,
			Object aResult)
	{
		logBehaviorExit(aThreadId, aTimestamp, false, aProbeId, aBehaviorId, aResult);		
	}
	
	private void logBehaviorExit(
			int aThreadId, 
			long aTimestamp, 
			boolean aClInit,
			int aProbeId, 
			int aBehaviorId,
			Object aResult)
	{
		if (DISABLE_COLLECTOR) return;
		ThreadData theThread = getThreadData(aThreadId);

		FrameInfo theFrame = theThread.popFrame();
		short theDepth = (short) (theThread.getCurrentDepth()+1);
		assert theFrame.behavior == aBehaviorId;
		assert theFrame.directParent;
		
		if (aClInit && theThread.currentFrame().entering)
		{
			theThread.decDepthAdjust();
		}
		
		if (COLLECTOR_LOG) print(theDepth, String.format(
				"logBehaviorExit(%d, %d, %d, %s)\n thread: %d, depth: %d\n frame: %s",
				aTimestamp,
				aProbeId,
				aBehaviorId,
				getObjectId(aResult),
				theThread.getId(),
				theDepth,
				theFrame));
		
		itsCollector.behaviorExit(
				theThread.getId(),
				theFrame.parentTimestamp,
				theDepth,
				aTimestamp,
				aProbeId,
				aBehaviorId,
				false,
				aResult);
	}
	
	public void logBehaviorExitWithException(
			int aThreadId, 
			long aTimestamp, 
			int aBehaviorId, 
			Object aException)
	{
		if (DISABLE_COLLECTOR) return;
		
		ThreadData theThread = getThreadData(aThreadId);
		
		if (COLLECTOR_LOG)
		{
			System.err.println("Exit with exception:");
			((Throwable) aException).printStackTrace();
			
			itsPrintStream.println(String.format(
				"logBehaviorExitWithException(%d, %d, %s)",
				aTimestamp,
				aBehaviorId,
				aException));
		}


		
		FrameInfo theFrame = theThread.popFrame();
		assert theFrame.behavior == aBehaviorId;
		assert theFrame.directParent;
		
		if (COLLECTOR_LOG) itsPrintStream.println(String.format(
				" thread: %d, depth: %d\n frame: %s",
				theThread.getId(),
				theThread.getCurrentDepth(),
				theFrame));
		

		itsCollector.behaviorExit(
				theThread.getId(),
				theFrame.parentTimestamp,
				(short) (theThread.getCurrentDepth()+1), // The exit event is at the same depths as other children
				aTimestamp,
				-1,
				aBehaviorId,
				true,
				aException);
	}
	
	public void logExceptionGenerated(
			int aThreadId, 
			long aTimestamp, 
			String aMethodName,
			String aMethodSignature,
			String aMethodDeclaringClassSignature, 
			int aOperationBytecodeIndex,
			Object aException)
	{
		if (DISABLE_COLLECTOR) return;
		ThreadData theThread = getThreadData(aThreadId);
		
		FrameInfo theFrame = theThread.currentFrame();
		short theDepth = theThread.getCurrentDepth();

		if (COLLECTOR_LOG) print(theDepth, String.format(
				"logExceptionGenerated(%d, %s, %s, %s, %d, %s)\n thread: %d, depth: %d\n frame: %s",
				aTimestamp,
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
				theThread.getId(),
				theFrame.parentTimestamp,
				theDepth,
				aTimestamp,
				aMethodName,
				aMethodSignature,
				aMethodDeclaringClassSignature,
				aOperationBytecodeIndex,
				aException);
	}
	
	public void logFieldWrite(
			int aThreadId, 
			long aTimestamp, 
			int aProbeId, 
			int aFieldId,
			Object aTarget, 
			Object aValue)
	{
		if (DISABLE_COLLECTOR) return;
		ThreadData theThread = getThreadData(aThreadId);

		FrameInfo theFrame = theThread.currentFrame();
		short theDepth = theThread.getCurrentDepth();
		
		if (COLLECTOR_LOG) print(theDepth, String.format(
				"logFieldWrite(%d, %d, %d, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				aTimestamp,
				aProbeId,
				aFieldId,
				getObjectId(aTarget),
				getObjectId(aValue),
				theThread.getId(),
				theDepth,
				theFrame));

		itsCollector.fieldWrite(
				theThread.getId(),
				theFrame.parentTimestamp,
				theDepth,
				aTimestamp,
				aProbeId,
				aFieldId,
				aTarget,
				aValue);
	}
	
	public void logNewArray(
			int aThreadId, 
			long aTimestamp, 
			int aProbeId, 
			Object aTarget,
			int aBaseTypeId,
			int aSize)
	{
		if (DISABLE_COLLECTOR) return;
		ThreadData theThread = getThreadData(aThreadId);
		
		FrameInfo theFrame = theThread.currentFrame();
		
		short theDepth = theThread.getCurrentDepth();
		if (COLLECTOR_LOG) print(theDepth, String.format(
				"logNewArray(%d, %d, %s, %d, %d)\n thread: %d, depth: %d\n frame: %s",
				aTimestamp,
				aProbeId,
				getObjectId(aTarget),
				aBaseTypeId,
				aSize,
				theThread.getId(),
				theDepth,
				theFrame));
		
		itsCollector.newArray(
				theThread.getId(),
				theFrame.parentTimestamp,
				theDepth,
				aTimestamp,
				aProbeId,
				aTarget,
				aBaseTypeId,
				aSize);
	}
	

	public void logArrayWrite(
			int aThreadId, 
			long aTimestamp, 
			int aProbeId, 
			Object aTarget,
			int aIndex,
			Object aValue)
	{
		if (DISABLE_COLLECTOR) return;
		ThreadData theThread = getThreadData(aThreadId);
		
		FrameInfo theFrame = theThread.currentFrame();
		
		short theDepth = theThread.getCurrentDepth();
		if (COLLECTOR_LOG) print(theDepth, String.format(
				"logArrayWrite(%d, %d, %s, %d, %s)\n thread: %d, depth: %d\n frame: %s",
				aTimestamp,
				aProbeId,
				getObjectId(aTarget),
				aIndex,
				getObjectId(aValue),
				theThread.getId(),
				theDepth,
				theFrame));
		
		itsCollector.arrayWrite(
				theThread.getId(),
				theFrame.parentTimestamp,
				theDepth,
				aTimestamp,
				aProbeId,
				aTarget,
				aIndex,
				aValue);
	}
	
	public void logLocalVariableWrite(
			int aThreadId, 
			long aTimestamp, 
			int aProbeId, 
			int aVariableId,
			Object aValue)
	{
		if (DISABLE_COLLECTOR) return;
		ThreadData theThread = getThreadData(aThreadId);

		FrameInfo theFrame = theThread.currentFrame();
		short theDepth = theThread.getCurrentDepth();
		
		if (COLLECTOR_LOG) print(theDepth, String.format(
				"logLocalVariableWrite(%d, %d, %d, %s)\n thread: %d, depth: %d\n frame: %s",
				aTimestamp,
				aProbeId,
				aVariableId,
				getObjectId(aValue),
				theThread.getId(),
				theDepth,
				theFrame));
		
		itsCollector.localWrite(
				theThread.getId(),
				theFrame.parentTimestamp,
				theDepth,
				aTimestamp,
				aProbeId,
				aVariableId,
				aValue);
	}
	
	/**
	 * Dry version
	 */
	public void logBeforeBehaviorCallDry(
			int aThreadId, 
			long aTimestamp, 
			int aProbeId, 
			int aBehaviorId,
			BehaviorCallType aCallType)
	{
		if (DISABLE_COLLECTOR) return;
		assert aCallType != null;
		ThreadData theThread = getThreadData(aThreadId);

		FrameInfo theFrame = theThread.currentFrame();
		assert theFrame.directParent && theFrame.behavior > 0;
		
		if (COLLECTOR_LOG)
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
			int aThreadId, 
			long aTimestamp, 
			int aProbeId, 
			int aBehaviorId,
			BehaviorCallType aCallType,
			Object aTarget, 
			Object[] aArguments)
	{
		if (DISABLE_COLLECTOR) return;
		ThreadData theThread = getThreadData(aThreadId);

		FrameInfo theFrame = theThread.currentFrame();
		short theDepth = theThread.getCurrentDepth();
		
		if (COLLECTOR_LOG) print(theDepth, String.format(
				"logBeforeBehaviorCall(%d, %d, %d, %s, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				aTimestamp,
				aProbeId,
				aBehaviorId,
				aCallType,
				getObjectId(aTarget),
				Arrays.asList(aArguments),
				theThread.getId(),
				theDepth,
				theFrame));
		
		call(
				aCallType,
				theThread.getId(),
				theFrame.parentTimestamp,
				theDepth,
				aTimestamp,
				aProbeId,
				false,
				aBehaviorId,
				0,
				aTarget,
				aArguments);
		
		theThread.pushFrame(false, aBehaviorId, false, aTimestamp, null, -1);
	}

	/**
	 * Dry version
	 */
	public void logAfterBehaviorCallDry(
			int aThreadId, 
			long aTimestamp)
	{
		if (DISABLE_COLLECTOR) return;
		// Nothing to do here, maybe we don't need this message
		ThreadData theThread = getThreadData(aThreadId);
		FrameInfo theFrame = theThread.currentFrame();
		short theDepth = theThread.getCurrentDepth();
		
		if (COLLECTOR_LOG)
		{
			print(theDepth, String.format(
					"logAfterBehaviorCallDry()\n thread: %d, depth: %d\n frame: %s",
					theThread.getId(),
					theDepth,
					theFrame));
		}
		
		if (theFrame.entering)
		{
			String theMessage = String.format(
					"[EventInterpreter] Unexpected dry after (check trace scope) - bid: %d, thread: %d, p.ts: %d, ts: %d",
					theFrame.behavior,
					theThread.getId(),
					theFrame.parentTimestamp,
					aTimestamp);
			
			itsPrintStream.println(theMessage);
			System.err.println(theMessage);

			theThread.popFrame();
			
			// Send message anyway, although there is a lot of missing info
			String s = new String("<?TOD?-dry>");
			logBeforeBehaviorCall(aThreadId, aTimestamp, -1, theFrame.behavior, BehaviorCallType.METHOD_CALL, s, null);
			logAfterBehaviorCall(aThreadId, aTimestamp, -1, theFrame.behavior, s, s);
		}
	}
	
	public void logAfterBehaviorCall(
			int aThreadId, 
			long aTimestamp, 
			int aProbeId, 
			int aBehaviorId, 
			Object aTarget,
			Object aResult)
	{
		if (DISABLE_COLLECTOR) return;
		ThreadData theThread = getThreadData(aThreadId);

		FrameInfo theFrame = theThread.popFrame();
		short theDepth = (short) (theThread.getCurrentDepth()+1);
		assert theFrame.behavior == aBehaviorId;
		
		if (COLLECTOR_LOG) print(theDepth, String.format(
				"logAfterBehaviorCall(%d, %d, %d, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				aTimestamp,
				aProbeId,
				aBehaviorId,
				getObjectId(aTarget),
				getObjectId(aResult),
				theThread.getId(),
				theDepth,
				theFrame));
		
		itsCollector.behaviorExit(
				theThread.getId(),
				theFrame.parentTimestamp,
				theDepth,
				aTimestamp,
				aProbeId,
				aBehaviorId,
				false,
				aResult);
	}
	
	public void logAfterBehaviorCallWithException(
			int aThreadId, 
			long aTimestamp, 
			int aProbeId, 
			int aBehaviorId, 
			Object aTarget, 
			Object aException)
	{
		if (DISABLE_COLLECTOR) return;
		ThreadData theThread = getThreadData(aThreadId);

		FrameInfo theFrame = theThread.popFrame();
		short theDepth = (short) (theThread.getCurrentDepth()+1);
		assert theFrame.behavior == aBehaviorId;
		
		if (COLLECTOR_LOG) print(theDepth, String.format(
				"logAfterBehaviorCallWithException(%d, %d, %d, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				aTimestamp,
				aProbeId,
				aBehaviorId,
				getObjectId(aTarget),
				aException,
				theThread.getId(),
				theDepth,
				theFrame));

		itsCollector.behaviorExit(
				theThread.getId(),
				theFrame.parentTimestamp,
				theDepth,
				aTimestamp,
				aProbeId,
				aBehaviorId,
				true,
				aException);
	}
	
	public void logOutput(
			int aThreadId, 
			long aTimestamp, 
			Output aOutput, 
			byte[] aData)
	{
		if (DISABLE_COLLECTOR) return;
		ThreadData theThread = getThreadData(aThreadId);

		FrameInfo theFrame = theThread.currentFrame();
		short theDepth = theThread.getCurrentDepth();
		
		if (COLLECTOR_LOG) print(theDepth, String.format(
				"logOutput(%d, %s, %s)\n thread: %d, depth: %d\n frame: %s",
				aTimestamp,
				aOutput,
				aData,
				theThread.getId(),
				theDepth,
				theFrame));

		itsCollector.output(
				theThread.getId(),
				theFrame.parentTimestamp,
				theDepth,
				aTimestamp,
				aOutput,
				aData);
	}

	public static class ThreadData 
	{
		/**
		 * Internal thread id.
		 */
		private int itsId;
		
		// We don't want to use ArrayList here, it might be instrumented.
		private FrameInfo[] itsStack = new FrameInfo[10000];
		private int itsStackSize;
		
		private FrameInfo[] itsFreeFrames = new FrameInfo[10000];
		private int itsFreeFramesSize;
		
		private long itsLastTimestamp = 0;
		
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
