/*
 * Created on Jul 20, 2006
 */
package tod.impl.common;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

import tod.core.ILogCollector;
import tod.core.Output;
import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ThreadInfo;
import tod.impl.common.event.BehaviorCallEvent;
import tod.impl.common.event.BehaviorExitEvent;
import tod.impl.common.event.ConstructorChainingEvent;
import tod.impl.common.event.Event;
import tod.impl.common.event.ExceptionGeneratedEvent;
import tod.impl.common.event.FieldWriteEvent;
import tod.impl.common.event.InstantiationEvent;
import tod.impl.common.event.LocalVariableWriteEvent;
import tod.impl.common.event.MethodCallEvent;
import tod.impl.common.event.OutputEvent;

/**
 * An abstract log collector that transforms log messages to events.
 * Whenever an event is produced it is passed to the 
 * {@link #processEvent(tod.impl.common.EventCollector.DefaultThreadInfo, Event)} method.
 * If there are multiple debugged hosts there must be multiple collectors.
 * @author gpothier
 */
public abstract class EventCollector// extends LocationRegistrer 
implements ILogCollector
{
	/**
	 * The host whose events are sent to this collector.
	 */
	private IHostInfo itsHost;
	
	private ILocationsRepository itsLocationsRepository;
	
	private	Map<Long, DefaultThreadInfo> itsThreads = new HashMap<Long, DefaultThreadInfo>();
	
	public EventCollector(IHostInfo aHost, ILocationsRepository aLocationsRepository)
	{
		itsHost = aHost;
		itsLocationsRepository = aLocationsRepository;
	}

	public ILocationsRepository getLocationsRepository()
	{
		return itsLocationsRepository;
	}

	/**
	 * This method is called whenever an event is generated.
	 * Note that subclasses should take care of adding the event to
	 * its parent if they whish so.
	 */
	protected abstract void processEvent(DefaultThreadInfo aThread, Event aEvent);

	/**
	 * Returns the host associated with this collector.
	 */
	public IHostInfo getHost()
	{
		return itsHost;
	}

	public void registerThread(long aThreadId, String aName)
	{
		ThreadInfo theThreadInfo = getThread(aThreadId);
		setupThreadInfo(theThreadInfo, aName);
	}
	
	protected void setupThreadInfo (ThreadInfo aThreadInfo, String aName)
	{
		aThreadInfo.setName(aName);
	}
	
	public DefaultThreadInfo getThread (long aId)
	{
		DefaultThreadInfo theThreadInfo = itsThreads.get(aId);
		if (theThreadInfo == null)
		{
			theThreadInfo = createThreadInfo(aId);
			itsThreads.put (aId, theThreadInfo);
		}
		return theThreadInfo;
	}

	protected final DefaultThreadInfo createThreadInfo(long aId)
	{
		BehaviorCallEvent theRootEvent = new MethodCallEvent();
		DefaultThreadInfo theThreadInfo = createThreadInfo(aId, theRootEvent);
		theRootEvent.setThread(theThreadInfo);
		theRootEvent.setDirectParent(false);
		
		return theThreadInfo;
	}
	
	/**
	 * Subclasses can override this method if they whish to provide custom subclasses
	 * of {@link DefaultThreadInfo}
	 */
	protected DefaultThreadInfo createThreadInfo(long aId, BehaviorCallEvent aRootEvent)
	{
		return new DefaultThreadInfo(itsHost, aId, aRootEvent);
	}
	
	/**
	 * Returns all available threads.
	 */
	public Iterable<IThreadInfo> getThreads()
	{
		return (Iterable) itsThreads.values();
	}

	
	private void addEvent(DefaultThreadInfo aThread, Event aEvent)
	{
		assert aEvent.getTimestamp() >= 0;
		processEvent(aThread, aEvent);
	}
	
	/**
	 * Initializes common event attributes and sets up control flow relationships
	 */
	private void initEvent (
			Event aEvent, 
			long aTimestamp,
			DefaultThreadInfo aThreadInfo, 
            int aOperationBytecodeIndex)
	{
		aEvent.setTimestamp(aTimestamp);
		aEvent.setHost(itsHost);
		aEvent.setThread(aThreadInfo);
		aEvent.setOperationBytecodeIndex(aOperationBytecodeIndex);
		aEvent.setSerial(aThreadInfo.getSerial());
		
		BehaviorCallEvent theCurrentBehavior = aThreadInfo.getCurrentParent();
		
		aEvent.setParent(theCurrentBehavior);
	}
	

	public void logBehaviorEnter(
			long aTimestamp, 
			long aThreadId, 
			int aBehaviorLocationId, 
			Object aObject, 
			Object[] aArguments)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);
		IBehaviorInfo theBehavior = itsLocationsRepository.getBehavior(aBehaviorLocationId);
		assert theBehavior != null;
		BehaviorCallEvent theEvent;
		
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();
		if (theCurrentParent.isDirectParent())
		{
			// We come from instrumented code, an event already exists
			// (but it has not been processed yet).
			theEvent = theCurrentParent;
			theEvent.setTimestamp(aTimestamp);
		}
		else
		{
			// We come from non instrumented code, create a new event
			
			// TODO: there can be a problem as we cannot detect constructor 
			// chaining.
			theEvent = theBehavior.isConstructor() ?
					new InstantiationEvent()
					: new MethodCallEvent();
			
			initEvent(theEvent, aTimestamp, theThread, -1);
			
			theThread.setCurrentParent(theEvent);
			theEvent.setDirectParent(true);
		}
		
		theEvent.setExecutedBehavior(theBehavior);
		theEvent.setTarget(aObject);
		theEvent.setArguments(aArguments);
		
		addEvent(theThread, theEvent);
	}

	public void logBehaviorExit(
			long aTimestamp,
			long aThreadId,
			int aBehaviorLocationId,
			Object aResult)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);
		
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();
		assert theCurrentParent.getExecutedBehavior().getId() == aBehaviorLocationId;
		assert theCurrentParent.isDirectParent();
		
		BehaviorExitEvent theExitEvent = new BehaviorExitEvent();
		initEvent(theExitEvent, aTimestamp, theThread, aBehaviorLocationId);
		theExitEvent.setResult(aResult);
		theExitEvent.setHasThrown(false);
		
		addEvent(theThread, theExitEvent);
		
		theThread.setCurrentParent(theCurrentParent.getParent());
	}
	
	public void logBehaviorExitWithException(
			long aTimestamp, 
			long aThreadId,
			int aBehaviorLocationId, 
			Object aException)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);
		
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();
		assert theCurrentParent.getExecutedBehavior().getId() == aBehaviorLocationId;
		assert theCurrentParent.isDirectParent();
		
		BehaviorExitEvent theExitEvent = new BehaviorExitEvent();
		initEvent(theExitEvent, aTimestamp, theThread, aBehaviorLocationId);
		theExitEvent.setResult(aException);
		theExitEvent.setHasThrown(true);
		
		addEvent(theThread, theExitEvent);
		
		theThread.setCurrentParent(theCurrentParent.getParent());
	}
	
	public void logExceptionGenerated(
			long aTimestamp,
			long aThreadId,
			int aBehaviorLocationId,
			int aOperationBytecodeIndex, 
			Object aException)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);
		IBehaviorInfo theBehavior = itsLocationsRepository.getBehavior(aBehaviorLocationId);

		logExceptionGenerated(aTimestamp, theThread, theBehavior, aOperationBytecodeIndex, aException);
	}
	
	public void logExceptionGenerated(
			long aTimestamp, 
			long aThreadId, 
			String aMethodName,
			String aMethodSignature,
			String aMethodDeclaringClassSignature, 
			int aOperationBytecodeIndex,
			Object aException)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);
		
		String theClassName = Type.getType(aMethodDeclaringClassSignature).getClassName();
		IClassInfo theClass = (IClassInfo) itsLocationsRepository.getType(theClassName);
		
		if (theClass == null) return; // TODO: don't do that...
		
		ITypeInfo[] theArgumentTypes = itsLocationsRepository.getArgumentTypes(aMethodSignature);
		IBehaviorInfo theBehavior = theClass.getBehavior(aMethodName, theArgumentTypes);

		logExceptionGenerated(aTimestamp, theThread, theBehavior, aOperationBytecodeIndex, aException);
	}
	
	protected void logExceptionGenerated(
			long aTimestamp, 
			DefaultThreadInfo aThread,
			IBehaviorInfo aBehavior,
			int aOperationBytecodeIndex,
			Object aException)
	{
		BehaviorCallEvent theCurrentParent = aThread.getCurrentParent();		

		// We check if we really entered in the current parent, or
		// if some exception prevented the call from succeeding.
		boolean theFailedCall = false;
		
		IBehaviorInfo theCallingBehavior = theCurrentParent.getCallingBehavior();
		if (aBehavior != null && theCallingBehavior != null)
		{
			if (theCurrentParent.getExecutedBehavior() == null
					&& theCurrentParent.getCallingBehavior().getId() == aBehavior.getId())
				theFailedCall = true;
		}
		
		if (theFailedCall)
		{
			aThread.setCurrentParent(theCurrentParent.getParent());
		}

		ExceptionGeneratedEvent theEvent = new ExceptionGeneratedEvent();
		initEvent(theEvent, aTimestamp, aThread, aOperationBytecodeIndex);
		
		theEvent.setThrowingBehavior(aBehavior);
		theEvent.setException(aException);
		
		addEvent(aThread, theEvent);
	}
	
	public void logFieldWrite(
			long aTimestamp,
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aFieldLocationId, 
			Object aTarget, 
			Object aValue)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);
		IFieldInfo theField = itsLocationsRepository.getField(aFieldLocationId);

		FieldWriteEvent theEvent = new FieldWriteEvent();
		initEvent(theEvent, aTimestamp, theThread, aOperationBytecodeIndex);
		
		theEvent.setTarget(aTarget);
		theEvent.setField(theField);
		theEvent.setValue(aValue);
		
		addEvent(theThread, theEvent);
	}
	
	public void logLocalVariableWrite(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex,
			int aVariableId,
			Object aValue)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();
		IBehaviorInfo theBehavior = theCurrentParent.getExecutedBehavior();
		
		LocalVariableWriteEvent theEvent = new LocalVariableWriteEvent();
		initEvent(theEvent, aTimestamp, theThread, aOperationBytecodeIndex);
		
        LocalVariableInfo theInfo = null;
