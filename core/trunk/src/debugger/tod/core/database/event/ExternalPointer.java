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

import java.io.Serializable;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;


/**
 * External event pointer, comprised of host id, thread id
 * and timestamp, which is enough information to uniquely
 * identify an event. 
 * @see ILogBrowser#getEvent(ExternalPointer)
 * @author gpothier
 */
public class ExternalPointer implements Serializable
{
	private static final long serialVersionUID = -3084204556891153420L;
	
	public final IThreadInfo thread;
	public final long timestamp;

	public ExternalPointer(IThreadInfo aThread, long aTimestamp)
	{
		thread = aThread;
		timestamp = aTimestamp;
	}

	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((thread == null) ? 0 : thread.hashCode());
		result = PRIME * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final ExternalPointer other = (ExternalPointer) obj;
		if (thread == null)
		{
			if (other.thread != null) return false;
		}
		else if (!thread.equals(other.thread)) return false;
		if (timestamp != other.timestamp) return false;
		return true;
	}

}

