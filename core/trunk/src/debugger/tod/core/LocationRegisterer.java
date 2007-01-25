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
package tod.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import tod.Util;
import tod.core.ILocationRegisterer.Stats;
import tod.core.database.browser.ILocationStore;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.ArrayTypeInfo;
import tod.core.database.structure.BehaviorInfo;
import tod.core.database.structure.ClassInfo;
import tod.core.database.structure.FieldInfo;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.PrimitiveTypeInfo;
import tod.core.database.structure.TypeInfo;
import tod.core.database.structure.UnknownTypeInfo;
import zz.utils.IteratorsIterator;
import zz.utils.Utils;
/**
 * This class permits to register location ids.
 * @author gpothier
 */
public class LocationRegisterer implements ILocationStore
{
	private List<ClassInfo> itsTypes = new ArrayList<ClassInfo>();
	private List<UnknownTypeInfo> itsUnknownTypes = new ArrayList<UnknownTypeInfo>();
	private Map<String, TypeInfo> itsTypesMap = new HashMap<String, TypeInfo>();
	
	private List<String> itsFiles = new ArrayList<String>();
	private List<BehaviorInfo> itsBehaviors = new ArrayList<BehaviorInfo>();
	private List<FieldInfo> itsFields = new ArrayList<FieldInfo>();
	
	public LocationRegisterer()
	{
		for (PrimitiveTypeInfo thePrimitiveType : PrimitiveTypeInfo.TYPES)
		{
			itsTypesMap.put(thePrimitiveType.getName(), thePrimitiveType);
		}
	}
	
	/**
	 * For testing only.
	 * Clears all registered info.
	 */
    public void _clear()
    {
    	itsBehaviors.clear();
    	itsFields.clear();
    	itsFiles.clear();
    	itsTypes.clear();
    	itsTypesMap.clear();
    	itsUnknownTypes.clear();
    }

	
	public void registerFile(int aFileId, String aFileName)
	{
		Util.ensureSize(itsFiles, aFileId);
		itsFiles.set(aFileId, aFileName);
	}

	public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
	{
		ClassInfo theClass = getClass(aTypeId); // this method creates the type if it doesn't exist yet.
		setupClassInfo(theClass, aTypeName, aSupertypeId, aInterfaceIds);
		
		itsTypesMap.put(aTypeName, theClass);
	}

	public void registerBehavior(
			BehaviorKind aBehaviourType, 
			int aBehaviourId, 
			int aTypeId, 
			String aBehaviourName,
			String aSignature)
	{
		Util.ensureSize(itsBehaviors, aBehaviourId);
		ClassInfo theType = getClass(aTypeId);
		
		BehaviorInfo theBehaviourInfo = createBehaviourInfo(
				aBehaviourType, 
				aBehaviourId, 
				theType, 
				aBehaviourName,
				aSignature,
				null,
				null);
		
		itsBehaviors.set (aBehaviourId, theBehaviourInfo);
		theType.register(theBehaviourInfo);
	}

	public void registerBehaviorAttributes(
			int aBehaviourId, 
			LineNumberInfo[] aLineNumberTable, 
			LocalVariableInfo[] aLocalVariableTable)
	{
		BehaviorInfo theBehavior = getBehavior(aBehaviourId);
		theBehavior.setAttributes(aLineNumberTable, aLocalVariableTable);
	}
	
	public void registerField(int aFieldId, int aTypeId, String aFieldName)
	{
		Util.ensureSize(itsFields, aFieldId);
		ClassInfo theType = getClass(aTypeId);
		FieldInfo theFieldInfo = createFieldInfo(aFieldId, theType, aFieldName);
		itsFields.set (aFieldId, theFieldInfo);
		theType.register(theFieldInfo);
	}
	
	protected ClassInfo createClassInfo (int aId)
	{
		return new ClassInfo(aId);
	}
	
	/**
	 * Factory method for field info
	 */
	protected FieldInfo createFieldInfo(int aId, ClassInfo aTypeInfo, String aName)
	{
		return new FieldInfo(aId, aTypeInfo, aName);
	}

