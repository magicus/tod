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

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationsRepository;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.SourceRange;
import tod.gui.IGUIManager;
import tod.utils.TODUtils;

/**
 * Utilities related to {@link IStructureDatabase}
 * @author gpothier
 */
public class LocationUtils
{
	/**
	 * Returns the argument types that correspond to the given behavior signature. 
	 */
	public static ITypeInfo[] getArgumentTypes(
			IStructureDatabase aDatabase,
			String aSignature)
	{
		Type[] theASMArgumentTypes = Type.getArgumentTypes(aSignature);
		ITypeInfo[] theArgumentTypes = new ITypeInfo[theASMArgumentTypes.length];
		
		for (int i = 0; i < theASMArgumentTypes.length; i++)
		{
			Type theASMType = theASMArgumentTypes[i];
			theArgumentTypes[i] = aDatabase.getType(theASMType.getDescriptor(), true);
		}
		
		return theArgumentTypes;
	}

	/**
	 * Returns the argument types that correspond to the given behavior signature. 
	 * If some type is not found in the database it is created.
	 */
	public static ITypeInfo[] getArgumentTypes(
			IMutableStructureDatabase aDatabase,
			String aSignature)
	{
		Type[] theASMArgumentTypes = Type.getArgumentTypes(aSignature);
		ITypeInfo[] theArgumentTypes = new ITypeInfo[theASMArgumentTypes.length];
		
		for (int i = 0; i < theASMArgumentTypes.length; i++)
		{
			Type theASMType = theASMArgumentTypes[i];
			theArgumentTypes[i] = aDatabase.getNewType(theASMType.getDescriptor());
					
		}
		
		return theArgumentTypes;
	}
	

	/**
	 * Determines a TOD return type given a method signature
	 */
	public static ITypeInfo getReturnType(
			IStructureDatabase aDatabase,
			String aSignature)
	{
		Type theASMType = Type.getReturnType(aSignature);
		return aDatabase.getType(theASMType.getDescriptor(), true);
	}
	
	/**
	 * Determines a TOD return type given a method signature
	 * If some type is not found in the database it is created.
	 */
	public static ITypeInfo getReturnType(
			IMutableStructureDatabase aDatabase,
			String aSignature)
	{
		Type theASMType = Type.getReturnType(aSignature);
		return aDatabase.getNewType(theASMType.getDescriptor());
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
			IStructureDatabase aDatabase,
			IClassInfo aClass, 
			String aName, 
			String aSignature, 
			boolean aSearchAncestors)
	{
		ITypeInfo[] theArgumentTypes = getArgumentTypes(aDatabase, aSignature);
		
		while (aClass != null)
		{
			IBehaviorInfo theBehavior = aClass.getBehavior(aName, theArgumentTypes);
			if (theBehavior != null) return theBehavior;
			
			if (! aSearchAncestors) return null;
			
			aClass = aClass.getSupertype();
		}

		return null;

	}
	
	/**
	 * Returns the JVM signature of the given behavior.
	 * eg (Ljava/lang/Object;I)V
	 */
	public static String getDescriptor(IBehaviorInfo aBehavior)
	{
		StringBuilder theBuilder = new StringBuilder("(");
		
		for (ITypeInfo theType : aBehavior.getArgumentTypes())
		{
			theBuilder.append(theType.getJvmName());
		}

		theBuilder.append(')');
		theBuilder.append(aBehavior.getReturnType().getJvmName());
		
		return theBuilder.toString();
	}

	/**
	 * Returns the source range corresponding to the given event.
	 */
	public static SourceRange getSourceRange (ILogEvent aEvent)
	{
		if (aEvent instanceof ICallerSideEvent)
		{
			ICallerSideEvent theEvent = (ICallerSideEvent) aEvent;
			IBehaviorCallEvent theParent = theEvent.getParent();
		    if (theParent == null) return null;
		    
		    int theBytecodeIndex = theEvent.getOperationBytecodeIndex();
		    IBehaviorInfo theBehavior = theParent.getExecutedBehavior();
		    if (theBehavior == null) return null;
		    
		    int theLineNumber = theBehavior.getLineNumber(theBytecodeIndex);
		    ITypeInfo theType = theBehavior.getType();
		    
		    String theTypeName = theType.getName();
		    return new SourceRange(theTypeName, theLineNumber);
		}
		else return null;
	}
	
	/**
	 * Tries to show the source code for the given event in the gui manager.
	 */
	public static void gotoSource(IGUIManager aGUIManager, ILogEvent aEvent)
	{
		SourceRange theSourceRange = LocationUtils.getSourceRange(aEvent);
		if (theSourceRange != null) aGUIManager.gotoSource(theSourceRange);
	}


}
