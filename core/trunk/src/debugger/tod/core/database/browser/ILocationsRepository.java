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
package tod.core.database.browser;

import tod.core.ILocationRegistrer.Stats;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.TypeInfo;

/**
 * Permits to obtain the location info objects that have been registered during a 
 * {@link tod.core.session.ISession}
 * @author gpothier
 */
public interface ILocationsRepository
{
	/**
	 * Retrieves a type given its id.
	 */
	public ITypeInfo getType(int aId);
	
	/**
	 * Returns the type object that corresponds to the given name.
	 */
	public ITypeInfo getType(String aName);
	
	/**
	 * Retrieves a field given its id.
	 */
	public IFieldInfo getField(int aFieldId);
	
	/**
	 * Retrieves a field given a type and a name.
	 * @param aSearchAncestors If false, the field will be searched only in the
	 * specified type. If true, the field will also be searched in ancestors. In the case
	 * of private fields, the first (closest to specified type) matching field is returned. 
	 */
	public IFieldInfo getField(ITypeInfo aType, String aName, boolean aSearchAncestors);

	/**
	 * Retrieves a behavior given its id.
	 */
	public IBehaviorInfo getBehavior(int aBehaviorId);
	
	/**
	 * Searches a behavior in the given type
	 * @param aSearchAncestors See {@link #getField(ITypeInfo, String, boolean)}.
	 */
	public IBehaviorInfo getBehavior(
			ITypeInfo aType, 
			String aName, 
			String aSignature, 
			boolean aSearchAncestors);
	
	/**
	 * Returns the argument types that correspond to the given behavior signature. 
	 */
	public ITypeInfo[] getArgumentTypes(String aSignature);

	/**
	 * Returns all registered types.
	 */
	public Iterable<IClassInfo> getClasses();
	
	/**
	 * Returns all registered behaviours.
	 */
	public Iterable<IBehaviorInfo> getBehaviours();
	
	/**
	 * Returns all registered fields.
	 */
	public Iterable<IFieldInfo> getFields();
	
	/**
	 * Returns all registered files.
	 */
	public Iterable<String> getFiles();
	
	/**
	 * Returns statistics about registered locations
	 */
	public Stats getStats();
	

	
}
