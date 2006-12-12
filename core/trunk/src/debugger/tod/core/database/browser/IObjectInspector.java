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
package tod.core.database.browser;

import java.util.List;

import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;

/**
 * Permits to estimate the state of an object at a given moment.
 * It maintains a current timestamp and permit to navigate step by
 * by step in the object's history.
 * <br/>
 * It can also provide browsers for individual members. 
 * @author gpothier
 */
public interface IObjectInspector
{
	/**
	 * Returns the identifier of the inspected object.
	 */
	public ObjectId getObject();
	
	/**
	 * Returns the type descriptor of the object.
	 */
	public ITypeInfo getType ();
	
	/**
	 * Retrieves all the member descriptors of the inspected object.
	 */
	public List<IMemberInfo> getMembers();
	
	/**
	 * Retrieves all the field descriptors of the inspected object.
	 */
	public List<IFieldInfo> getFields();
	
	/**
	 * Sets the current timestamp of this inspector.
	 */
	public void setTimestamp (long aTimestamp);
	
	public void setCurrentEvent (ILogEvent aEvent);
	
	/**
	 * Returns the current timestamp of this inspector
	 */
	public long getTimestamp ();
	
	/**
	 * Returns the possible values of the given field at the current timestamp
	 * @param aField The field whose value is to be retrieved.
	 * @return A list of possible values. If there is more than one value,
	 * it means that it was impossible to retrive an unambiguous value.
	 * This can happen for instance if several field write events have
	 * the same timestamp.
	 */
	public List<Object> getFieldValue (IFieldInfo aField);
	
	/**
	 * Returns the candidate events corresponding to {@link #getFieldValue(IFieldInfo)}.
	 */
	public List<IFieldWriteEvent> getFieldSetter (IFieldInfo aField);
	
	/**
	 * Returns a filter on field write or behavior call events for the specified member
	 * of the inspected object.
	 */
	public IEventFilter getFilter (IMemberInfo aMemberInfo);
	
	/**
	 * Returns an event broswer on field write or behavior call events for the specified member
	 * of the inspected object.
	 */
	public IEventBrowser getBrowser (IMemberInfo aMemberInfo);
	
	/**
	 * Positions this inspector to the point of the next field write/behavior call
	 * of the specified member.
	 */
	public void stepToNext (IMemberInfo aMemberInfo);
	
	/**
	 * Indicates if there is a field write/behavior call to the specified member after
	 * the current timestamp.
	 */
	public boolean hasNext (IMemberInfo aMemberInfo);

	/**
	 * Positions this inspector to the point of the previous field write/behavior call
	 * of the specified member.
	 */
	public void stepToPrevious (IMemberInfo aMemberInfo);
	
	/**
	 * Indicates if there is a field write/behavior call to the specified member before
	 * the current timestamp.
	 */
	public boolean hasPrevious (IMemberInfo aMemberInfo);

	/**
	 * Positions this inspector to the point of the next field write/behavior call
	 * of any member.
	 */
	public void stepToNext ();
	
	/**
	 * Indicates if there is a field write/behavior call to any member after
	 * the current timestamp.
	 */
	public boolean hasNext ();

	/**
	 * Positions this inspector to the point of the previous field write/behavior call
	 * of any member.
	 */
	public void stepToPrevious ();

	/**
	 * Indicates if there is a field write/behavior call to any member before
	 * the current timestamp.
	 */
	public boolean hasPrevious ();
}
