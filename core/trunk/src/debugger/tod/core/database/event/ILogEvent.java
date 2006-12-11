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

import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;

/**
 * Root of the interface graph of logging events.
 * @author gpothier
 */
public interface ILogEvent
{
	/**
	 * Identifies the host in which the event occurred.
	 */
	public IHostInfo getHost();
	
	/**
	 * Identifies the thread in which the event occured.
	 */
	public IThreadInfo getThread();
	
	/**
	 * Depth of this event in its control flow stack.
	 */
	public int getDepth();
	
	/**
	 * Timestamp of the event. Its absolute value has no
	 * meaning, but the difference between two timestamps
	 * is a duration in nanoseconds.
	 */
	public long getTimestamp();
	
	/**
	 * Returns behavior call event corresponding to the behavior execution
	 * during which this event occured.
	 */
	public IBehaviorCallEvent getParent();
}
