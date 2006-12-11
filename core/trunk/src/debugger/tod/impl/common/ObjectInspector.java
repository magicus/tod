/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
package tod.impl.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ClassInfo;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import zz.utils.Utils;

/**
 * Implementation of {@link tod.core.database.browser.IObjectInspector} based 
 * on API only.
 * @author gpothier
 */
public class ObjectInspector implements IObjectInspector
{
	private final ILogBrowser itsEventTrace;
	private ObjectId itsObjectId;
	private List<IMemberInfo> itsMembers;
	private List<IFieldInfo> itsFields;
	
	private Map<IMemberInfo, IEventBrowser> itsBrowsersMap = new HashMap<IMemberInfo, IEventBrowser>();
	private Map<IMemberInfo, IEventFilter> itsFiltersMap = new HashMap<IMemberInfo, IEventFilter>();
	private ITypeInfo itsType;
	
	private long itsTimestamp;

	public ObjectInspector(ILogBrowser aEventTrace, ObjectId aObjectId)
	{
		itsEventTrace = aEventTrace;
		itsObjectId = aObjectId;
	}
	
	public ObjectInspector(ILogBrowser aEventTrace, IClassInfo aClass)
	{
		itsEventTrace = aEventTrace;
		itsType = aClass;
	}
	
	public ObjectId getObject()
	{
		return itsObjectId;
	}
	
	public ITypeInfo getType()
	{
		if (itsType == null) 
		{
			IEventFilter theFilter = itsEventTrace.createInstantiationFilter(getObject());
			IEventBrowser theBrowser = itsEventTrace.createBrowser(theFilter);
			
			if (theBrowser.hasNext())
			{
				IInstantiationEvent theEvent = (IInstantiationEvent) theBrowser.next();
				itsType = theEvent.getType();
			}
			else itsType = new ClassInfo(-1, "Unknown"); 
		}
		return itsType;
	}
	
	/**
	 * Recursively finds all inherited members of the inspected object.
	 */
	private void fillMembers (ITypeInfo aType)
	{
		if (aType instanceof IClassInfo)
		{
			IClassInfo theClass = (IClassInfo) aType;
			
			Utils.fillCollection(itsMembers, theClass.getFields());
			Utils.fillCollection(itsMembers, theClass.getBehaviors());

			// Fill supertypes & interfaces, recursively
			ITypeInfo theSupertype = theClass.getSupertype();
			if (theSupertype != null) fillMembers(theSupertype);
			ITypeInfo[] theInterfaces = theClass.getInterfaces();
			if (theInterfaces != null) for (ITypeInfo theInterface : theInterfaces)
			{
				fillMembers(theInterface);
			}
		}
	}
	
	public List<IMemberInfo> getMembers()
	{
		if (itsMembers == null)
		{
			itsMembers = new ArrayList<IMemberInfo>();
			fillMembers(getType());
		}
		return itsMembers;
	}
	
	public List<IFieldInfo> getFields()
	{
		if (itsFields == null)
		{
			itsFields = new ArrayList<IFieldInfo>();
			
			for (IMemberInfo theMember : getMembers())
			{
				if (theMember instanceof IFieldInfo)
				{
					IFieldInfo theField = (IFieldInfo) theMember;
					itsFields.add (theField);
				}
			}
		}
		return itsFields;
	}

	public void setTimestamp(long aTimestamp)
	{
		itsTimestamp = aTimestamp;
	}
	
	public void setCurrentEvent(ILogEvent aEvent)
	{
		setTimestamp(aEvent.getTimestamp());
	}

	public long getTimestamp()
	{
		return itsTimestamp;
	}
	
	public List<Object> getFieldValue(IFieldInfo aField)
	{
		List<IFieldWriteEvent> theSetters = getFieldSetter(aField);
		List<Object> theResult = new ArrayList<Object>();
		for (IFieldWriteEvent theSetter : theSetters) theResult.add(theSetter.getValue());
		return theResult;
	}
	
	public List<IFieldWriteEvent> getFieldSetter(IFieldInfo aField)
	{
		List<IFieldWriteEvent> theResult = new ArrayList<IFieldWriteEvent>();
		
		IEventBrowser theBrowser = getBrowser(aField);
		theBrowser.setPreviousTimestamp(itsTimestamp);
		
		long thePreviousTimestamp = -1;
		
		while (theBrowser.hasPrevious())
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) theBrowser.previous();
			long theTimestamp = theEvent.getTimestamp();
			
			if (thePreviousTimestamp == -1) thePreviousTimestamp = theTimestamp;
			
			if (theTimestamp == thePreviousTimestamp) theResult.add(theEvent);
			else break;
		}
		
		return theResult;
	}

	/**
	 * Returns an event browser for the specified field.
	 */
	public IEventBrowser getBrowser(IMemberInfo aMember)
	{
		IEventBrowser theBrowser = itsBrowsersMap.get(aMember);
		if (theBrowser == null)
		{
			theBrowser = itsEventTrace.createBrowser(getFilter(aMember));
			itsBrowsersMap.put (aMember, theBrowser);
		}
		
		return theBrowser;
	}

	public IEventFilter getFilter(IMemberInfo aMember)
	{
		IEventFilter theFilter = itsFiltersMap.get(aMember);
		if (theFilter == null)
		{
			if (aMember instanceof IFieldInfo)
			{
				IFieldInfo theField = (IFieldInfo) aMember;
				
				theFilter = itsEventTrace.createIntersectionFilter(
						itsEventTrace.createFieldFilter(theField),
						itsEventTrace.createFieldWriteFilter(),
						itsEventTrace.createTargetFilter(itsObjectId));
			}
			else if (aMember instanceof IBehaviorInfo)
			{
				IBehaviorInfo theBehavior = (IBehaviorInfo) aMember;

				theFilter = itsEventTrace.createIntersectionFilter(
						itsEventTrace.createBehaviorCallFilter(theBehavior),
//						itsLog.createFieldWriteFilter(),
						itsEventTrace.createTargetFilter(itsObjectId));
				
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
	
	public boolean hasNext(IMemberInfo aMember)
	{
		IEventBrowser theBrowser = getBrowser(aMember);
		theBrowser.setPreviousTimestamp(itsTimestamp);
		return theBrowser.hasNext();
	}
	
	public void stepToNext(IMemberInfo aMember)
	{
		IEventBrowser theBrowser = getBrowser(aMember);
		theBrowser.setPreviousTimestamp(itsTimestamp);
		ILogEvent theEvent = theBrowser.next();
		if (theEvent != null) setTimestamp(theEvent.getTimestamp());
	}
	
	public boolean hasPrevious(IMemberInfo aMember)
	{
		IEventBrowser theBrowser = getBrowser(aMember);
		theBrowser.setNextTimestamp(itsTimestamp);
		return theBrowser.hasPrevious();
	}
	
	public void stepToPrevious(IMemberInfo aMember)
	{
		IEventBrowser theBrowser = getBrowser(aMember);
		theBrowser.setNextTimestamp(itsTimestamp);
		ILogEvent theEvent = theBrowser.previous();
		if (theEvent != null) setTimestamp(theEvent.getTimestamp());
	}
}