//        if (aVariableId >= 0)
//        {
//        	IBehaviorInfo theBehavior = aThreadInfo.getCurrentBehavior().getBehavior();
//    		theInfo = theBehavior.getLocalVariableInfo(aVariableId);
//    		if (theInfo == null) theInfo = new LocalVariableInfo((short)-1, (short)-1, "$"+aVariableId, "", (short)-1);
//        }
//        else
//        {
//        	short theIndex = (short) (-aVariableId-1);
//        	theInfo = new LocalVariableInfo((short)-1, (short)-1, "$"+aVariableId, "", theIndex);
//        }
				
       	theInfo = theBehavior.getLocalVariableInfo(aOperationBytecodeIndex+35, aVariableId); // 35 is the size of our instrumentation
       	if (theInfo == null) theInfo = new LocalVariableInfo((short)-1, (short)-1, "$"+aVariableId, "", (short)-1);
        
		theEvent.setVariable(theInfo);
		theEvent.setValue(aValue);
		
		addEvent(theThread, theEvent);
	}
	
	public void logInstantiation(long aThreadId)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);
		theThread.expectInstantiation();
	}
	
	public void logConstructorChaining(long aThreadId)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);
		theThread.expectConstructorChaining();
	}
	
	public void logBeforeBehaviorCall(
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aBehaviorLocationId)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();
		assert theCurrentParent.isDirectParent() && theCurrentParent.getExecutedBehavior() != null;
		
		IBehaviorInfo theBehavior = itsLocationsRepository.getBehavior(aBehaviorLocationId);

		BehaviorCallEvent theEvent = theThread.createCallEvent();
		
		initEvent(theEvent, -1, theThread, aOperationBytecodeIndex);
		theEvent.setDirectParent(true);
		theEvent.setCalledBehavior(theBehavior);

		// The event is not processed now but when its timestamp is set
		// (in logBehaviorEnter)
