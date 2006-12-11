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
package tod.core.database.event;

/**
 * This event is produced whenever a behavior exits, either normally
 * or because of an exception.
 * It is always the last event of a {@link IBehaviorCallEvent}.
 * @author gpothier
 */
public interface IBehaviorExitEvent extends ILogEvent
{
	/**
	 * Whether the behavior returned normally or with an
	 * exception.
	 * <br/>
	 * This information is always available.
	 */
	public boolean hasThrown();

	/**
	 * Value returned by the behavior, or exception thrown by the 
	 * behavior, according to the value of {@link #hasThrown()}.
	 */
	public Object getResult();
	

}
