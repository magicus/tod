/*
 * Created on Oct 25, 2004
 */
package reflex.lib.logging.miner.impl.local;

import java.util.HashMap;
import java.util.Map;

import reflex.lib.logging.miner.impl.common.AbstractMinerCollector;
import reflex.lib.logging.miner.impl.common.ObjectInspector;
import reflex.lib.logging.miner.impl.common.event.AfterMethodCall;
import reflex.lib.logging.miner.impl.common.event.BeforeMethodCall;
import reflex.lib.logging.miner.impl.common.event.BehaviorEnter;
import reflex.lib.logging.miner.impl.common.event.BehaviourExit;
import reflex.lib.logging.miner.impl.common.event.Event;
import reflex.lib.logging.miner.impl.common.event.FieldWriteEvent;
import reflex.lib.logging.miner.impl.common.event.Instantiation;
import reflex.lib.logging.miner.impl.common.event.LocalVariableWriteEvent;
import reflex.lib.logging.miner.impl.common.event.OutputEvent;
import reflex.lib.logging.miner.impl.local.filter.AbstractFilter;
import reflex.lib.logging.miner.impl.local.filter.BehaviorCallFilter;
import reflex.lib.logging.miner.impl.local.filter.BehaviourFilter;
import reflex.lib.logging.miner.impl.local.filter.FieldWriteFilter;
import reflex.lib.logging.miner.impl.local.filter.InstantiationFilter;
import reflex.lib.logging.miner.impl.local.filter.IntersectionFilter;
import reflex.lib.logging.miner.impl.local.filter.LocationFilter;
import reflex.lib.logging.miner.impl.local.filter.TargetFilter;
import reflex.lib.logging.miner.impl.local.filter.ThreadFilter;
import reflex.lib.logging.miner.impl.local.filter.UnionFilter;
import tod.core.Output;
import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.ObjectId;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.structure.TypeInfo;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.ICFlowBrowser;
import tod.core.model.trace.ICompoundFilter;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IEventFilter;
import tod.core.model.trace.IObjectInspector;

/**
 * This log collector stores all the events it receives,
 * and provides an API for accessing the recorded information
 * in a convenient way.
 * @author gpothier
 */
