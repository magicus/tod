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
package tod.gui.eventlist;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import tod.core.database.structure.IBehaviorInfo.BytecodeRole;

/**
 * Defines the level of intimacy for aspect debugging.
 * @author gpothier
 */
public final class IntimacyLevel
{
	public static final BytecodeRole[] ROLES = {
		BytecodeRole.ADVICE_ARG_SETUP,
		BytecodeRole.ADVICE_EXECUTE,
		BytecodeRole.ADVICE_TEST,
	};
	public static final IntimacyLevel FULL = new IntimacyLevel(ROLES);
	
	public final Set<BytecodeRole> roles;
	
	public IntimacyLevel(BytecodeRole... aRoles)
	{
		roles = new HashSet<BytecodeRole>();
		for (BytecodeRole theRole : aRoles) roles.add(theRole);
	}
	
	public IntimacyLevel(Set<BytecodeRole> aRoles)
	{
		roles = aRoles;
	}



	/**
	 * Whether the given role should be shown in this intimacy level.
	 */
	public boolean showRole(BytecodeRole aRole)
	{
		return roles.contains(aRole);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		IntimacyLevel other = (IntimacyLevel) obj;
		if (roles == null)
		{
			if (other.roles != null) return false;
		}
		else if (!roles.equals(other.roles)) return false;
		return true;
	}

	
	
}
