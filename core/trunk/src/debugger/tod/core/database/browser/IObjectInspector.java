/*
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
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
	 * Returns a filter on field write or behavior call events for the specified member
	 * of the inspected object.
	 * @return The filter, or null if the information is not available.
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
