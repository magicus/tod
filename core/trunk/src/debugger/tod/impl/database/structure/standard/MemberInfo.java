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
package tod.impl.database.structure.standard;

import tod.core.ILogCollector;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.ILocationInfo.ISerializableLocationInfo;


/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a type member (method, constructor, field).
 * @author gpothier
 */
public abstract class MemberInfo extends LocationInfo 
implements IMemberInfo, ISerializableLocationInfo
{
	private IClassInfo itsType;
	
	public MemberInfo(StructureDatabase aDatabase, int aId, IClassInfo aTypeInfo, String aName)
	{
		super(aDatabase, aId, aName);
		itsType = aTypeInfo;
	}
	
	public IClassInfo getType()
	{
		return itsType;
	}	
}
