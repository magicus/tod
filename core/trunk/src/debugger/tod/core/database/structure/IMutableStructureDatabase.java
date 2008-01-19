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

/**
 * Writable extension of {@link IStructureDatabase}.
 * @author gpothier
 */
public interface IMutableStructureDatabase extends IStructureDatabase
{
	/**
	 * This method either creates a new uninitialized class, or
	 * returns the latest added class of the specified name.
	 * If the class is created it is automatically assigned an id and added
	 * to the database.
	 */
	public IMutableClassInfo getNewClass(String aName);
	
	/**
	 * Same as {@link #getType(String, boolean)}, but if the type is a class and
	 * does not exist, it is created as by {@link #getNewClass(String)}.
	 */
	public ITypeInfo getNewType(String aName);
	
	/**
	 * Override so as to provide mutable version.
	 */
	public IMutableClassInfo getClass(String aName, boolean aFailIfAbsent);
	
	/**
	 * Override so as to provide mutable version.
	 */
	public IMutableClassInfo getClass(int aId, boolean aFailIfAbsent);

	/**
	 * Creates a new probe and returns its id. 
	 */
	public int addProbe(int aBehaviorId, int aBytecodeIndex, int aAdviceSourceId);
	
	/**
	 * Changes the probe info for the given id.
	 */
	public void setProbe(int aProbeId, int aBehaviorId, int aBytecodeIndex, int aAdviceSourceId);
}
