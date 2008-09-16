/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.core.database.browser;

import java.util.List;

import tod.core.database.event.ICreationEvent;
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
public interface IObjectInspector extends ICompoundInspector<IFieldInfo>
{
	/**
	 * Returns the log browser that created this inspector.
	 */
	public ILogBrowser getLogBrowser();
	
	/**
	 * Returns the identifier of the inspected object.
	 */
	public ObjectId getObject();
	
	/**
	 * Returns the event that corresponds to the creation of the
	 * inspected object.
	 */
	public ICreationEvent getCreationEvent();
	
	/**
	 * Returns the type descriptor of the object.
	 */
	public ITypeInfo getType();
	
	/**
	 * Retrieves all the field descriptors of the inspected object.
	 */
	public List<IFieldInfo> getFields();
	
	/**
	 * Returns an event broswer on the events that changed the value 
	 * of the specified member of the inspected object.
	 */
	public IEventBrowser getBrowser (IFieldInfo aField);
	
	/**
	 * Returns the possible values assigned to the given field by the given event.
	 * @param aEvent Must be an event returned by {@link #getBrowser(IFieldInfo)}.
	 */
	public Object[] getNewValue(IFieldInfo aField, ILogEvent aEvent);
}
