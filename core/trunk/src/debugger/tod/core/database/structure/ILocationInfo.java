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
package tod.core.database.structure;

import java.io.Serializable;


/**
 * Base interface for location info (ie structural information). 
 * Locations can be types, fields or methods
 * @author gpothier
 */
public interface ILocationInfo
{
	/**
	 * Returns the id of this location.
	 */
	public int getId();

	public String getName();
	
	/**
	 * Returns the database that owns this info.
	 */
	public IStructureDatabase getDatabase();
	
	/**
	 * Interface for location info implementations that are serializable.
	 * Such implementation should have their reference to the owner database
	 * transient, so that upon arriving at a new location they can be bound
	 * to a local database.
	 * @author gpothier
	 */
	public interface ISerializableLocationInfo extends Serializable
	{
		public void setDatabase(IMutableStructureDatabase aDatabase);
	}

}