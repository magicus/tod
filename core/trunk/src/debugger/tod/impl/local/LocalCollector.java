/*
 * Created on Oct 25, 2004
 */
package tod.impl.local;

import org.objectweb.asm.Type;

import tod.core.ILogCollector;
import tod.core.LocationRegistrer;
import tod.core.Output;
import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.structure.IBehaviorInfo;
import tod.core.model.structure.ClassInfo;
import tod.core.model.structure.IClassInfo;
import tod.core.model.structure.IFieldInfo;
import tod.core.model.structure.ITypeInfo;
import tod.core.model.structure.ObjectId;
import tod.core.model.structure.IThreadInfo;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.trace.ICFlowBrowser;
import tod.core.model.trace.ICompoundFilter;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IEventFilter;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.ILocationTrace;
import tod.core.model.trace.IObjectInspector;
import tod.core.model.trace.IVariablesInspector;
import tod.impl.local.event.BehaviorCallEvent;
import tod.impl.local.event.ConstructorChainingEvent;
import tod.impl.local.event.Event;
import tod.impl.local.event.ExceptionGeneratedEvent;
import tod.impl.local.event.FieldWriteEvent;
import tod.impl.local.event.InstantiationEvent;
import tod.impl.local.event.LocalVariableWriteEvent;
import tod.impl.local.event.MethodCallEvent;
import tod.impl.local.event.OutputEvent;
import tod.impl.local.filter.AbstractFilter;
import tod.impl.local.filter.BehaviorCallFilter;
import tod.impl.local.filter.FieldWriteFilter;
import tod.impl.local.filter.InstantiationFilter;
import tod.impl.local.filter.IntersectionFilter;
import tod.impl.local.filter.TargetFilter;
import tod.impl.local.filter.ThreadFilter;
import tod.impl.local.filter.UnionFilter;

/**
 * This log collector stores all the events it receives,
 * and provides an API for accessing the recorded information
 * in a convenient way.
 * @author gpothier
 */