//		processEvent(theEvent);
		
		theThread.setCurrentParent(theEvent);
	}
	
	public void logBeforeBehaviorCall(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex,
			int aBehaviorLocationId,
			Object aTarget, 
			Object[] aArguments)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);

		IBehaviorInfo theBehavior = itsLocationsRepository.getBehavior(aBehaviorLocationId);

		BehaviorCallEvent theEvent = theThread.createCallEvent();
		
		initEvent(theEvent, aTimestamp, theThread, aOperationBytecodeIndex);
		theEvent.setDirectParent(false);
		theEvent.setCalledBehavior(theBehavior);
		if (theBehavior.isConstructor()) theEvent.setExecutedBehavior(theBehavior);
		theEvent.setTarget(aTarget);
		theEvent.setArguments(aArguments);
		
		// Here we do process the event because it is not a direct parent.
		addEvent(theThread, theEvent);
		
		theThread.setCurrentParent(theEvent);
	}

	public void logAfterBehaviorCall(long aThreadId)
	{
		// Nothing to do here, maybe we don't need this message
	}
	
	public void logAfterBehaviorCall(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aBehaviorLocationId, 
			Object aTarget,
			Object aResult)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);
		
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();
		assert theCurrentParent.getCalledBehavior().getId() == aBehaviorLocationId;
		
		BehaviorExitEvent theExitEvent = new BehaviorExitEvent();
		initEvent(theExitEvent, aTimestamp, theThread, aOperationBytecodeIndex);
		theExitEvent.setResult(aResult);
		theExitEvent.setHasThrown(false);
		
		addEvent(theThread, theExitEvent);
		
		theThread.setCurrentParent(theCurrentParent.getParent());
	}
	
	public void logAfterBehaviorCallWithException(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex,
			int aBehaviorLocationId, 
			Object aTarget, 
			Object aException)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);
		
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();
		assert theCurrentParent.getCalledBehavior().getId() == aBehaviorLocationId;
		
		BehaviorExitEvent theExitEvent = new BehaviorExitEvent();
		initEvent(theExitEvent, aTimestamp, theThread, aBehaviorLocationId);
		theExitEvent.setResult(aException);
		theExitEvent.setHasThrown(true);
		
		addEvent(theThread, theExitEvent);
		
		theThread.setCurrentParent(theCurrentParent.getParent());
	}
	
	public void logOutput(
			long aTimestamp, 
			long aThreadId, 
			Output aOutput, 
			byte[] aData)
	{
		DefaultThreadInfo theThread = getThread(aThreadId);
		OutputEvent theEvent = new OutputEvent();
		initEvent(theEvent, aTimestamp, theThread, -1);
		
		theEvent.setOutput(aOutput);
		theEvent.setData(new String (aData));
		
		addEvent(theThread, theEvent);
	}

	protected static class DefaultThreadInfo extends ThreadInfo
	{
		private BehaviorCallEvent itsRootEvent;
		
		private BehaviorCallEvent itsCurrentParent;
		
		private boolean itsExpectingInstantiation;
		private boolean itsExpectingConstructorChaining;
		
		/**
		 * Serial number of the last event of this thread;
		 */
		private long itsSerial;
		
		public DefaultThreadInfo(IHostInfo aHost, long aId)
		{
			super(aHost, aId);
		}

		public DefaultThreadInfo(IHostInfo aHost, long aId, BehaviorCallEvent aRootEvent)
		{
			super(aHost, aId);
			itsRootEvent = aRootEvent;
			itsCurrentParent = itsRootEvent;
		}

		public BehaviorCallEvent getRootEvent()
		{
			return itsRootEvent;
		}
		
		public BehaviorCallEvent getCurrentParent()
		{
			return itsCurrentParent;
		}

		public void setCurrentParent(BehaviorCallEvent aCurrentParent)
		{
			itsCurrentParent = aCurrentParent;
		}

		public long getSerial ()
		{
			return itsSerial++;
		}

		public void expectConstructorChaining()
		{
			itsExpectingConstructorChaining = true;
		}

		public void expectInstantiation()
		{
			itsExpectingInstantiation = true;
		}

		/**
		 * Creates a behavior call event of the right type
		 * according to the current state of instantiation/constructor chaining
		 * modifiers.
		 */
		public BehaviorCallEvent createCallEvent()
		{
			BehaviorCallEvent theEvent;
			
			if (itsExpectingConstructorChaining)
				theEvent = new ConstructorChainingEvent();
			else if (itsExpectingInstantiation)
				theEvent = new InstantiationEvent();
			else
				theEvent = new MethodCallEvent();
			
			itsExpectingConstructorChaining = false;
			itsExpectingInstantiation = false;
			
			return theEvent;
		}
	}

}
