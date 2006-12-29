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

import tod.core.ILocationRegistrer;

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
	 * Registers this location to the given registrer.
	 */
	public void register(ILocationRegistrer aRegistrer);
}