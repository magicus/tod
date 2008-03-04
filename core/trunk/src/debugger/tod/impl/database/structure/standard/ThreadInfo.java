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
package tod.impl.database.structure.standard;

import java.io.Serializable;

import tod.core.ILogCollector;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;


/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a thread.
 * @author gpothier
 */
public class ThreadInfo implements IThreadInfo, Serializable
{
	private IHostInfo itsHost;
	private int itsId;
	private long itsJVMId;
	private String itsName;
	
	public ThreadInfo(IHostInfo aHost, int aId, long aJVMId, String aName)
	{
		itsHost = aHost;
		itsId = aId;
		itsJVMId = aJVMId;
		itsName = aName;
	}

	public int getId()
	{
		return itsId;
	}
	
	public long getJVMId()
	{
		return itsJVMId;
	}

	public IHostInfo getHost()
	{
		return itsHost;
	}

	public String getName()
	{
		return itsName;
	}

	public void setName(String aName)
	{
		itsName = aName;
	}
	
	public String getDescription()
	{
		return getId()+" ["+getName()+"]";
	}
	
	@Override
	public String toString()
	{
		return "Thread ("+getId()+", "+getJVMId()+", "+getName()+")";
	}

	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((itsHost == null) ? 0 : itsHost.hashCode());
		result = PRIME * result + itsId;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final ThreadInfo other = (ThreadInfo) obj;
		if (itsHost == null)
		{
			if (other.itsHost != null) return false;
		}
		else if (!itsHost.equals(other.itsHost)) return false;
		if (itsId != other.itsId) return false;
		return true;
	}

	
	

}