	public TypeInfo[] getArgumentTypes(String aSignature)
	{
		Type[] theASMArgumentTypes = Type.getArgumentTypes(aSignature);
		TypeInfo[] theArgumentTypes = new TypeInfo[theASMArgumentTypes.length];
		
		for (int i = 0; i < theASMArgumentTypes.length; i++)
		{
			Type theASMType = theASMArgumentTypes[i];
			theArgumentTypes[i] = getType(theASMType.getClassName());
		}
		
		return theArgumentTypes;
	}

	public TypeInfo getReturnType(String aSignature)
	{
		Type theASMReturnType = Type.getReturnType(aSignature);
		return getType(theASMReturnType.getClassName());
	}
	
	/**
	 * Factory method for constructor info.
	 */
	protected BehaviorInfo createBehaviourInfo(
			BehaviorKind aBehaviourType, 
			int aId, 
			ClassInfo aTypeInfo, 
			String aName,
			String aSignature,
			LineNumberInfo[] aLineNumberTable,
			LocalVariableInfo[] aLocalVariableTable)
	{
		
		return new BehaviorInfo(
				aBehaviourType,
				aId, 
				aTypeInfo,
				aName,
				aSignature,
				getArgumentTypes(aSignature),
				getReturnType(aSignature),
				aLineNumberTable,
				aLocalVariableTable);
	}

	/**
	 * Factory method for type info.
	 */
	protected void setupClassInfo(ClassInfo aClass, String aName, int aSupertypeId, int[] aInterfaceIds)
	{
		ClassInfo theSupertype = aSupertypeId >= 0 ? getClass(aSupertypeId) : null;
		
		ClassInfo[] theInterfaces = null;
		if (aInterfaceIds != null)
		{
			theInterfaces = new ClassInfo[aInterfaceIds.length];
			for (int i = 0; i < aInterfaceIds.length; i++)
			{
				int theId = aInterfaceIds[i];
				theInterfaces[i] = getClass(theId);
			}
		}
		
		aClass.setName(aName);
		aClass.setSupertype(theSupertype);
		aClass.setInterfaces(theInterfaces);
	}
	

	/**
	 * Returns the type info that corresponds to the specified id.
	 * If the type has not been registered yet, it is created and pre-registered.
	 */
	public ClassInfo getClass (int aId)
	{
		Util.ensureSize(itsTypes, aId);
		ClassInfo theClassInfo = itsTypes.get(aId);
		if (theClassInfo == null)
		{
			theClassInfo = createClassInfo(aId);
			itsTypes.set(aId, theClassInfo);
		}

		return theClassInfo;
	}
	
	private synchronized UnknownTypeInfo registerUnknownType(String aName)
	{
		UnknownTypeInfo theType = new UnknownTypeInfo(-itsUnknownTypes.size()-1, aName);
		itsUnknownTypes.add(theType);
		return theType;
	}

	/**
	 * Returns the type object that corresponds to the given name.
	 */
	public TypeInfo getType(String aName)
	{
		int theDimension = 0;
		while (aName.endsWith("[]"))
		{
			aName = aName.substring(0, aName.length()-2);
			theDimension++;
		}
		
		TypeInfo theType = itsTypesMap.get(aName);
		if (theType == null) theType = registerUnknownType(aName);
		
		if (theDimension == 0) return theType;
		else
		{
			return new ArrayTypeInfo(theType, theDimension);
		}
	}
	
	public BehaviorInfo getBehavior (int aId)
	{
		if (aId == -1)
		{
//			System.err.println("[LocationRegisterer] Warning: requested behavior id -1");
			return null;
		}
		
		return itsBehaviors.get(aId);
	}
	
	public FieldInfo getField (int aId)
	{
		return itsFields.get(aId);
	}
	
	public ITypeInfo getType(int aId)
	{
		if (aId < -1) return itsUnknownTypes.get(-aId-1);
		else return itsTypes.get(aId);
	}
	