public class LocalCollector extends LocationRegistrer 
implements ILogCollector, IEventTrace
{
	private EventList itsEvents = new EventList();
	
	public ILocationTrace getLocationTrace()
	{
		return this;
	}
	
	public void clear()
	{
		itsEvents.clear();
	}

	@Override
	protected ThreadInfo createThreadInfo(long aId)
	{
		BehaviorCallEvent theRootEvent = new MethodCallEvent();
		MyThreadInfo theThreadInfo = new MyThreadInfo(aId, theRootEvent);
		theRootEvent.setThread(theThreadInfo);
		theRootEvent.setDirectParent(false);
		
//		itsEvents.add (theRootEvent);
		return theThreadInfo;
	}
	
	@Override
	public MyThreadInfo getThread(long aId)
	{
		return (MyThreadInfo) super.getThread(aId);
	}
	
	public EventList getEvents()
	{
		return itsEvents;
	}
	
	private void initEvent (
			Event aEvent, 
			long aTimestamp,
			MyThreadInfo aThreadInfo, 
            int aOperationBytecodeIndex)
	{
		aEvent.setTimestamp(aTimestamp);
		aEvent.setThread(aThreadInfo);
		aEvent.setOperationBytecodeIndex(aOperationBytecodeIndex);
		aEvent.setSerial(aThreadInfo.getSerial());
		
		BehaviorCallEvent theCurrentBehavior = 
			(BehaviorCallEvent) aThreadInfo.getCurrentParent();
		
		aEvent.setParent(theCurrentBehavior);
		theCurrentBehavior.addChild (aEvent);
	}
	

	public void logBehaviorEnter(
			long aTimestamp, 
			long aThreadId, 
			int aBehaviorLocationId, 
			Object aObject, 
			Object[] aArguments)
	{
		MyThreadInfo theThread = getThread(aThreadId);
		IBehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);
		assert theBehavior != null;
		BehaviorCallEvent theEvent;
		
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();
		if (theCurrentParent.isDirectParent())
		{
			// We come from instrumented code, an event already exists
			theEvent = theCurrentParent;
			theEvent.setTimestamp(aTimestamp);
		}
		else
		{
			// We come from non instrumented code, create a new event
			
			// There can be a problem as we cannot detect constructor 
			// chaining.
			theEvent = theBehavior.isConstructor() ?
					new InstantiationEvent()
					: new MethodCallEvent();
			
			initEvent(theEvent, aTimestamp, theThread, -1);
			
			theThread.setCurrentParent(theEvent);
			theEvent.setDirectParent(true);
			itsEvents.add (theEvent);
		}
		
		theEvent.setExecutedBehavior(theBehavior);
		theEvent.setTarget(aObject);
		theEvent.setArguments(aArguments);
	}

	public void logBehaviorExit(
			long aTimestamp,
			long aThreadId,
			int aBehaviorLocationId,
			Object aResult)
	{
		MyThreadInfo theThread = getThread(aThreadId);
		
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();
		assert theCurrentParent.getExecutedBehavior().getId() == aBehaviorLocationId;
		assert theCurrentParent.isDirectParent();
		
		theCurrentParent.setResult(aResult);
		theCurrentParent.setHasThrown(false);
		theCurrentParent.setLastTimestamp(aTimestamp);
		
		theThread.setCurrentParent((BehaviorCallEvent) theCurrentParent.getParent());
	}
	
	public void logBehaviorExitWithException(
			long aTimestamp, 
			long aThreadId,
			int aBehaviorLocationId, 
			Object aException)
	{
		MyThreadInfo theThread = getThread(aThreadId);
		
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();
		assert theCurrentParent.getExecutedBehavior().getId() == aBehaviorLocationId;
		assert theCurrentParent.isDirectParent();
		
		theCurrentParent.setResult(aException);
		theCurrentParent.setHasThrown(true);
		theCurrentParent.setLastTimestamp(aTimestamp);
		
		theThread.setCurrentParent((BehaviorCallEvent) theCurrentParent.getParent());
	}
	
	public void logExceptionGenerated(
			long aTimestamp,
			long aThreadId,
			int aBehaviorLocationId,
			int aOperationBytecodeIndex, 
			Object aException)
	{
		MyThreadInfo theThread = getThread(aThreadId);
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();		
		IBehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);

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
		MyThreadInfo theThread = getThread(aThreadId);
		
		String theClassName = Type.getType(aMethodDeclaringClassSignature).getClassName();
		IClassInfo theClass = (IClassInfo) getType(theClassName);
		
		if (theClass == null) return; // TODO: don't do that...
		
		ITypeInfo[] theArgumentTypes = getArgumentTypes(aMethodSignature);
		IBehaviorInfo theBehavior = theClass.getBehavior(aMethodName, theArgumentTypes);

		logExceptionGenerated(aTimestamp, theThread, theBehavior, aOperationBytecodeIndex, aException);
	}
	
	protected void logExceptionGenerated(
			long aTimestamp, 
			MyThreadInfo aThread,
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
			aThread.setCurrentParent((BehaviorCallEvent) theCurrentParent.getParent());
		}

		ExceptionGeneratedEvent theEvent = new ExceptionGeneratedEvent();
		initEvent(theEvent, aTimestamp, aThread, aOperationBytecodeIndex);
		
		theEvent.setThrowingBehavior(aBehavior);
		theEvent.setException(aException);
		
		itsEvents.add (theEvent);
	}
	
	public void logFieldWrite(
			long aTimestamp,
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aFieldLocationId, 
			Object aTarget, 
			Object aValue)
	{
		MyThreadInfo theThread = getThread(aThreadId);
		IFieldInfo theField = getField(aFieldLocationId);

		FieldWriteEvent theEvent = new FieldWriteEvent();
		initEvent(theEvent, aTimestamp, theThread, aOperationBytecodeIndex);
		
		theEvent.setTarget(aTarget);
		theEvent.setField(theField);
		theEvent.setValue(aValue);
		
		itsEvents.add (theEvent);
	}
	
	public void logLocalVariableWrite(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex,
			int aVariableId,
			Object aValue)
	{
		MyThreadInfo theThread = getThread(aThreadId);
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
		
		itsEvents.add (theEvent);
	}
	
	public void logInstantiation(long aThreadId)
	{
		MyThreadInfo theThread = getThread(aThreadId);
		theThread.expectInstantiation();
	}
	
	public void logConstructorChaining(long aThreadId)
	{
		MyThreadInfo theThread = getThread(aThreadId);
		theThread.expectConstructorChaining();
	}
	
	public void logBeforeBehaviorCall(
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aBehaviorLocationId)
	{
		MyThreadInfo theThread = getThread(aThreadId);
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();
		assert theCurrentParent.isDirectParent() && theCurrentParent.getExecutedBehavior() != null;
		
		IBehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);

		BehaviorCallEvent theEvent = theThread.createCallEvent();
		
		initEvent(theEvent, -1, theThread, aOperationBytecodeIndex);
		theEvent.setDirectParent(true);
		theEvent.setCalledBehavior(theBehavior);
		
		itsEvents.add (theEvent);
		
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
		MyThreadInfo theThread = getThread(aThreadId);

		IBehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);

		BehaviorCallEvent theEvent = theThread.createCallEvent();
		
		initEvent(theEvent, aTimestamp, theThread, aOperationBytecodeIndex);
		theEvent.setDirectParent(false);
		theEvent.setCalledBehavior(theBehavior);
		if (theBehavior.isConstructor()) theEvent.setExecutedBehavior(theBehavior);
		theEvent.setTarget(aTarget);
		theEvent.setArguments(aArguments);
		
		itsEvents.add (theEvent);
		
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
		MyThreadInfo theThread = getThread(aThreadId);
		
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();
		assert theCurrentParent.getCalledBehavior().getId() == aBehaviorLocationId;
		
		theCurrentParent.setResult(aResult);
		theCurrentParent.setHasThrown(false);
		theCurrentParent.setLastTimestamp(aTimestamp);
		theCurrentParent.setTarget(aTarget);
		
		theThread.setCurrentParent((BehaviorCallEvent) theCurrentParent.getParent());
	}
	
	public void logAfterBehaviorCallWithException(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex,
			int aBehaviorLocationId, 
			Object aTarget, 
			Object aException)
	{
		MyThreadInfo theThread = getThread(aThreadId);
		
		BehaviorCallEvent theCurrentParent = theThread.getCurrentParent();
		assert theCurrentParent.getCalledBehavior().getId() == aBehaviorLocationId;
		
		theCurrentParent.setResult(aException);
		theCurrentParent.setHasThrown(true);
		theCurrentParent.setLastTimestamp(aTimestamp);
		theCurrentParent.setTarget(aTarget);
		
		theThread.setCurrentParent((BehaviorCallEvent) theCurrentParent.getParent());
	}
	
	public void logOutput(
			long aTimestamp, 
			long aThreadId, 
			Output aOutput, 
			byte[] aData)
	{
		MyThreadInfo theThread = getThread(aThreadId);
		OutputEvent theEvent = new OutputEvent();
		initEvent(theEvent, aTimestamp, theThread, -1);
		
		theEvent.setOutput(aOutput);
		theEvent.setData(new String (aData));
		
		itsEvents.add (theEvent);
	}

	public long getFirstTimestamp()
	{
		return itsEvents.getFirstTimestamp();
	}

	public long getLastTimestamp()
	{
		return itsEvents.getLastTimestamp();
	}

	public IEventBrowser createBrowser (IEventFilter aFilter)
	{
		AbstractFilter theFilter = (AbstractFilter) aFilter;
		return theFilter.createBrowser();
	}
	
	
	public IEventFilter createArgumentFilter(ObjectId aId)
	{
		return null;
	}
	
	public IEventFilter createBehaviorCallFilter()
	{
		return new BehaviorCallFilter(this);
	}

	public IEventFilter createBehaviorCallFilter(IBehaviorInfo aBehavior)
	{
		return new BehaviorCallFilter(this, aBehavior);
	}

	public IEventFilter createFieldFilter(IFieldInfo aFieldInfo)
	{
		return new FieldWriteFilter(this, aFieldInfo);
	}
	
	public IEventFilter createFieldWriteFilter()
	{
		return new FieldWriteFilter(this);
	}
	
	public IEventFilter createInstantiationsFilter()
	{
		return new InstantiationFilter(this);
	}
	
	public IEventFilter createInstantiationsFilter(ITypeInfo aTypeInfo)
	{
		return new InstantiationFilter(this, aTypeInfo);
	}
	
	public IEventFilter createInstantiationFilter(ObjectId aObjectId)
	{
		return new InstantiationFilter(this, aObjectId);
	}
	
	public ICompoundFilter createIntersectionFilter(IEventFilter... aFilters)
	{
		return new IntersectionFilter(this, aFilters);
	}
	
	public IEventFilter createTargetFilter(ObjectId aId)
	{
		return new TargetFilter(this, aId);
	}
	
	public IEventFilter createThreadFilter(IThreadInfo aThreadInfo)
	{
		return new ThreadFilter(this, aThreadInfo.getId());
	}
	
	public ICompoundFilter createUnionFilter(IEventFilter... aFilters)
	{
		return new UnionFilter(this, aFilters);
	}
	
	public IEventFilter createLocationFilter(ITypeInfo aTypeInfo, int aLineNumber)
	{
		throw new UnsupportedOperationException();
	}
	
	public ICFlowBrowser createCFlowBrowser(IThreadInfo aThread)
	{
		return new CFlowBrowser(this, aThread, ((MyThreadInfo) aThread).getRootEvent());
	}
	
	public IObjectInspector createObjectInspector(ObjectId aObjectId)
	{
		return new ObjectInspector(this, aObjectId);
	}
	
	public IObjectInspector createClassInspector(IClassInfo aClass)
	{
		return new ObjectInspector(this, aClass);
	}

	public IVariablesInspector createVariablesInspector(IBehaviorCallEvent aEvent)
	{
		return new VariablesInspector(aEvent);
	}
	
	private static class MyThreadInfo extends ThreadInfo
	{
		private BehaviorCallEvent itsRootEvent;
		
		private BehaviorCallEvent itsCurrentParent;
		
		private boolean itsExpectingInstantiation;
		private boolean itsExpectingConstructorChaining;
		
		/**
		 * Serial number of the last event of this thread;
		 */
		private long itsSerial;
		
		public MyThreadInfo(long aId)
		{
			super(aId);
		}

		public MyThreadInfo(long aId, BehaviorCallEvent aRootEvent)
		{
			super(aId);
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
