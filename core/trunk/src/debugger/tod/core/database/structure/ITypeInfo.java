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
 * Represents a Java type (class, interface, primitive type).
 * @author gpothier
 */
public interface ITypeInfo extends ILocationInfo
{
	/**
	 * Returns the number of JVM stack slots that an object of
	 * this type occupies.
	 * For instance, object reference is 1, long and double are 2, void is 0.
	 */
	public int getSize();

	/**
	 * Indicates if ths type is a primitive type.
	 */
	public boolean isPrimitive();

	/**
	 * Indicates if ths type is an array type.
	 */
	public boolean isArray();

	/**
	 * Indicates if ths type is the void type.
	 */
	public boolean isVoid();
	
	/**
	 * Creates a clone of this type info object that represents
	 * uncertain information.
	 */
	public IClassInfo createUncertainClone();
	

}