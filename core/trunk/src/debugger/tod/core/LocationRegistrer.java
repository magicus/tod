/*
 * Created on Oct 13, 2004
 */
package tod.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reflex.lib.logging.Util;
import tod.core.ILocationRegistrer;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.structure.TypeInfo;
import tod.core.model.trace.ILocationTrace;
/**
 * This class permits to register location ids.
 * @author gpothier
 */
public class LocationRegistrer implements ILocationRegistrer, ILocationTrace
{
	private List<TypeInfo> itsTypes = new ArrayList<TypeInfo>();
	private Map<String, TypeInfo> itsTypesMap = new HashMap<String, TypeInfo>();
	
	private List<String> itsFiles = new ArrayList<String>();
	private List<BehaviorInfo> itsBehaviors = new ArrayList<BehaviorInfo>();
	private List<FieldInfo> itsFields = new ArrayList<FieldInfo>();
	private	Map<Long, ThreadInfo> itsThreads = new HashMap<Long, ThreadInfo>();
	
	

	public void registerFile(int aFileId, String aFileName)
	{
		Util.ensureSize(itsFiles, aFileId);
		itsFiles.set(aFileId, aFileName);
	}

	public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
	{
		TypeInfo theType = getType(aTypeId); // this method creates the type if it doesn't exist yet.
		setupTypeInfo(theType, aTypeName, aSupertypeId, aInterfaceIds);
		
		itsTypesMap.put(aTypeName, theType);
	}

	public void registerBehavior(
			BehaviourType aBehaviourType, 
			int aBehaviourId, 
			int aTypeId, 
			String aBehaviourName,
			String aSignature)
	{
		Util.ensureSize(itsBehaviors, aBehaviourId);
		TypeInfo theTypeInfo = getType(aTypeId);
		
		BehaviorInfo theBehaviourInfo = createBehaviourInfo(
				aBehaviourType, 
				aBehaviourId, 
				theTypeInfo, 
				aBehaviourName,
				null,
				null);
		
		itsBehaviors.set (aBehaviourId, theBehaviourInfo);
		theTypeInfo.register(theBehaviourInfo);
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
		TypeInfo theTypeInfo = getType(aTypeId);
		FieldInfo theFieldInfo = createFieldInfo(aFieldId, theTypeInfo, aFieldName);
		itsFields.set (aFieldId, theFieldInfo);
		theTypeInfo.register(theFieldInfo);
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
	
	protected TypeInfo createTypeInfo (int aId)
	{
		return new TypeInfo(aId);
	}
	
	/**
	 * Factory method for field info
	 */
	protected FieldInfo createFieldInfo(int aId, TypeInfo aTypeInfo, String aName)
	{
		return new FieldInfo(aId, aTypeInfo, aName);
	}

	/**
	 * Factory method for constructor info.
	 */
	protected BehaviorInfo createBehaviourInfo(
			BehaviourType aBehaviourType, 
			int aId, 
			TypeInfo aTypeInfo, 
			String aName,
			LineNumberInfo[] aLineNumberTable,
			LocalVariableInfo[] aLocalVariableTable)
	{
		return new BehaviorInfo(aBehaviourType, aId, aTypeInfo, aName, aLineNumberTable, aLocalVariableTable);
	}

	/**
	 * Factory method for type info.
	 */
	protected void setupTypeInfo(TypeInfo aTypeInfo, String aName, int aSupertypeId, int[] aInterfaceIds)
	{
		TypeInfo theSupertype = aSupertypeId >= 0 ? getType(aSupertypeId) : null;
		
		TypeInfo[] theInterfaces = null;
		if (aInterfaceIds != null)
		{
			theInterfaces = new TypeInfo[aInterfaceIds.length];
			for (int i = 0; i < aInterfaceIds.length; i++)
			{
				int theId = aInterfaceIds[i];
				theInterfaces[i] = getType(theId);
			}
		}
		
		aTypeInfo.setName(aName);
		aTypeInfo.setSupertype(theSupertype);
		aTypeInfo.setInterfaces(theInterfaces);
	}
	
	protected void setupThreadInfo (ThreadInfo aThreadInfo, String aName)
	{
		aThreadInfo.setName(aName);
	}


	/**
	 * Returns the type info that corresponds to the specified id.
	 * If the type has not been registered yet, it is created and pre-registered.
	 */
	public TypeInfo getType (int aId)
	{
		Util.ensureSize(itsTypes, aId);
		TypeInfo theTypeInfo = itsTypes.get(aId);
		if (theTypeInfo == null)
		{
			theTypeInfo = createTypeInfo(aId);
			itsTypes.set(aId, theTypeInfo);
		}

		return theTypeInfo;
	}
	
	/**
	 * Returns the type object that corresponds to the given name.
	 */
	public TypeInfo getType(String aName)
	{
		return itsTypesMap.get(aName);
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
	public Iterable<TypeInfo> getTypes()
	{
		return itsTypes;
	}
	
	/**
	 * Returns all available behaviours.
	 */
	public Iterable<BehaviorInfo> getBehaviours()
	{
		return itsBehaviors;
	}
	
	/**
	 * Returns all available fields.
	 */
	public Iterable<FieldInfo> getFields()
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
	public Iterable<ThreadInfo> getThreads()
	{
		return itsThreads.values();
	}
}
