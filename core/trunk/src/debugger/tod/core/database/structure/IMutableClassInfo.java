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

import java.util.Map;

/**
 * Represents an in-construction {@link IClassInfo}.
 * @author gpothier
 */
public interface IMutableClassInfo extends IClassInfo, IMutableLocationInfo
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
	 * Sets up the bytecode information of this class.
	 */
	public void setBytecode(byte[] aBytecode);
	
	/**
	 * This method either creates a new uninitialized behavior, or
	 * returns the behavior of the specified name/descriptor.
	 * If the behavior is created it is automatically assigned an id and added
	 * to the database.
	 * @param aDescriptor The descriptor (signature) of the behavior. 
	 * For now this is the ASM-provided descriptor.
	 */
	public IMutableBehaviorInfo getNewBehavior(String aName, String aDescriptor);
	
	/**
	 * This method either creates a new uninitialized field, or 
	 * returns the field that has the specified name.
	 * if the field is created it is automatically assigned an id and added
	 * to the database.
	 */
	public IMutableFieldInfo getNewField(String aName, ITypeInfo aType);

}
