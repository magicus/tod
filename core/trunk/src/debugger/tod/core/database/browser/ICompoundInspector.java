/*
TOD - Trace Oriented Debugger.
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

import tod.core.database.event.ILogEvent;
import tod.core.database.event.IWriteEvent;

/**
 * Inspector of coumpound entities such as objects or stack frames.
 * A compound entity is composed of entries (eg. variables, fields).
 * @author gpothier
 */
public interface ICompoundInspector<E>
{
	/**
	 * Sets the reference event of this inspector. Values of entries 
	 * obtained by {@link #getEntryValue(Object)} 
	 * are the values they had at the moment
	 * the reference event was executed.
	 */
	public void setReferenceEvent (ILogEvent aEvent);
	
	/**
	 * Returns the current reference event of this inspector.
	 * Values are reconstituted at the time the reference event occurred.
	 */
	public ILogEvent getReferenceEvent();
	
	/**
	 * Returns the possible values of the specified entry at the time the 
	 * current event was executed.
	 * @return An array of possible values. If there is more than one value,
	 * it means that it was impossible to retrive an unambiguous value.
	 * This can happen for instance if several write events have
	 * the same timestamp.
	 */
	public Object[] getEntryValue (E aEntry);
	
	/**
	 * Returns the possible events that set the entry to the value it had at
	 * the time the current event was executed.
	 */
	public IWriteEvent[] getEntrySetter(E aEntry);

}