public class LocalCollector extends AbstractMinerCollector 
implements IEventTrace
{
	private EventList itsEvents = new EventList();
	
	public void clear()
	{
		itsEvents.clear();
	}

	@Override
	protected ThreadInfo createThreadInfo(long aId, String aName)
	{
		ThreadInfo theThreadInfo = super.createThreadInfo(aId, aName);
		BehaviorEnter theBehaviorEnter = new BehaviorEnter();
		theBehaviorEnter.setThread(theThreadInfo);
		theThreadInfo.setCurrentBehavior(theBehaviorEnter);
		itsEvents.add (theBehaviorEnter);
		
		return theThreadInfo;
	}
	
	public EventList getEvents()
	{
		return itsEvents;
	}
	
	private void initEvent (
			Event aEvent, 
			long aTimestamp,
			ThreadInfo aThreadInfo, 
            int aOperationBytecodeIndex, 
			long aSerial, 
			int aDepth)
	{
		aEvent.setTimestamp(aTimestamp);
		aEvent.setThread(aThreadInfo);
		aEvent.setOperationBytecodeIndex(aOperationBytecodeIndex);
		aEvent.setSerial(aSerial);
		aEvent.setDepth(aDepth);
		
		BehaviorEnter theCurrentBehavior = (BehaviorEnter) aThreadInfo.getCurrentBehavior();
		aEvent.setFather(theCurrentBehavior);
		theCurrentBehavior.addChild (aEvent);
	}
	

	protected void logBehaviorEnter(
			long aTimestamp,
			ThreadInfo aThreadInfo, 
			long aSerial, 
			int aDepth, 
			int aBehaviorId)
	{
		BehaviorEnter theEvent = new BehaviorEnter();
		initEvent(theEvent, aTimestamp, aThreadInfo, -1, aSerial, aDepth);
		
		theEvent.setBehavior(getBehavior(aBehaviorId));
		
		aThreadInfo.setCurrentBehavior(theEvent);
		
		itsEvents.add (theEvent);
	}

	protected void logBehaviorExit(
			long aTimestamp, 
			ThreadInfo aThreadInfo, 
			long aSerial,
			int aDepth, 
			int aBehaviorId)
	{
		BehaviourExit theEvent = new BehaviourExit();
		initEvent(theEvent, aTimestamp, aThreadInfo, -1, aSerial, aDepth);
		
		theEvent.setBehavior(getBehavior(aBehaviorId));

		aThreadInfo.setCurrentBehavior(aThreadInfo.getCurrentBehavior().getFather());
		
		itsEvents.add (theEvent);
	}
	
	protected void logFieldWrite(
			long aTimestamp, 
			ThreadInfo aThreadInfo,
            int aOperationBytecodeIndex, 
			long aSerial, 
			int aDepth, 
			int aFieldId,
			Object aTarget,
			Object aValue)
	{
		FieldWriteEvent theEvent = new FieldWriteEvent();
		initEvent(theEvent, aTimestamp, aThreadInfo, aOperationBytecodeIndex, aSerial, aDepth);
		
		theEvent.setTarget(aTarget);
		
		theEvent.setField(getField(aFieldId));
		theEvent.setValue(aValue);
		
		itsEvents.add (theEvent);
	}
	
	protected void logLocalVariableWrite(
			long aTimestamp, 
			ThreadInfo aThreadInfo,
            int aOperationBytecodeIndex, 
			long aSerial, 
			int aDepth, 
			int aVariableId,
			Object aTarget,
			Object aValue)
	{
		LocalVariableWriteEvent theEvent = new LocalVariableWriteEvent();
		initEvent(theEvent, aTimestamp, aThreadInfo, aOperationBytecodeIndex, aSerial, aDepth);
		
		theEvent.setTarget(aTarget);
		
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
				
       	BehaviorInfo theBehavior = aThreadInfo.getCurrentBehavior().getBehavior();
       	theInfo = theBehavior.getLocalVariableInfo(aOperationBytecodeIndex+35, aVariableId); // 35 is the size of our instrumentation
       	if (theInfo == null) theInfo = new LocalVariableInfo((short)-1, (short)-1, "$"+aVariableId, "", (short)-1);
        
		theEvent.setVariable(theInfo);
		theEvent.setValue(aValue);
		
		itsEvents.add (theEvent);
	}
	
	protected void logInstantiation(
			long aTimestamp, 
			ThreadInfo aThreadInfo,
            int aOperationBytecodeIndex, 
			long aSerial, 
			int aDepth, 
			int aLocationId,
			Object aInstance)
	{
		Instantiation theEvent = new Instantiation();
		initEvent(theEvent, aTimestamp, aThreadInfo, aOperationBytecodeIndex, aSerial, aDepth);
		
		theEvent.setType(getType(aLocationId));
		theEvent.setInstance(aInstance);
		
		itsEvents.add (theEvent);
	}
	
	protected void logBeforeMethodCall(
			long aTimestamp, 
			ThreadInfo aThreadInfo,
            int aOperationBytecodeIndex, 
			long aSerial, 
			int aDepth, 
			int aLocationId,
			Object aTarget,
			Object[] aArguments)
	{
		BeforeMethodCall theEvent = new BeforeMethodCall();
		initEvent(theEvent, aTimestamp, aThreadInfo, aOperationBytecodeIndex, aSerial, aDepth);
		
		theEvent.setTarget (aTarget);
		theEvent.setBehavior(getBehavior(aLocationId));
		theEvent.setArguments(aArguments);
		
		itsEvents.add (theEvent);
	}
	
	protected void logAfterMethodCall(
			long aTimestamp, 
			ThreadInfo aThreadInfo,
            int aOperationBytecodeIndex, 
			long aSerial, 
			int aDepth, 
			int aLocationId,
			Object aTarget,
			Object aReturnValue)
	{
		AfterMethodCall theEvent = new AfterMethodCall();
		initEvent(theEvent, aTimestamp, aThreadInfo, aOperationBytecodeIndex, aSerial, aDepth);
		
		theEvent.setTarget (aTarget);
		theEvent.setBehavior(getBehavior(aLocationId));
		theEvent.setReturnValue(aReturnValue);
		
		itsEvents.add (theEvent);
	}
	
	protected void logOutput(
			long aTimestamp,
			ThreadInfo aThreadInfo, 
			long aSerial, 
			int aDepth, 
			Output aOutput, 
			byte[] aData)
	{
		OutputEvent theEvent = new OutputEvent();
		initEvent(theEvent, aTimestamp, aThreadInfo, -1, aSerial, aDepth);
		
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
	
	public IEventFilter createBehaviorFilter()
	{
		return new BehaviourFilter(this);
	}
	
	public IEventFilter createBehaviorFilter(BehaviorInfo aBehaviourInfo)
	{
		return new BehaviourFilter(this, aBehaviourInfo);
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
		return new LocationFilter(this, aTypeInfo, aLineNumber);
	}
	
	public ICFlowBrowser createCFlowBrowser(ThreadInfo aThread)
	{
		return new CFlowBrowser(this, aThread);
	}
	
	public IObjectInspector createObjectInspector(ObjectId aObjectId)
	{
		return new ObjectInspector(this, aObjectId);
	}
	
	
}
