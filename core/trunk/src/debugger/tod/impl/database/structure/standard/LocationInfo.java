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

import java.io.Serializable;

import tod.core.database.structure.IMutableLocationInfo;
import tod.core.database.structure.IShareableStructureDatabase;
import zz.utils.PublicCloneable;

/**
 * Base class for aggregation of location information.
 * @author gpothier
 */
public abstract class LocationInfo extends PublicCloneable 
implements IMutableLocationInfo, Serializable
{
	/**
	 * A flag that can be used to check if this location info is local or remote. 
	 */
	private transient Boolean itsOriginal;
	private transient IShareableStructureDatabase itsDatabase;
	private final int itsId;
	private String itsName;
	
	public LocationInfo(IShareableStructureDatabase aDatabase, int aId)
	{
		itsOriginal = true;
		itsDatabase = aDatabase;
		itsId = aId;
	}

	public LocationInfo(IShareableStructureDatabase aDatabase, int aId, String aName)
	{
		itsOriginal = true;
		itsDatabase = aDatabase;
		itsId = aId;
		setName(aName);
	}
	
	/**
	 * Whether this location info is the original.
	 * @return True if original, false if remote version
	 */
	protected boolean isOriginal()
	{
		return itsOriginal != null;
	}
	
	public int getId()
	{
		return itsId;
	}
	
	public String getName()
	{
		return itsName;
	}

	public IShareableStructureDatabase getDatabase()
	{
		return itsDatabase;
	}
	
	public IShareableStructureDatabase _getMutableDatabase()
	{
		return getDatabase();
	}
	
	public void setDatabase(IShareableStructureDatabase aDatabase)
	{
		assert itsDatabase == null;
		itsDatabase = aDatabase;
	}
	
	/**
	 * This is used for defered type registration.
	 */ 
	public void setName(String aName)
	{
		assert itsName == null || itsName.equals(aName);
		itsName = aName;
	}
	
	protected void changeName(String aName)
	{
		itsName = aName;
	}

	@Override
	public final int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((itsDatabase == null) ? 0 : itsDatabase.hashCode());
		result = prime * result + itsId;
		return result;
	}

	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final LocationInfo other = (LocationInfo) obj;
		if (itsDatabase == null)
		{
			if (other.itsDatabase != null) return false;
		}
		else if (!itsDatabase.equals(other.itsDatabase)) return false;
		if (itsId != other.itsId) return false;
		return true;
	}
	
	
}
