/*
 * Created on Oct 13, 2004
 */
package tod.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import tod.Util;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.ArrayTypeInfo;
import tod.core.database.structure.BehaviorInfo;
import tod.core.database.structure.ClassInfo;
import tod.core.database.structure.FieldInfo;
import tod.core.database.structure.PrimitiveTypeInfo;
import tod.core.database.structure.ThreadInfo;
import tod.core.database.structure.TypeInfo;
/**
 * This class permits to register location ids.
 * @author gpothier
 */
public class LocationRegistrer implements ILocationRegistrer, ILocationsRepository
{
	private List<ClassInfo> itsTypes = new ArrayList<ClassInfo>();
	private Map<String, TypeInfo> itsTypesMap = new HashMap<String, TypeInfo>();
	
	private List<String> itsFiles = new ArrayList<String>();
	private List<BehaviorInfo> itsBehaviors = new ArrayList<BehaviorInfo>();
	private List<FieldInfo> itsFields = new ArrayList<FieldInfo>();
	private	Map<Long, ThreadInfo> itsThreads = new HashMap<Long, ThreadInfo>();
	
	public LocationRegistrer()
	{
		registerPrimitiveType("void", 0);
		registerPrimitiveType("boolean", 1);
		registerPrimitiveType("int", 1);
		registerPrimitiveType("long", 2);
		registerPrimitiveType("byte", 1);
		registerPrimitiveType("short", 1);
		registerPrimitiveType("char", 1);
		registerPrimitiveType("double", 2);
		registerPrimitiveType("float", 1);
	}
	
	private void registerPrimitiveType(String aName, int aSize)
	{
		TypeInfo theType = new PrimitiveTypeInfo(this, aName, aSize);
		itsTypesMap.put(aName, theType);
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
			BehaviourKind aBehaviourType, 
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
	
	public void registerThread(long aThreadId, String aName)
	{
		ThreadInfo theThreadInfo = getThread(aThreadId);
		setupThreadInfo(theThreadInfo, aName);
	}
	
	/**
	 * Factory method for thread info.
	 */
	protected ThreadInfo createThreadInfo (long aId)
	{
		return new ThreadInfo (aId);
	}
	
	protected ClassInfo createClassInfo (int aId)
	{
		return new ClassInfo(this, aId);
	}
	
	/**
	 * Factory method for field info
	 */
	protected FieldInfo createFieldInfo(int aId, ClassInfo aTypeInfo, String aName)
	{
		return new FieldInfo(this, aId, aTypeInfo, aName);
	}

	/**
	 * Determines the TOD argument types given a method signature.
	 */
	protected TypeInfo[] getArgumentTypes(String aSignature)
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

	/**
	 * Determines a TOD return type given a method signature
	 */
	protected TypeInfo getReturnType(String aSignature)
	{
		Type theASMReturnType = Type.getReturnType(aSignature);
		return getType(theASMReturnType.getClassName());
	}
	
	/**
	 * Factory method for constructor info.
	 */
	protected BehaviorInfo createBehaviourInfo(
			BehaviourKind aBehaviourType, 
			int aId, 
			ClassInfo aTypeInfo, 
			String aName,
			String aSignature,
			LineNumberInfo[] aLineNumberTable,
			LocalVariableInfo[] aLocalVariableTable)
	{
		
		return new BehaviorInfo(
				this,
				aBehaviourType,
				aId, 
				aTypeInfo,
				aName,
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
	
	protected void setupThreadInfo (ThreadInfo aThreadInfo, String aName)
	{
		aThreadInfo.setName(aName);
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
		if (theDimension == 0) return theType;
		else
		{
			return new ArrayTypeInfo(this, theType, theDimension);
		}
	}
	
	public BehaviorInfo getBehavior (int aId)
	{
		return itsBehaviors.get(aId);
	}
	
	public FieldInfo getField (int aId)
	{
		return itsFields.get(aId);
	}
	
	public ThreadInfo getThread (long aId)
	{
		ThreadInfo theThreadInfo = itsThreads.get(aId);
		if (theThreadInfo == null)
		{
			theThreadInfo = createThreadInfo(aId);
			itsThreads.put (aId, theThreadInfo);
		}
		return theThreadInfo;
	}

	/**
	 * Returns all available classes.
	 */
	public Iterable getClasses()
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
	
	/**
	 * Returns all available threads.
	 */
	public Iterable getThreads()
	{
		return itsThreads.values();
	}
}
