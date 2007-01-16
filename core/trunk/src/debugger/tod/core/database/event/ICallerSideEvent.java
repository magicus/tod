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
package tod.core.database.event;

import tod.core.database.structure.IBehaviorInfo;


/**
 * This interface provide methods that give information
 * about a caller-side event.
 * @author gpothier
 */
public interface ICallerSideEvent extends ILogEvent
{
	/**
	 * Bytecode index of the call within the calling behavior.
	 * <br/>
	 * This information is available only if the caller behavior
	 * was instrumented.
	 * @return Index of the call, or -1 if not available
	 * @see #getCallingBehavior()
	 */
	public int getOperationBytecodeIndex();
	
	/**
	 * Returns the id of the behavior that performed the operation.
	 */
	public IBehaviorInfo getOperationBehavior();

}
