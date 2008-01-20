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

import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IMutableLocationInfo;
import tod.core.database.structure.IMutableStructureDatabase;
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
	
	/**
	 * Two location info are equal if they have the same class and
	 * the same id.
	 */
	@Override
	public final boolean equals(Object aObj)
	{
		if (aObj instanceof LocationInfo)
		{
			ILocationInfo theInfo = (ILocationInfo) aObj;
			return theInfo.getClass().equals(getClass()) 
					&& theInfo.getId() == getId();
		}
		else return false;
	}
	
}
