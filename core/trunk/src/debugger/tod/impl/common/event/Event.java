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
package tod.impl.common.event;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ExternalPointer;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import zz.utils.Utils;

/**
 * Base class of all logged events.
 * @author gpothier
 */
public abstract class Event implements ICallerSideEvent
{
	private ILogBrowser itsLogBrowser;
	
	private long itsTimestamp;
	
	private IThreadInfo itsThread;

	private int itsProbeId;
	private int[] itsAdviceCFlow;
	
	private long itsParentTimestamp;
	private BehaviorCallEvent itsParent;
	
	private int itsDepth;
	
	public Event(ILogBrowser aLogBrowser)
	{
		itsLogBrowser = aLogBrowser;
	}

	public ILogBrowser getLogBrowser()
	{
		return itsLogBrowser;
	}

	public ExternalPointer getPointer()
	{
		return new ExternalPointer(getThread(), getTimestamp());
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
		return new ExternalPointer(getThread(), itsParentTimestamp);
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
		IThreadInfo theThread = getThread();
		return theThread != null ? theThread.getHost() : null;
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
		ProbeInfo theProbeInfo = getProbeInfo();
		return theProbeInfo != null ? 
				itsLogBrowser.getStructureDatabase().getBehavior(theProbeInfo.behaviorId, true)
				: null;
	}

	public int getOperationBytecodeIndex()
	{
		ProbeInfo theProbeInfo = getProbeInfo();
		return theProbeInfo != null ? 
				theProbeInfo.bytecodeIndex
				: -1;
	}

	public int getAdviceSourceId()
	{
		ProbeInfo theProbeInfo = getProbeInfo();
		return theProbeInfo != null ? 
				theProbeInfo.adviceSourceId
				: -1;
	}

	public ProbeInfo getProbeInfo()
	{
		return getProbeId() > 0 ? itsLogBrowser.getStructureDatabase().getProbeInfo(getProbeId()) : null;
	}
	
	public int getProbeId()
	{
		return itsProbeId;
	}

	public void setProbeId(int aProbeId)
	{
		itsProbeId = aProbeId;
	}

	public int[] getAdviceCFlow()
	{
		return itsAdviceCFlow;
	}

	public void setAdviceCFlow(int[] aAdviceCFlow)
	{
		itsAdviceCFlow = aAdviceCFlow;
	}

	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((itsLogBrowser == null) ? 0 : itsLogBrowser.hashCode());
		result = PRIME * result + ((itsThread == null) ? 0 : itsThread.hashCode());
		result = PRIME * result + (int) (itsTimestamp ^ (itsTimestamp >>> 32));
		return result;
	}

	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		Event other = (Event) obj;
		if (Utils.different(itsLogBrowser, other.itsLogBrowser)) return false;
		if (Utils.different(itsThread, other.itsThread)) return false;
		if (itsTimestamp != other.itsTimestamp) return false;

		return true;
	}

	@Override
	public String toString()
	{
		return String.format(
				"Event [kind: %s, host: %s, thread: %s, depth: %d]",
				getClass().getSimpleName(),
				getHost(),
				getThread(),
				getDepth());
	}
}
