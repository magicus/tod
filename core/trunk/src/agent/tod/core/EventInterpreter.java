/*
 * Created on Sep 7, 2006
 */
package tod.core;

import java.util.ArrayList;
import java.util.List;

import tod.agent.AgentUtils;

/**
 * Interprets low-level events sent by the instrumentation code and
 * transforms them into higher-level events.
 * @author gpothier
 */
public class EventInterpreter<T extends EventInterpreter.ThreadData>
{
	private static final boolean LOG = false;
	
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

	
	public void logBehaviorEnter(
			long aTimestamp, 
			int aBehaviorId, 
			BehaviorCallType aCallType,
			Object aObject, 
			Object[] aArguments)
	{
		T theThread = getThreadData();
		long theTimestamp = theThread.transformTimestamp(aTimestamp);
		
		FrameInfo theFrame = theThread.currentFrame();
		
		if (LOG) System.out.println(String.format(
				"logBehaviorEnter(%d, %d, %s, %s, %s)\n depth: %d\n frame: %s",
				aTimestamp,
				aBehaviorId,
				aCallType,
				aObject,
				aArguments,
				theThread.getCurrentDepth(),
				theFrame));
		
		if (theFrame.directParent)
		{
			// We come from instrumented code, ie. before/enter scheme
			// Part of the event info is available in the frame, but the
			// event has not been sent
			theFrame.callType.call(
					itsCollector,
					theThread,
					theFrame.parentTimestamp,
					theThread.getCurrentDepth(),
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
			long aTimestamp,
			int aOperationBytecodeIndex, 
			int aBehaviorId,
			Object aResult)
	{
		T theThread = getThreadData();
		long theTimestamp = theThread.transformTimestamp(aTimestamp);
		
		FrameInfo theFrame = theThread.popFrame();
		assert theFrame.behavior == aBehaviorId;
		assert theFrame.directParent;
		
		if (LOG) System.out.println(String.format(
				"logBehaviorExit(%d, %d, %d, %s)\n depth: %d\n frame: %s",
				aTimestamp,
				aOperationBytecodeIndex,
				aBehaviorId,
				aResult,
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
			long aTimestamp, 
			int aBehaviorId, 
			Object aException)
	{
		((Throwable) aException).printStackTrace();
		if (LOG) System.out.println(String.format(
				"logBehaviorExitWithException(%d, %d, %s)",
				aTimestamp,
				aBehaviorId,
				aException));

		T theThread = getThreadData();
		long theTimestamp = theThread.transformTimestamp(aTimestamp);
		
		FrameInfo theFrame = theThread.popFrame();
		assert theFrame.behavior == aBehaviorId;
		assert theFrame.directParent;
		
		if (LOG) System.out.println(String.format(
				" depth: %d\n frame: %s",
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
			long aTimestamp, 
			String aMethodName,
			String aMethodSignature,
			String aMethodDeclaringClassSignature, 
			int aOperationBytecodeIndex,
			Object aException)
	{
		T theThread = getThreadData();
		long theTimestamp = theThread.transformTimestamp(aTimestamp);

		FrameInfo theFrame = theThread.currentFrame();

		if (LOG) System.out.println(String.format(
				"logExceptionGenerated(%d, %%s, %s, %s, %d, %s)\n depth: %d\n frame: %s",
				aTimestamp,
				aMethodName,
				aMethodSignature,
				aMethodDeclaringClassSignature,
				aOperationBytecodeIndex,
				aException,
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
			long aTimestamp,
			int aOperationBytecodeIndex, 
			int aFieldId,
			Object aTarget, 
			Object aValue)
	{
		T theThread = getThreadData();
		long theTimestamp = theThread.transformTimestamp(aTimestamp);

		FrameInfo theFrame = theThread.currentFrame();
		
		if (LOG) System.out.println(String.format(
				"logFieldWrite(%d, %d, %d, %s, %s)\n depth: %d\n frame: %s",
				aTimestamp,
				aOperationBytecodeIndex,
				aFieldId,
				aTarget,
				aValue,
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
	
	public void logLocalVariableWrite(
			long aTimestamp, 
			int aOperationBytecodeIndex,
			int aVariableId,
			Object aValue)
	{
		T theThread = getThreadData();
		long theTimestamp = theThread.transformTimestamp(aTimestamp);

		FrameInfo theFrame = theThread.currentFrame();
		
		if (LOG) System.out.println(String.format(
				"logLocalVariableWrite(%d, %d, %d, %s)\n depth: %d\n frame: %s",
				aTimestamp,
				aOperationBytecodeIndex,
				aVariableId,
				aValue,
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
		T theThread = getThreadData();

		FrameInfo theFrame = theThread.currentFrame();
		assert theFrame.directParent && theFrame.behavior > 0;
		
		if (LOG) System.out.println(String.format(
				"logBeforeBehaviorCall(%d, %d, %s)\n depth: %d\n frame: %s",
				aOperationBytecodeIndex,
				aBehaviorId,
				aCallType,
				theThread.getCurrentDepth(),
				theFrame));

		theThread.pushFrame(true, aBehaviorId, false, theFrame.parentTimestamp, aCallType, aOperationBytecodeIndex);
	}
	
	public void logBeforeBehaviorCall(
			long aTimestamp, 
			int aOperationBytecodeIndex,
			int aBehaviorId,
			BehaviorCallType aCallType,
			Object aTarget, 
			Object[] aArguments)
	{
		T theThread = getThreadData();
		long theTimestamp = theThread.transformTimestamp(aTimestamp);

		FrameInfo theFrame = theThread.currentFrame();
		
		if (LOG) System.out.println(String.format(
				"logBeforeBehaviorCall(%d, %d, %d, %s, %s, %s)\n depth: %d\n frame: %s",
				aTimestamp,
				aOperationBytecodeIndex,
				aBehaviorId,
				aCallType,
				aTarget,
				aArguments,
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
		// Nothing to do here, maybe we don't need this message
		T theThread = getThreadData();
		FrameInfo theFrame = theThread.popFrame();
		
		if (LOG) System.out.println(String.format(
				"logAfterBehaviorCall()\n depth: %d\n frame: %s",
				theThread.getCurrentDepth(),
				theFrame));
	}
	
	public void logAfterBehaviorCall(
			long aTimestamp, 
			int aOperationBytecodeIndex, 
			int aBehaviorId, 
			Object aTarget,
			Object aResult)
	{
		T theThread = getThreadData();
		long theTimestamp = theThread.transformTimestamp(aTimestamp);

		FrameInfo theFrame = theThread.popFrame();
		assert theFrame.behavior == aBehaviorId;
		
		if (LOG) System.out.println(String.format(
				"logAfterBehaviorCall(%d, %d, %d, %s, %s)\n depth: %d\n frame: %s",
				aTimestamp,
				aOperationBytecodeIndex,
				aBehaviorId,
				aTarget,
				aResult,
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
			long aTimestamp, 
			int aOperationBytecodeIndex,
			int aBehaviorId, 
			Object aTarget, 
			Object aException)
	{
		T theThread = getThreadData();
		long theTimestamp = theThread.transformTimestamp(aTimestamp);

		FrameInfo theFrame = theThread.currentFrame();
		assert theFrame.behavior == aBehaviorId;
		
		if (LOG) System.out.println(String.format(
				"logAfterBehaviorCallWithException(%d, %d, %d, %s, %s)\n depth: %d\n frame: %s",
				aTimestamp,
				aOperationBytecodeIndex,
				aBehaviorId,
				aTarget,
				aException,
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
				aException);
	}
	
	public void logOutput(
			long aTimestamp, 
			Output aOutput, 
			byte[] aData)
	{
		T theThread = getThreadData();
		long theTimestamp = theThread.transformTimestamp(aTimestamp);

		FrameInfo theFrame = theThread.currentFrame();
		
		if (LOG) System.out.println(String.format(
				"logOutput(%d, %%s, %s)\n depth: %d\n frame: %s",
				aTimestamp,
				aOutput,
				aData,
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
		 * use the whole 64 range. Internal ids are sequential.
		 */
		private int itsId;
		
		private List<FrameInfo> itsStack = new ArrayList<FrameInfo>();
		private List<FrameInfo> itsFreeFrames = new ArrayList<FrameInfo>();
		
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
			int theSize = itsFreeFrames.size();
			if (theSize == 0) return new FrameInfo();
			else return itsFreeFrames.remove(theSize-1);
		}
		
		public FrameInfo pushFrame(
				boolean aEntering,
				int aBehavior, 
				boolean aDirectParent,
				long aParentTimestamp,
				BehaviorCallType aCallType, 
				int aBytecodeIndex)
		{
			FrameInfo theFrame = getFreeFrame();
			theFrame.entering = aEntering;
			theFrame.behavior = aBehavior;
			theFrame.directParent = aDirectParent;
			theFrame.parentTimestamp = aParentTimestamp;
			theFrame.callType = aCallType;
			theFrame.bytecodeIndex = aBytecodeIndex;
			
			itsStack.add(theFrame);
			
			return theFrame;
		}
		
		public FrameInfo popFrame()
		{
			FrameInfo theFrame = itsStack.remove(itsStack.size()-1);
			itsFreeFrames.add(theFrame);
			return theFrame;
		}
		
		public FrameInfo currentFrame()
		{
			return itsStack.get(itsStack.size()-1);
		}
		
		public short getCurrentDepth()
		{
			return (short) itsStack.size();
		}
		
		private byte getNextSerial(long aTimestamp)
		{
			if (aTimestamp == itsLastTimestamp) return ++itsSerial;
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