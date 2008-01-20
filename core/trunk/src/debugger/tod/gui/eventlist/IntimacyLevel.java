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
