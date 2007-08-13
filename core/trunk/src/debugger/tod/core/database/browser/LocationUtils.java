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

import org.objectweb.asm.Type;

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationsRepository;
import tod.core.database.structure.ITypeInfo;
import tod.impl.database.structure.standard.TypeInfo;

/**
 * Utilities related to {@link ILocationsRepository}
 * @author gpothier
 */
public class LocationUtils
{
	/**
	 * Returns the argument types that correspond to the given behavior signature. 
	 */
	public static ITypeInfo[] getArgumentTypes(
			ILocationsRepository aRepository,
			String aSignature)
	{
		Type[] theASMArgumentTypes = Type.getArgumentTypes(aSignature);
		ITypeInfo[] theArgumentTypes = new ITypeInfo[theASMArgumentTypes.length];
		
		for (int i = 0; i < theASMArgumentTypes.length; i++)
		{
			Type theASMType = theASMArgumentTypes[i];
			theArgumentTypes[i] = aRepository.getType(theASMType.getClassName());
		}
		
		return theArgumentTypes;
	}


	/**
	 * Determines a TOD return type given a method signature
	 */
	public static ITypeInfo getReturnType(
			ILocationsRepository aRepository,
			String aSignature)
	{
		Type theASMReturnType = Type.getReturnType(aSignature);
		return aRepository.getType(theASMReturnType.getClassName());
	}
	
	

	
	/**
	 * Retrieves a field given a type and a name.
	 * @param aSearchAncestors If false, the field will be searched only in the
	 * specified type. If true, the field will also be searched in ancestors. In the case
	 * of private fields, the first (closest to specified type) matching field is returned. 
	 */
	public static IFieldInfo getField(
			ITypeInfo aType, 
			String aName, 
			boolean aSearchAncestors)
	{
		IClassInfo theClassInfo = (IClassInfo) aType;
		
		while (theClassInfo != null)
		{
			IFieldInfo theField = theClassInfo.getField(aName);
			if (theField != null) return theField;
			
			if (! aSearchAncestors) return null;
			
			theClassInfo = theClassInfo.getSupertype();
		}

		return null;
	}
	
	/**
	 * Searches a behavior in the given type
	 * @param aSearchAncestors See {@link #getField(ITypeInfo, String, boolean)}.
	 */
	public IBehaviorInfo getBehavior(
			ILocationsRepository aRepository,
			ITypeInfo aType, 
			String aName, 
			String aSignature, 
			boolean aSearchAncestors)
	{
		IClassInfo theClassInfo = (IClassInfo) aType;
		ITypeInfo[] theArgumentTypes = getArgumentTypes(aRepository, aSignature);
		
		while (theClassInfo != null)
		{
			IBehaviorInfo theBehavior = theClassInfo.getBehavior(aName, theArgumentTypes);
			if (theBehavior != null) return theBehavior;
			
			if (! aSearchAncestors) return null;
			
			theClassInfo = theClassInfo.getSupertype();
		}

		return null;

	}
	


}
