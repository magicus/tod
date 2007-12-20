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
import zz.utils.PublicCloneable;

/**
 * Base class for aggregation of location information.
 * @author gpothier
 */
public abstract class LocationInfo extends PublicCloneable 
implements IMutableLocationInfo, Serializable
{
	private transient IMutableStructureDatabase itsDatabase;
	private final int itsId;
	private String itsName;
	
	public LocationInfo(IMutableStructureDatabase aDatabase, int aId)
	{
		itsDatabase = aDatabase;
		itsId = aId;
	}

	public LocationInfo(IMutableStructureDatabase aDatabase, int aId, String aName)
	{
		itsDatabase = aDatabase;
		itsId = aId;
		setName(aName);
	}
	
	public int getId()
	{
		return itsId;
	}
	
	public String getName()
	{
		return itsName;
	}

	public IMutableStructureDatabase getDatabase()
	{
		return itsDatabase;
	}
	
	public void setDatabase(IMutableStructureDatabase aDatabase)
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
