/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.agent;


/**
 * Provides a helper method that retrieves the id
 * of an object.
 * @author gpothier
 */
public class ObjectIdentity
{
	/**
	 * Retrieves the identifier of an object.
	 * Returns a positive value if the object was already tagged.
	 * If this call causes the object to be tagged, the opposite of 
	 * the actual tag value is returned.
	 */
	public static native long get (Object aObject);
}
