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
package tod.impl.common.event;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ExternalPointer;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;

/**
 * Base class of all logged events.
 * @author gpothier
 */
public abstract class Event implements ICallerSideEvent
{
	private ILogBrowser itsLogBrowser;
	
	private long itsTimestamp;
	
	private IHostInfo itsHost;
	private IThreadInfo itsThread;
	
	private IBehaviorInfo itsOperationBehavior; 
	private int itsOperationBytecodeIndex;
	
	private long itsParentTimestamp;
	private BehaviorCallEvent itsParent;
	
	private int itsDepth;
	
	public Event(ILogBrowser aLogBrowser)
	{
		itsLogBrowser = aLogBrowser;
	}

	public ExternalPointer getPointer()
	{
		return new ExternalPointer(getHost(), getThread(), getTimestamp());
	}
	
	public int getDepth()
	{
		return itsDepth;
	}

	public void setDepth(int aDepth)
	{
		itsDepth = aDepth;
	}

	public BehaviorCallEvent getParent()
	{
		if (itsParent == null)
		{
			itsParent = (BehaviorCallEvent) itsLogBrowser.getEvent(getParentPointer());
		}
		
		return itsParent;
	}

	
	public ExternalPointer getParentPointer()
	{
		return new ExternalPointer(getHost(), getThread(), itsParentTimestamp);
	}

	public void setParentTimestamp(long aTimestamp)
	{
		itsParentTimestamp = aTimestamp;
		itsParent = null;
	}

	public IThreadInfo getThread()
	{
		return itsThread;
	}
	
	public void setThread(IThreadInfo aThreadInfo)
	{
		itsThread = aThreadInfo;
	}
	
	public IHostInfo getHost()
	{
		return itsHost;
	}

	public void setHost(IHostInfo aHost)
	{
		itsHost = aHost;
	}

	public long getTimestamp()
	{
		return itsTimestamp;
	}
	
	public void setTimestamp(long aTimestamp)
	{
		itsTimestamp = aTimestamp;
	}
	
	public IBehaviorInfo getOperationBehavior()
	{
		return itsOperationBehavior;
	}

	public void setOperationBehavior(IBehaviorInfo aOperationBehavior)
	{
		itsOperationBehavior = aOperationBehavior;
	}

	public int getOperationBytecodeIndex()
	{
		return itsOperationBytecodeIndex;
	}

	public void setOperationBytecodeIndex(int aOperationBytecodeIndex)
	{
		itsOperationBytecodeIndex = aOperationBytecodeIndex;
	}

	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((itsHost == null) ? 0 : itsHost.hashCode());
		result = PRIME * result + ((itsLogBrowser == null) ? 0 : itsLogBrowser.hashCode());
		result = PRIME * result + ((itsThread == null) ? 0 : itsThread.hashCode());
		result = PRIME * result + (int) (itsTimestamp ^ (itsTimestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Event other = (Event) obj;
		if (itsHost == null)
		{
			if (other.itsHost != null) return false;
		}
		else if (!itsHost.equals(other.itsHost)) return false;
		if (itsLogBrowser == null)
		{
			if (other.itsLogBrowser != null) return false;
		}
		else if (!itsLogBrowser.equals(other.itsLogBrowser)) return false;
		if (itsThread == null)
		{
			if (other.itsThread != null) return false;
		}
		else if (!itsThread.equals(other.itsThread)) return false;
		if (itsTimestamp != other.itsTimestamp) return false;
		return true;
	}
	
	
}
