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
 * Represents a Java class or interface.
 * @author gpothier
 */
public interface IClassInfo extends ITypeInfo
{
	/**
	 * Sets up the basic information about this class.
	 * This operation can be performed only once (and throws
	 * an exception if it is called more than once).
	 */
	public void setup(
			boolean aIsInterface,
			boolean aIsInScope,
			String aChecksum, 
			IClassInfo[] aInterfaces,
			IClassInfo aSuperclass);
	
	/**
	 * Whether this object represents an interface, or a class.
	 */
	public boolean isInterface();
	
	/**
	 * Whether this class is in the instrumentation scope.
	 */
	public boolean isInScope();
	
	/**
	 * Returns the MD5 checksum of the class' original bytecode.
	 */
	public String getChecksum();
	
	/**
	 * Indicates the time at which this particular version
	 * of the class has been loaded into the system.
	 * This is important for cases where class redefinition is used.
	 * TODO: Consolidate this. We should have a map of all currently connected
	 * VMs with the version of the classes they use.
	 * @return A timestamp, as measured by the debugged VM.
	 */
	public long getStartTime();
	
	/**
	 * Returns the superclass of this class.
	 */
	public IClassInfo getSupertype();
	
	/**
	 * Returns all the interfaces directly implemented by this class.
	 * The returned list is immutable.
	 */
	public IClassInfo[] getInterfaces();
	
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
	 * This method either creates a new uninitialized behavior, or
	 * returns the behavior of the specified name/descriptor.
	 * If the behavior is created it is automatically assigned an id and added
	 * to the database.
	 * @param aDescriptor The descriptor (signature) of the behavior. 
	 * For now this is the ASM-provided descriptor.
	 */
	public IBehaviorInfo getNewBehavior(String aName, String aDescriptor);
	
	/**
	 * This method either creates a new uninitialized field, or 
	 * returns the field that has the specified name.
	 * if the field is created it is automatically assigned an id and added
	 * to the database.
	 */
	public IFieldInfo getNewField(String aName, ITypeInfo aType);


}