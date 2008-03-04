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
package tod.core.database.structure;

import java.io.Serializable;

import tod.agent.AgentConfig;
import tod.core.DebugFlags;

/**
 * Permits to identify an object.
 * There are two identification schemes:
 * <li>Instances of classes that were elegible to the 
 * {@link reflex.lib.logging.core.impl.mop.identification.ObjectIdentifier} 
 * mechanism have a truly unique identifier; they are represented
 * by the {@link ObjectUID} inner class.
 * <li>Instances of other classes are represented by a 
 * {@link ObjectHash}, which provides only a hint to a object's identity,
 * as several objects can have the same hash code.
 * @author gpothier
 */
public class ObjectId implements Serializable
{
	private static final long serialVersionUID = 8201251692076120987L;

	private long itsId;
	
	public ObjectId(long aId)
	{
		itsId = aId;
	}
	
	public long getId()
	{
		return itsId;
	}
	
	/**
	 * Returns a human-readable description of this object id.
	 */
	public String getDescription()
	{
		return DebugFlags.IGNORE_HOST ?
				"" +getObjectId(itsId)
				: getObjectId(itsId) +"." +getHostId(itsId);
	}
	
	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (int) (itsId ^ (itsId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ObjectId other = (ObjectId) obj;
		if (itsId != other.itsId) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return DebugFlags.IGNORE_HOST ?
				"UID: " +getObjectId(itsId)
				: "UID: " +getObjectId(itsId) +"." +getHostId(itsId);
	}
	
	/**
	 * Returns the intra-host object id for the given object id.
	 * See bci-agent.
	 */
	public static long getObjectId(long aId)
	{
		return DebugFlags.IGNORE_HOST ? aId : aId >>> AgentConfig.HOST_BITS;
	}
	
	/**
	 * Returns the host id for the given object id.
	 * See bci-agent.
	 */
	public static int getHostId(long aId)
	{
		return DebugFlags.IGNORE_HOST ? 0 : (int) (aId & AgentConfig.HOST_MASK);
	}


}

