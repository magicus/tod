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
