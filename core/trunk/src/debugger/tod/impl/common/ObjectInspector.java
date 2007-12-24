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
package tod.impl.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.DebugFlags;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IWriteEvent;
import tod.core.database.structure.IArraySlotFieldInfo;
import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.impl.database.structure.standard.ArraySlotFieldInfo;
import zz.utils.Utils;

/**
 * Implementation of {@link tod.core.database.browser.IObjectInspector} based 
 * on API only.
 * @author gpothier
 */
public class ObjectInspector implements IObjectInspector
{
	private final ILogBrowser itsLogBrowser;
	private ObjectId itsObjectId;
	
	private Delegate itsDelegate;
	
	private Map<IMemberInfo, IEventBrowser> itsBrowsersMap = new HashMap<IMemberInfo, IEventBrowser>();
	private Map<IMemberInfo, IEventFilter> itsFiltersMap = new HashMap<IMemberInfo, IEventFilter>();
	private ITypeInfo itsType;
	
	private IInstantiationEvent itsInstantiationEvent;
	private boolean itsInstantiationEventValid = false;
	
	private long itsTimestamp;

	public ObjectInspector(ILogBrowser aEventTrace, ObjectId aObjectId)
	{
		itsLogBrowser = aEventTrace;
		itsObjectId = aObjectId;
	}
	
	public ObjectInspector(ILogBrowser aEventTrace, IClassInfo aClass)
	{
		itsLogBrowser = aEventTrace;
		itsType = aClass;
	}
	
	protected ILogBrowser getLogBrowser()
	{
		return itsLogBrowser;
	}

	public ObjectId getObject()
	{
		return itsObjectId;
	}
	
	public IInstantiationEvent getInstantiationEvent()
	{
		if (itsInstantiationEvent == null) 
		{
			System.out.println("[ObjectInspector] Retrieving instantiation event for object: "+getObject());
			IEventFilter theFilter = itsLogBrowser.createTargetFilter(getObject());
			IEventBrowser theBrowser = itsLogBrowser.createBrowser(theFilter);
			
			// Instantiation is the first event if it has been captured
			// Check for timestamp because of concurrency & accuracy of timer.
			long theTimestamp = 0;
			while (theBrowser.hasNext())
			{
				ILogEvent theEvent = theBrowser.next();
				if (theTimestamp == 0) theTimestamp = theEvent.getTimestamp();
				else if (theTimestamp != theEvent.getTimestamp()) break;
				
				if (theEvent instanceof IInstantiationEvent)
				{
					itsInstantiationEvent = (IInstantiationEvent) theEvent;
					break;
				}
			}
		}
		return itsInstantiationEvent;
		
	}
	
	public ITypeInfo getType()
	{
		if (itsType == null)
		{
			IInstantiationEvent theInstantiationEvent = getInstantiationEvent();
			
			if (theInstantiationEvent != null)
			{
				itsType = theInstantiationEvent.getType();
			}
			else if (DebugFlags.TRY_GUESS_TYPE)
			{
				// Try to guess type
				IEventFilter theFilter = itsLogBrowser.createIntersectionFilter(
						itsLogBrowser.createTargetFilter(itsObjectId),
						itsLogBrowser.createFieldWriteFilter());
				
				IEventBrowser theBrowser = itsLogBrowser.createBrowser(theFilter);
				if (theBrowser.hasNext())
				{
					IFieldWriteEvent theEvent = (IFieldWriteEvent) theBrowser.next();
					ITypeInfo theClass = theEvent.getField().getType();
					itsType = theClass.createUncertainClone();
				}
			}
			
			// Note that we cannot give a null
			if (itsType == null) itsType = getLogBrowser().getStructureDatabase().getUnknownClass();
		}
		return itsType;
	}
	
	protected Delegate getDelegate()
	{
		if (itsDelegate == null)
		{
			IInstantiationEvent theEvent = getInstantiationEvent();
			if (theEvent == null) itsDelegate = UNAVAILABLE;
			else
			{
				ITypeInfo theType = theEvent.getType();
				
				if (theType instanceof IArrayTypeInfo)
				{
					itsDelegate = new ArrayDelegate();
				}
				else if (theType instanceof IClassInfo)
				{
					itsDelegate = new ObjectDelegate();
				}
				else throw new RuntimeException("Not handled: "+theType);
			}
		}
		
		return itsDelegate;
	}
	
	public List<IMemberInfo> getMembers()
	{
		Delegate theDelegate = getDelegate();
		return theDelegate != UNAVAILABLE ? theDelegate.getMembers() : Collections.EMPTY_LIST;
	}
	
