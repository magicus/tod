/*
 * Created on Oct 25, 2004
 */
package reflex.lib.logging.miner.impl.common;

import tod.core.ILogCollector;
import tod.core.LocationRegistrer;
import tod.core.Output;
import tod.core.model.structure.ThreadInfo;

/**
 * A base class that helps the implementation of collectors for the miner.
 * In particular, it separates the event logging into several methods, and handles
 * the depth and serial number of events.
 * @author gpothier
 */
public abstract class AbstractMinerCollector extends LocationRegistrer implements ILogCollector
{

    public void logFieldWrite(
    		long aTimestamp, 
            long aThreadId, 
            int aOperationBytecodeIndex, 
            int aFieldLocationId, 
            Object aTarget,
            Object aValue)
    {
		MyThreadInfo theThread = (MyThreadInfo) getThread(aThreadId);
		int theDepth = theThread.getDepth();

		logFieldWrite (
				aTimestamp, 
				theThread,
                aOperationBytecodeIndex,
				theThread.getSerial(),
				theDepth,
                aFieldLocationId, 
				aTarget,
				aValue);		
	}
	
    public void logLocalVariableWrite(
    		long aTimestamp, 
            long aThreadId,
            int aOperationBytecodeIndex, 
            int aVariableId,
            Object aTarget, 
            Object aValue)
    {
		MyThreadInfo theThread = (MyThreadInfo) getThread(aThreadId);
		int theDepth = theThread.getDepth();
		
		logLocalVariableWrite (
				aTimestamp, 
				theThread,
                aOperationBytecodeIndex,
				theThread.getSerial(),
				theDepth,
                aVariableId, 
				aTarget,
				aValue);		
	}
	
    public void logInstantiation(
    		long aTimestamp, 
            long aThreadId, 
            int aOperationBytecodeIndex,
            int aTypeLocationId,
            Object aInstance)
    {
		MyThreadInfo theThread = (MyThreadInfo) getThread(aThreadId);
		int theDepth = theThread.getDepth();
		
		logInstantiation (
				aTimestamp, 
				theThread,
                aOperationBytecodeIndex,
				theThread.getSerial(), 
				theDepth,
                aTypeLocationId,
				aInstance);
	}
	
    public void logBeforeMethodCall(
    		long aTimestamp, 
            long aThreadId,
            int aOperationBytecodeIndex,
            int aMethodLocationId, 
            Object aTarget, 
            Object[] aArguments)
    {
		MyThreadInfo theThread = (MyThreadInfo) getThread(aThreadId);
		int theDepth = theThread.getDepth();
		
		logBeforeMethodCall (
				aTimestamp,
				theThread, 
                aOperationBytecodeIndex,
				theThread.getSerial(),
				theDepth,
                aMethodLocationId, 
				aTarget,
				aArguments);
	}
	
    public void logAfterMethodCall(
    		long aTimestamp, 
            long aThreadId, 
            int aOperationBytecodeIndex, 
            int aMethodLocationId, 
            Object aTarget, 
            Object aResult)
    {
		MyThreadInfo theThread = (MyThreadInfo) getThread(aThreadId);
		int theDepth = theThread.getDepth();
		
		logAfterMethodCall (
				aTimestamp,
				theThread, 
                aOperationBytecodeIndex,
				theThread.getSerial(),
				theDepth, 
                aMethodLocationId,
				aTarget,
				aResult);
	}
	
	
	public void logBehaviorEnter(
			long aTimestamp, 
			long aThreadId, 
			int aLocationId)
	{
		MyThreadInfo theThread = (MyThreadInfo) getThread(aThreadId);
		int theDepth = theThread.getDepth();

		theThread.enter();
		logBehaviorEnter (
				aTimestamp, 
				theThread,
				theThread.getSerial(),
				theDepth, 
				aLocationId);
	}
	
	
	public void logBehaviorExit(
			long aTimestamp, 
			long aThreadId, 
			int aLocationId)
	{
		MyThreadInfo theThread = (MyThreadInfo) getThread(aThreadId);
		int theDepth = theThread.getDepth();

		theThread.exit();
		logBehaviorExit (
				aTimestamp,
				theThread, 
				theThread.getSerial(),
				theDepth,
				aLocationId);		
	}
	
	
	public final void logOutput(
			long aTimestamp, 
			long aThreadId, 
			Output aOutput, 
			byte[] aData)
	{
		MyThreadInfo theThread = (MyThreadInfo) getThread(aThreadId);
		int theDepth = theThread.getDepth();

		logOutput (
				aTimestamp, 
				theThread,
				theThread.getSerial(), 
				theDepth, 
				aOutput, 
				aData);
	}


	protected abstract void logFieldWrite(
			long aTimestamp, 
			ThreadInfo aThreadInfo, 
            int aOperationBytecodeIndex, 
			long aSerial, 
			int aDepth, 
			int aFieldId, 
			Object aTarget, 
			Object aValue);
	
	protected abstract void logLocalVariableWrite(
			long aTimestamp, 
			ThreadInfo aThreadInfo, 
            int aOperationBytecodeIndex, 
			long aSerial, 
			int aDepth, 
			int aVariableId, 
			Object aTarget, 
			Object aValue);
	
	protected abstract void logInstantiation(
			long aTimestamp, 
			ThreadInfo aThreadInfo, 
            int aOperationBytecodeIndex, 
			long aSerial, 
			int aDepth, 
			int aTypeId, 
			Object aInstance);
	
	protected abstract void logBeforeMethodCall(
			long aTimestamp,
			ThreadInfo aThreadInfo,
            int aOperationBytecodeIndex, 
			long aSerial,
			int aDepth,
			int aTypeId,
			Object aInstance,
			Object[] aArguments);
	
	protected abstract void logAfterMethodCall(
			long aTimestamp,
			ThreadInfo aThreadInfo,
            int aOperationBytecodeIndex, 
			long aSerial,
			int aDepth, 
			int aTypeId, 
			Object aInstance,
			Object aResult);
	
	protected abstract void logBehaviorEnter(
			long aTimestamp,
			ThreadInfo aThreadInfo, 
			long aSerial,
			int aDepth, 
			int aBehaviourId);
	
	protected abstract void logBehaviorExit(
			long aTimestamp, 
			ThreadInfo aThreadInfo, 
			long aSerial,
			int aDepth, 
			int aBehaviourId);
	
	protected abstract void logOutput(
			long aTimestamp, 
			ThreadInfo aThreadInfo, 
			long aSerial,
			int aDepth,
			Output aOutput,
			byte[] aData);

	public final LocationRegistrer getLocationRegistrer()
	{
		return this;
	}


	
	/**
	 * We override the factory so as to create our extended thread info.
	 */
	protected ThreadInfo createThreadInfo(long aId, String aName)
	{
		return new MyThreadInfo (aId, aName);
	}
	

	/**
	 * We extend the thread info so that we can keep track of current call
	 * depth.
	 * @author gpothier
	 */
	private static class MyThreadInfo extends ThreadInfo
	{
		private int itsDepth;
		
		/**
		 * Serial number of the last event of this thread;
		 */
		private long itsSerial;
		
		public MyThreadInfo(long aId, String aName)
		{
			super(aId, aName);
		}
		
		public void enter ()
		{
			itsDepth++;
		}
		
		public void exit()
		{
			itsDepth--;
		}
		
		public int getDepth()
		{
			return itsDepth;
		}
		
		public long getSerial ()
		{
			return itsSerial++;
		}
	}
	
}
