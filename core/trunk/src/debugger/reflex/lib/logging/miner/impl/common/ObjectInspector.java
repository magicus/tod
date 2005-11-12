/*
 * Created on Nov 18, 2004
 */
package reflex.lib.logging.miner.impl.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.IInstantiationEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.MemberInfo;
import tod.core.model.structure.ObjectId;
import tod.core.model.structure.TypeInfo;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IEventFilter;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.IObjectInspector;
import zz.utils.Utils;

/**
 * Implementation of {@link tod.core.model.trace.IObjectInspector} based 
 * on API only.
 * @author gpothier
 */
public class ObjectInspector implements IObjectInspector
{
	private final IEventTrace itsLog;
	private ObjectId itsObjectId;
	private List<MemberInfo> itsMembers;
	
	private Map<MemberInfo, IEventBrowser> itsBrowsersMap = new HashMap<MemberInfo, IEventBrowser>();
	private Map<MemberInfo, IEventFilter> itsFiltersMap = new HashMap<MemberInfo, IEventFilter>();
	private TypeInfo itsType;
	
	private long itsTimestamp;

	public ObjectInspector(IEventTrace aLog, ObjectId aObjectId)
	{
		itsLog = aLog;
		itsObjectId = aObjectId;
	}
	
	public ObjectId getObject()
	{
		return itsObjectId;
	}
	
	public TypeInfo getType()
	{
		if (itsType == null) 
		{
			IEventFilter theFilter = itsLog.createInstantiationFilter(getObject());
			IEventBrowser theBrowser = itsLog.createBrowser(theFilter);
			
			if (theBrowser.hasNext())
			{
				IInstantiationEvent theEvent = (IInstantiationEvent) theBrowser.getNext();
				itsType = theEvent.getType();
			}
			else itsType = new TypeInfo(-1, "Unknown", null, null); 
		}
		return itsType;
	}
	
	/**
	 * Recursively finds all inherited members of the inspected object.
	 */
	private void fillMembers (TypeInfo aType)
	{
		Utils.fillCollection(itsMembers, aType.getFields());
		Utils.fillCollection(itsMembers, aType.getBehaviors());

		// Fill supertypes & interfaces, recursively
		TypeInfo theSupertype = aType.getSupertype();
		if (theSupertype != null) fillMembers(theSupertype);
		TypeInfo[] theInterfaces = aType.getInterfaces();
		if (theInterfaces != null) for (TypeInfo theInterface : theInterfaces)
		{
			fillMembers(theInterface);
		}
	}
	
	public List<MemberInfo> getMembers()
	{
		if (itsMembers == null)
		{
			itsMembers = new ArrayList<MemberInfo>();
			fillMembers(getType());
		}
		return itsMembers;
	}

	public void setTimestamp(long aTimestamp)
	{
		itsTimestamp = aTimestamp;
	}
	
	public long getTimestamp()
	{
		return itsTimestamp;
	}
	
	public List<Object> getFieldValue(FieldInfo aField)
	{
		List<Object> theResult = new ArrayList<Object>();
		
		IEventBrowser theBrowser = getBrowser(aField);
		theBrowser.setPreviousTimestamp(itsTimestamp);
		
		long thePreviousTimestamp = -1;
		
		while (theBrowser.hasPrevious())
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) theBrowser.getPrevious();
			long theTimestamp = theEvent.getTimestamp();
			
			if (thePreviousTimestamp == -1) thePreviousTimestamp = theTimestamp;
			
			if (theTimestamp == thePreviousTimestamp) theResult.add(theEvent.getValue());
			else break;
		}
		
		return theResult;
	}

	/**
	 * Returns an event browser for the specified field.
	 */
	public IEventBrowser getBrowser(MemberInfo aMember)
	{
		IEventBrowser theBrowser = itsBrowsersMap.get(aMember);
		if (theBrowser == null)
		{
			theBrowser = itsLog.createBrowser(getFilter(aMember));
			itsBrowsersMap.put (aMember, theBrowser);
		}
		
		return theBrowser;
	}

	public IEventFilter getFilter(MemberInfo aMember)
	{
		IEventFilter theFilter = itsFiltersMap.get(aMember);
		if (theFilter == null)
		{
			if (aMember instanceof FieldInfo)
			{
				FieldInfo theField = (FieldInfo) aMember;
				
				theFilter = itsLog.createIntersectionFilter(
						itsLog.createFieldFilter(theField),
						itsLog.createFieldWriteFilter(),
						itsLog.createTargetFilter(itsObjectId));
			}
			else if (aMember instanceof BehaviorInfo)
			{
				BehaviorInfo theBehavior = (BehaviorInfo) aMember;

				theFilter = itsLog.createIntersectionFilter(
						itsLog.createBehaviorCallFilter(theBehavior),
//						itsLog.createFieldWriteFilter(),
						itsLog.createTargetFilter(itsObjectId));
				
			}
			else throw new RuntimeException("Not handled: "+aMember);
			
			itsFiltersMap.put(aMember, theFilter);
		}
		
		return theFilter;
	}
	
	
	public boolean hasNext()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public boolean hasPrevious()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public void stepToNext()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public void stepToPrevious()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public boolean hasNext(MemberInfo aMember)
	{
		IEventBrowser theBrowser = getBrowser(aMember);
		theBrowser.setPreviousTimestamp(itsTimestamp);
		return theBrowser.hasNext();
	}
	
	public void stepToNext(MemberInfo aMember)
	{
		IEventBrowser theBrowser = getBrowser(aMember);
		theBrowser.setPreviousTimestamp(itsTimestamp);
		ILogEvent theEvent = theBrowser.getNext();
		if (theEvent != null) setTimestamp(theEvent.getTimestamp());
	}
	
	public boolean hasPrevious(MemberInfo aMember)
	{
		IEventBrowser theBrowser = getBrowser(aMember);
		theBrowser.setNextTimestamp(itsTimestamp);
		return theBrowser.hasPrevious();
	}
	
	public void stepToPrevious(MemberInfo aMember)
	{
		IEventBrowser theBrowser = getBrowser(aMember);
		theBrowser.setNextTimestamp(itsTimestamp);
		ILogEvent theEvent = theBrowser.getPrevious();
		if (theEvent != null) setTimestamp(theEvent.getTimestamp());
	}
}