	public List<IFieldInfo> getFields()
	{
		Delegate theDelegate = getDelegate();
		return theDelegate != UNAVAILABLE ? theDelegate.getFields() : Collections.EMPTY_LIST;
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
	
	public ILogEvent getCurrentEvent()
	{
		throw new UnsupportedOperationException();
	}

	public Object[] getEntryValue(IFieldInfo aField)
	{
		IWriteEvent[] theSetters = getEntrySetter(aField);
		Object[] theResult = new Object[theSetters.length];
		
		for (int i = 0; i < theSetters.length; i++)
		{
			theResult[i] = theSetters[i].getValue();
		}
		
		return theResult;
	}

	public IWriteEvent[] getEntrySetter(IFieldInfo aField)
	{
		List<IWriteEvent> theResult = new ArrayList<IWriteEvent>();
		
		IEventBrowser theBrowser = getBrowser(aField);
		theBrowser.setPreviousTimestamp(itsTimestamp);
		
		long thePreviousTimestamp = -1;
		
		while (theBrowser.hasPrevious())
		{
			IWriteEvent theEvent = (IWriteEvent) theBrowser.previous();
			long theTimestamp = theEvent.getTimestamp();
			
			if (thePreviousTimestamp == -1) thePreviousTimestamp = theTimestamp;
			
			if (theTimestamp == thePreviousTimestamp) theResult.add(theEvent);
			else break;
		}
		
		return theResult.toArray(new IWriteEvent[theResult.size()]);
	}

	/**
	 * Returns an event browser for the specified field.
	 */
	public IEventBrowser getBrowser(IMemberInfo aMember)
	{
		IEventBrowser theBrowser = itsBrowsersMap.get(aMember);
		if (theBrowser == null)
		{
			theBrowser = itsLogBrowser.createBrowser(getFilter(aMember));
			itsBrowsersMap.put (aMember, theBrowser);
		}
		
		return theBrowser;
	}

	public IEventFilter getFilter(IMemberInfo aMember)
	{
		IEventFilter theFilter = itsFiltersMap.get(aMember);
		if (theFilter == null)
		{
			Delegate theDelegate = getDelegate();
			theFilter = theDelegate != UNAVAILABLE ? theDelegate.getFilter(aMember) : null;
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
	
	private static final Delegate UNAVAILABLE = new Delegate()
	{

		@Override
		public List<IFieldInfo> getFields()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public IEventFilter getFilter(IMemberInfo aMember)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public List<IMemberInfo> getMembers()
		{
			throw new UnsupportedOperationException();
		}
	};
	
	/**
	 * The object inspector uses a delegate that permits to abstract away the 
	 * differences between regular objects and arrays.
	 * @author gpothier
	 */
	private static abstract class Delegate
	{
		/**
		 * Delegate method for {@link ObjectInspector#getMembers()}
		 */
		public abstract List<IMemberInfo> getMembers();

		/**
		 * Delegate method for {@link ObjectInspector#getFields()}
		 */
		public abstract List<IFieldInfo> getFields();

		/**
		 * Delegate method for {@link ObjectInspector#getFilter(IMemberInfo)}
		 */
		public abstract IEventFilter getFilter(IMemberInfo aMember);
	}
	
	private class ObjectDelegate extends Delegate
	{
		private List<IMemberInfo> itsMembers;
		private List<IFieldInfo> itsFields;
		
		/**
		 * Recursively finds all inherited members of the inspected object.
		 */
		private void fillMembers (List<IMemberInfo> aMembers, ITypeInfo aType)
		{
			if (aType instanceof IClassInfo)
			{
				IClassInfo theClass = (IClassInfo) aType;
				
				Utils.fillCollection(aMembers, theClass.getFields());
				Utils.fillCollection(aMembers, theClass.getBehaviors());

				// Fill supertypes & interfaces, recursively
				ITypeInfo theSupertype = theClass.getSupertype();
				if (theSupertype != null) fillMembers(aMembers, theSupertype);
				ITypeInfo[] theInterfaces = theClass.getInterfaces();
				if (theInterfaces != null) for (ITypeInfo theInterface : theInterfaces)
				{
					fillMembers(aMembers, theInterface);
				}
			}
		}
		
		public List<IMemberInfo> getMembers()
		{
			if (itsMembers == null)
			{
				itsMembers = new ArrayList<IMemberInfo>();
				fillMembers(itsMembers, getType());
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
		
		public IEventFilter getFilter(IMemberInfo aMember)
		{
			IEventFilter theFilter;
			
			if (aMember instanceof IFieldInfo)
			{
				IFieldInfo theField = (IFieldInfo) aMember;
				
				theFilter = itsLogBrowser.createIntersectionFilter(
						itsLogBrowser.createFieldFilter(theField),
						itsLogBrowser.createTargetFilter(itsObjectId));
			}
			else if (aMember instanceof IBehaviorInfo)
			{
				IBehaviorInfo theBehavior = (IBehaviorInfo) aMember;

				theFilter = itsLogBrowser.createIntersectionFilter(
						itsLogBrowser.createBehaviorCallFilter(theBehavior),
						itsLogBrowser.createTargetFilter(itsObjectId));
				
			}
			else throw new RuntimeException("Not handled: "+aMember);
			
			return theFilter;
		}
	}
	
	private class ArrayDelegate extends Delegate
	{
		private List<IFieldInfo> itsFields;

		@Override
		public List<IMemberInfo> getMembers()
		{
			return (List) getFields();
		}
		
		@Override
		public List<IFieldInfo> getFields()
		{
			if (itsFields == null)
			{
				itsFields = new ArrayList<IFieldInfo>();
				IInstantiationEvent theEvent = getInstantiationEvent();
				int theSize = (Integer) theEvent.getArguments()[0];
				
				for (int i=0;i<theSize;i++)
				{
					itsFields.add(new ArraySlotFieldInfo(
							getLogBrowser().getStructureDatabase(),
							(IArrayTypeInfo) getType(),
							i));
				}
			}
			return itsFields;
		}
		
		@Override
		public IEventFilter getFilter(IMemberInfo aMember)
		{
			IArraySlotFieldInfo theField = (IArraySlotFieldInfo) aMember;
			return itsLogBrowser.createIntersectionFilter(
					itsLogBrowser.createFieldFilter(theField),
					itsLogBrowser.createTargetFilter(itsObjectId));
			
		}
	}
	
}
