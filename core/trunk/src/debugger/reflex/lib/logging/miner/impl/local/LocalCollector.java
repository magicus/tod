/*
 * Created on Oct 25, 2004
 */
package reflex.lib.logging.miner.impl.local;

import org.objectweb.asm.Type;

import reflex.lib.logging.miner.impl.common.ObjectInspector;
import reflex.lib.logging.miner.impl.common.VariablesInspector;
import reflex.lib.logging.miner.impl.common.event.BehaviorCallEvent;
import reflex.lib.logging.miner.impl.common.event.ConstructorChainingEvent;
import reflex.lib.logging.miner.impl.common.event.Event;
import reflex.lib.logging.miner.impl.common.event.ExceptionGeneratedEvent;
import reflex.lib.logging.miner.impl.common.event.FieldWriteEvent;
import reflex.lib.logging.miner.impl.common.event.InstantiationEvent;
import reflex.lib.logging.miner.impl.common.event.LocalVariableWriteEvent;
import reflex.lib.logging.miner.impl.common.event.MethodCallEvent;
import reflex.lib.logging.miner.impl.common.event.OutputEvent;
import reflex.lib.logging.miner.impl.local.filter.AbstractFilter;
import reflex.lib.logging.miner.impl.local.filter.BehaviorCallFilter;
import reflex.lib.logging.miner.impl.local.filter.FieldWriteFilter;
import reflex.lib.logging.miner.impl.local.filter.InstantiationFilter;
import reflex.lib.logging.miner.impl.local.filter.IntersectionFilter;
import reflex.lib.logging.miner.impl.local.filter.TargetFilter;
import reflex.lib.logging.miner.impl.local.filter.ThreadFilter;
import reflex.lib.logging.miner.impl.local.filter.UnionFilter;
import tod.bci.asm.BCIUtils;
import tod.core.ILogCollector;
import tod.core.LocationRegistrer;
import tod.core.Output;
import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.event.IParentEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.ClassInfo;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.ObjectId;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.structure.TypeInfo;
import tod.core.model.trace.ICFlowBrowser;
import tod.core.model.trace.ICompoundFilter;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IEventFilter;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.ILocationTrace;
import tod.core.model.trace.IObjectInspector;
import tod.core.model.trace.IVariablesInspector;

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
		ThreadInfo theThreadInfo = new MyThreadInfo(aId, theRootEvent);
		theRootEvent.setThread(theThreadInfo);
		theRootEvent.setDirectParent(false);
		
		itsEvents.add (theRootEvent);
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
		BehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);
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
			itsEvents.add (theEvent);
		}
		
		theEvent.setCalledBehavior(theBehavior);
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
		assert theCurrentParent.getCalledBehavior().getId() == aBehaviorLocationId;
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
		assert theCurrentParent.getCalledBehavior().getId() == aBehaviorLocationId;
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
		
		BehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);

		ExceptionGeneratedEvent theEvent = new ExceptionGeneratedEvent();
		initEvent(theEvent, aTimestamp, theThread, aOperationBytecodeIndex);
		
		theEvent.setThrowingBehavior(theBehavior);
		theEvent.setException(aException);
		
		itsEvents.add (theEvent);
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
		ClassInfo theClass = (ClassInfo) getType(theClassName);
		TypeInfo[] theArgumentTypes = getArgumentTypes(aMethodSignature);
		BehaviorInfo theBehavior = theClass.getBehavior(aMethodName, theArgumentTypes);
		
		ExceptionGeneratedEvent theEvent = new ExceptionGeneratedEvent();
		initEvent(theEvent, aTimestamp, theThread, aOperationBytecodeIndex);
		
		theEvent.setThrowingBehavior(theBehavior);
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
		FieldInfo theField = getField(aFieldLocationId);

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
		BehaviorInfo theBehavior = theCurrentParent.getCalledBehavior();
		
		LocalVariableWriteEvent theEvent = new LocalVariableWriteEvent();
		initEvent(theEvent, aTimestamp, theThread, aOperationBytecodeIndex);
		
        LocalVariableInfo theInfo = null;
//        if (aVariableId >= 0)
//        {
//        	BehaviorInfo theBehavior = aThreadInfo.getCurrentBehavior().getBehavior();
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
			int aMethodLocationId)
	{
		MyThreadInfo theThread = getThread(aThreadId);

		BehaviorCallEvent theEvent = theThread.createCallEvent();
		
		initEvent(theEvent, -1, theThread, aOperationBytecodeIndex);
		theEvent.setDirectParent(true);
		
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
		BehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);

		BehaviorCallEvent theEvent = theThread.createCallEvent();
		
		initEvent(theEvent, aTimestamp, theThread, aOperationBytecodeIndex);
		theEvent.setDirectParent(false);
		theEvent.setCalledBehavior(theBehavior);
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
		return itsEvents.get(0).getTimestamp();
	}

	public long getLastTimestamp()
	{
		return itsEvents.getLast().getTimestamp();
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

	public IEventFilter createBehaviorCallFilter(BehaviorInfo aBehaviorInfo)
	{
		return new BehaviorCallFilter(this, aBehaviorInfo);
	}

	public IEventFilter createFieldFilter(FieldInfo aFieldInfo)
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
	
	public IEventFilter createInstantiationsFilter(TypeInfo aTypeInfo)
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
	
	public IEventFilter createThreadFilter(ThreadInfo aThreadInfo)
	{
		return new ThreadFilter(this, aThreadInfo.getId());
	}
	
	public ICompoundFilter createUnionFilter(IEventFilter... aFilters)
	{
		return new UnionFilter(this, aFilters);
	}
	
	public IEventFilter createLocationFilter(TypeInfo aTypeInfo, int aLineNumber)
	{
		throw new UnsupportedOperationException();
	}
	
	public ICFlowBrowser createCFlowBrowser(ThreadInfo aThread)
	{
		return new CFlowBrowser(this, aThread, ((MyThreadInfo) aThread).getRootEvent());
	}
	
	public IObjectInspector createObjectInspector(ObjectId aObjectId)
	{
		return new ObjectInspector(this, aObjectId);
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
