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
 * The structure database contains static information about the
 * debugged program. In particular, it contains a list of types
 * and behaviors.
 * The structure database is thightly coupled with the instrumentation:
 * ids of types and behaviors are hardcoded in the instrumented code
 * and must match those used in the currently used structure database.
 * Therefore the structure database has a unique identifier, which is sent
 * to the native agent during setup, so that the agent can ensure that cached
 * versions of instrumented classes match the structure database.
 * <p>
 * It is possible for the database to contain various classes with the same name.
 * This can occurr for three reasons:
 * <li>Several classes with the same name but different contents exist in the
 * codebase and are loaded by different class loaders (this is both improbable
 * and quite risky for the developper...)
 * <li>A class is modified between different sessions, ie. the developper changes
 * the source code of the class and relaunches the program. In this case the 
 * old class info can be deleted.
 * <li>A class is redefined during a given session (edit & continue). This
 * is more problematic, because it is necessary to keep all the versions.
 * Although the structure of the class should not change (at least in current JDK),
 * the content of behaviors can change.  
 * @author gpothier
 */
public interface IStructureDatabase
{
	/**
	 * Returns the unique identifier of this database. 
	 */
	public String getId();
	
	/**
	 * Returns the information for a class of the given name and checksum,
	 * or null if not found.
	 */
	public IClassInfo getClass(String aName, String aChecksum);
	
	/**
	 * Returns all the class info that have the specified name.
	 * It is possible to have several classes with the same name in case
	 * of class redefinition, or classloader hacking.
	 */
	public IClassInfo[] getClasses(String aName);
	
	/**
	 * Factory method that creates a new class info object.
	 * The object is automatically added to the database.
	 */
	public IClassInfo createClass(String aName);
	
}
