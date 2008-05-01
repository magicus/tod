/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.core.database.browser;

import java.util.List;

import org.objectweb.asm.Type;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IAdviceInfo;
import tod.core.database.structure.IAspectInfo;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.SourceRange;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.gui.IGUIManager;

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
	public static IBehaviorInfo getBehavior(
			IStructureDatabase aDatabase,
			IClassInfo aClass, 
			String aName, 
			String aSignature, 
			boolean aSearchAncestors)
	{
		ITypeInfo[] theArgumentTypes = getArgumentTypes(aDatabase, aSignature);
		ITypeInfo theReturnType = getReturnType(aDatabase, aSignature);
		
		while (aClass != null)
		{
			IBehaviorInfo theBehavior = aClass.getBehavior(aName, theArgumentTypes, theReturnType);
			if (theBehavior != null) return theBehavior;
			
			if (! aSearchAncestors) return null;
			
			aClass = aClass.getSupertype();
		}

		return null;
	}
	
	/**
	 * Searches a behavior in the given type
	 * @param aSearchAncestors See {@link #getField(ITypeInfo, String, boolean)}.
	 */
	public static IBehaviorInfo getBehavior(
			IStructureDatabase aDatabase,
			String aClassName, 
			String aName, 
			String aSignature, 
			boolean aSearchAncestors)
	{
		IClassInfo theClass = aDatabase.getClass(aClassName, false);
		if (theClass == null) return null;
		else return getBehavior(aDatabase, theClass, aName, aSignature, aSearchAncestors);
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

	/**
	 * Go to the source of the specified location.
	 */
	public static void gotoSource(IGUIManager aGUIManager, ILocationInfo aLocation)
	{
		if (aLocation instanceof IAdviceInfo)
		{
			IAdviceInfo theAdvice = (IAdviceInfo) aLocation;
			aGUIManager.gotoSource(theAdvice.getSourceRange());
		}
		else if (aLocation instanceof IAspectInfo)
		{
			IAspectInfo theAspect = (IAspectInfo) aLocation;
			aGUIManager.gotoSource(new SourceRange(theAspect.getSourceFile(), 1));
		}
		else throw new UnsupportedOperationException(""+aLocation);
	}
	
	
	/**
	 * Returns the probe info of the given event.
	 */
	public static ProbeInfo getProbeInfo(ILogEvent aEvent)
	{
		if (aEvent instanceof ICallerSideEvent)
		{
			ICallerSideEvent theEvent = (ICallerSideEvent) aEvent;
			return theEvent.getProbeInfo();
		}
		else return null;
	}

	/**
	 * Returns the role of the given event.
	 */
	public static BytecodeRole getEventRole(ILogEvent aEvent)
	{
		ProbeInfo theProbeInfo = getProbeInfo(aEvent);
		return theProbeInfo != null ? theProbeInfo.role : null;
	}
	
	/**
	 * Returns all the advice ids corresponding to the specified location, which
	 * can be either an aspect or an advice.
	 */
	public static int[] getAdviceSourceIds(ILocationInfo aLocation)
	{
		if (aLocation instanceof IAspectInfo)
		{
			IAspectInfo theAspect = (IAspectInfo) aLocation;
			List<IAdviceInfo> theAdvices = theAspect.getAdvices();
			int[] theResult = new int[theAdvices.size()];
			int i=0;
			for(IAdviceInfo theAdvice : theAdvices) theResult[i++] = theAdvice.getId();
			return theResult;
		}
		else if (aLocation instanceof IAdviceInfo)
		{
			IAdviceInfo theAdvice = (IAdviceInfo) aLocation;
			return new int[] {theAdvice.getId()};
		}
		else throw new RuntimeException("Not handled: "+aLocation);
	}
}
