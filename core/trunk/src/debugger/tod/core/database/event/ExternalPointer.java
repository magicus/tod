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
package tod.core.database.event;

import java.io.Serializable;

import tod.core.database.browser.ILogBrowser;
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
	
	@Override
	public String toString()
	{
		return String.format(
				"ExternalPointer [host: %d, thread: %d, ts: %d]", 
				thread.getHost().getId(),
				thread.getId(),
				timestamp);
	}

}

