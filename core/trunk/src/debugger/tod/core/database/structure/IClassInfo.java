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
 * Represents a Java class (interfaces are not currently represented).
 * @author gpothier
 */
public interface IClassInfo extends ITypeInfo
{
	/**
	 * Returns the MD5 checksum of the class' original bytecode.
	 */
	public String getChecksum();
	
	/**
	 * Returns the superclass of this class.
	 */
	public IClassInfo getSupertype();
	
	/**
	 * Sets the superclass of this class.
	 */
	public void setSupertype(IClassInfo aClass);
	
	/**
	 * Returns all the interfaces directly implemented by this class.
	 * The returned list is immutable.
	 */
	public IClassInfo[] getInterfaces();
	
	/**
	 * Sets the list of interfaces directly implemented by this class.
	 */
	public void setInterfaces(IClassInfo[] aInterfaces);

	/**
	 * Searches a field
	 * @param aName Name of the searched field.
	 * @return The field, or null if not found.
	 */
	public IFieldInfo getField(String aName);

	/**
	 * Searches a behavior according to its signature.
	 */
	public IBehaviorInfo getBehavior(String aName, ITypeInfo[] aArgumentTypes);

	/**
	 * Returns all the fields of this class (excluding inherited ones).
	 */
	public Iterable<IFieldInfo> getFields();

	/**
	 * Returns all the behaviors of this class (excluding inherited ones).
	 */
	public Iterable<IBehaviorInfo> getBehaviors();
	
	/**
	 * Creates a clone of this class info object that represents
	 * uncertain information.
	 */
	public IClassInfo createUncertainClone();
	
	/**
	 * Factory method that creates a new field info in this class.
	 */
	public IFieldInfo createField(String aName);
	
	/**
	 * Factory method that creates a new behavior info in this class.
	 */
	public IBehaviorInfo createBehavior(String aName);

}