	/**
	 * Returns all available classes.
	 */
	public Iterable getTypes()
	{
		return itsTypes;
	}
	
	/**
	 * Returns all available behaviours.
	 */
	public Iterable getBehaviours()
	{
		return itsBehaviors;
	}
	
	/**
	 * Returns all available fields.
	 */
	public Iterable getFields()
	{
		return itsFields;
	}
	
	/**
	 * Returns all available files.
	 */
	public Iterable<String> getFiles()
	{
		return itsFiles;
	}
	
	public IBehaviorInfo getBehavior(
			ITypeInfo aType, 
			String aName, 
			String aSignature, 
			boolean aSearchAncestors)
	{
		ClassInfo theClassInfo = (ClassInfo) aType;
		ITypeInfo[] theArgumentTypes = getArgumentTypes(aSignature);
		
		while (theClassInfo != null)
		{
			IBehaviorInfo theBehavior = theClassInfo.getBehavior(aName, theArgumentTypes);
			if (theBehavior != null) return theBehavior;
			
			if (! aSearchAncestors) return null;
			
			theClassInfo = theClassInfo.getSupertype();
		}

		return null;
	}

	public IFieldInfo getField(
			ITypeInfo aType, 
			String aName,
			boolean aSearchAncestors)
	{
		ClassInfo theClassInfo = (ClassInfo) aType;
		
		while (theClassInfo != null)
		{
			IFieldInfo theField = theClassInfo.getField(aName);
			if (theField != null) return theField;
			
			if (! aSearchAncestors) return null;
			
			theClassInfo = theClassInfo.getSupertype();
		}

		return null;
	}
	
	public Iterable<ILocationInfo> getLocations()
	{
		List<ILocationInfo> theLocations = new ArrayList<ILocationInfo>();
		
		for(ITypeInfo theType : itsTypes) 
			if (theType != null) theLocations.add(theType);
		
		for(IFieldInfo theField : itsFields)
			if (theField != null) theLocations.add(theField);
		
		for(IBehaviorInfo theBehavior : itsBehaviors) 
			if (theBehavior != null) theLocations.add(theBehavior);
		
		return theLocations;
	}

	public Stats getStats()
	{
		return new Stats(itsTypes.size(), itsBehaviors.size(), itsFields.size());
	}

	/**
	 * Returns a new synchronized view of the given registrer.
	 */
	public static ILocationRegisterer createSynchronizedRegistrer(ILocationRegisterer aRegistrer)
	{
		return new SynchronizedRegisterer(aRegistrer);
	}
	
	/**
	 * A wrapper that synchronizes calls to the registering methods.
	 * @author gpothier
	 */
	private static class SynchronizedRegisterer implements ILocationRegisterer
	{
		private ILocationRegisterer itsDelegate;

		private SynchronizedRegisterer(ILocationRegisterer aDelegate)
		{
			itsDelegate = aDelegate;
		}

		public synchronized void registerBehavior(BehaviorKind aBehaviourType, int aBehaviourId, int aTypeId,
				String aBehaviourName, String aSignature)
		{
			itsDelegate.registerBehavior(aBehaviourType, aBehaviourId, aTypeId, aBehaviourName, aSignature);
		}

		public synchronized void registerBehaviorAttributes(int aBehaviourId, LineNumberInfo[] aLineNumberTable,
				LocalVariableInfo[] aLocalVariableTable)
		{
			itsDelegate.registerBehaviorAttributes(aBehaviourId, aLineNumberTable, aLocalVariableTable);
		}

		public synchronized void registerField(int aFieldId, int aTypeId, String aFieldName)
		{
			itsDelegate.registerField(aFieldId, aTypeId, aFieldName);
		}

		public synchronized void registerFile(int aFileId, String aFileName)
		{
			itsDelegate.registerFile(aFileId, aFileName);
		}

		public synchronized void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
		{
			itsDelegate.registerType(aTypeId, aTypeName, aSupertypeId, aInterfaceIds);
		}
		
	}
	
}
