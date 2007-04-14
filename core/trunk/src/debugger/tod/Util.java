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
package tod;

import java.util.List;

/**
 * @author gpothier
 */
public class Util
{
	public static void ensureSize (List<?> aList, int aSize)
	{
		while (aList.size() <= aSize) aList.add (null);
	}
	
	/**
	 * Retrieves the package name of the given class
	 */
	public static String getPackageName(String aFullyQualifiedName)
	{
		int theIndex = aFullyQualifiedName.lastIndexOf('.');
		if (theIndex == -1) return "";
		else return aFullyQualifiedName.substring(0, theIndex);
	}
	
	/**
	 * Strips the package name from the given class.
	 */
	public static String getSimpleName(String aFullyQualifiedName)
	{
		int theIndex = aFullyQualifiedName.lastIndexOf('.');
		
		String theName = theIndex == -1 ?
				aFullyQualifiedName
				: aFullyQualifiedName.substring(theIndex+1);
		
		return theName.replace('$', '.');
	}
	
	public static String getSimpleInnermostName(String aFullyQualifiedName)
	{
		int theIndex = Math.max(
				aFullyQualifiedName.lastIndexOf('.'),
				aFullyQualifiedName.lastIndexOf('$'));
		
		String theName = theIndex == -1 ?
				aFullyQualifiedName
				: aFullyQualifiedName.substring(theIndex+1);
		
		return theName;
	}
	
	/**
	 * Returns the name of the class,
	 * with the '$' changed to a '.' in the case
	 * of an inner class.
	 */
	public static String getPrettyName(String aFullyQualifiedName)
	{
		return aFullyQualifiedName.replace('$', '.');
	}
}
