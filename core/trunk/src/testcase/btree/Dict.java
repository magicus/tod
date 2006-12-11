/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package btree;
/*
 * Created on May 26, 2006
 */

/**
 * Interface of a simple dictionary
 */
public interface Dict
{
	/**
	 * Retrieve the value associated with a given key.
	 * @return The value associated with the key, or null if not found.
	 */
	public Long get(long aKey);

	/**
	 * Sets the value associated with a given key.
	 * @return True if the key is added, false if it was updated
	 */
	public boolean put(long aKey, long aValue);

	/**
	 * Removes any association with the given key.
	 * @return True if the key was removed, false if it was not present
	 */
	public boolean remove(long aKey);
}