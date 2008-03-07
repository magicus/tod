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
package tod.impl.database.structure.standard;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import tod.Util;
import tod.core.DebugFlags;
import tod.core.config.TODConfig;
import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMutableBehaviorInfo;
import tod.core.database.structure.IMutableClassInfo;
import tod.core.database.structure.IMutableFieldInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IShareableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.SourceRange;
import tod.core.database.structure.IStructureDatabase.AspectInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.utils.TODUtils;
import tod.utils.remote.RemoteStructureDatabase;
import zz.utils.Utils;

/**
 * Standard implementation of {@link IStructureDatabase}
 * @author gpothier
 */
public class StructureDatabase implements IShareableStructureDatabase
{
	/**
	 * Class ids below this value are reserved.
	 */
	public static final int FIRST_CLASS_ID = 100;
	
	private final TODConfig itsConfig;
	
	private final String itsId;
	
	/**
	 * The directory that stores the database.
	 * Can be null if the database is not stored.
	 */
	private final File itsFile;
	
	/**
	 * Next free ids.
	 */
	private final Ids itsIds;
	
	/**
	 * Maps class names to {@link ClassNameInfo} objects that keep track
	 * of all the versions of a same class.
	 */
	private final Map<String, ClassNameInfo> itsClassNameInfos =
		new HashMap<String, ClassNameInfo>(1000);
	
	private final List<BehaviorInfo> itsBehaviors = new ArrayList<BehaviorInfo>(10000);
	private final List<FieldInfo> itsFields = new ArrayList<FieldInfo>(10000);
	private final List<ClassInfo> itsClasses = new ArrayList<ClassInfo>(1000);
	
	private final List<ProbeInfo> itsProbes;
	
	/**
	 * Maps advice source ids (see {@link IBehaviorInfo.BytecodeTagType#ADVICE_SOURCE_ID})
	 * to their position in source code.
	 */
	private Map<Integer, SourceRange> itsAdviceSourceMap = 
		new HashMap<Integer, SourceRange>();
	
	private Map<String, AspectInfo> itsAspectInfoMap = 
		new HashMap<String, AspectInfo>();
	
	private final IClassInfo itsUnknownClass = new ClassInfo(this, null, "Unknown", -1);
	
	protected StructureDatabase(TODConfig aConfig, String aId, File aFile, Ids aIds)
	{
		itsConfig = aConfig;
		itsId = aId;
		itsFile = aFile;
		itsIds = aIds;
		itsProbes = new ArrayList<ProbeInfo>(10000);
		itsProbes.add(null);
	}

	/**
	 * Creates a non-persistent structure database.
	 */
	public static StructureDatabase create(TODConfig aConfig, String aId)
	{
		return new StructureDatabase(aConfig, aId, null, new Ids());
	}
	
	/**
	 * Creates a structure database at the location specified in the given config.
	 */
	public static StructureDatabase create(TODConfig aConfig)
	{
		File theFile = new File(aConfig.get(TODConfig.STRUCTURE_DATABASE_LOCATION));
		return create(aConfig, theFile);
	}
	
	/**
	 * Creates a new structure database at the specified location.
	 * @param aFile Location where the structure database must be stored.
	 * The file should not exist.
	 */
	public static StructureDatabase create(TODConfig aConfig, File aFile)
	{
		try
		{
			aFile.mkdirs();
			
			// Generate a new id.
			long theTime = System.nanoTime();
			String theId = Utils.md5String(BigInteger.valueOf(theTime).toByteArray());
			
			Utils.writeObject(theId, new File(aFile, "id"));
			
			StructureDatabase theDatabase = new StructureDatabase(aConfig, theId, aFile, new Ids());
			theDatabase.save();
			return theDatabase;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Loads an existing structure database.
	 */
	public static IStructureDatabase load(File aFile)
	{
		try
		{
			String theId = (String) Utils.readObject(new File(aFile, "id"));
			Ids theIds = (Ids) Utils.readObject(new File(aFile, "ids"));
			// TODO: read config
//			return new StructureDatabase(null, theId, aFile, theIds);
			throw new UnsupportedOperationException();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void save()
	{
		if (itsFile == null) return;
		
		try
		{
			Utils.writeObject(itsIds, new File(itsFile, "ids"));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public String getId()
	{
		return itsId;
	}
	
	public TODConfig getConfig()
	{
		return itsConfig;
	}

	public IClassInfo getClass(String aName, String aChecksum, boolean aFailIfAbsent)
	{
		ClassNameInfo theClassNameInfo = itsClassNameInfos.get(aName);
		if (theClassNameInfo == null && itsFile != null)
		{
			
		}
		
		return null;
	}
	
	public IClassInfo getUnknownClass()
	{
		return itsUnknownClass;
	}

	public ClassInfo getClass(String aName, boolean aFailIfAbsent)
	{
		ClassNameInfo theClassNameInfo = itsClassNameInfos.get(aName);
		if (theClassNameInfo == null) 
		{
			if (aFailIfAbsent) throw new RuntimeException("Class not found: "+aName);
			else return null;
		}
		return theClassNameInfo.getLatest();
	}

	public ClassInfo[] getClasses(String aName)
	{
		ClassNameInfo theClassNameInfo = itsClassNameInfos.get(aName);
		if (theClassNameInfo == null) 
		{
			return new ClassInfo[0];
		}
		return theClassNameInfo.getAll();
	}

	protected void registerClass(IClassInfo aClass)
	{
		Utils.listSet(itsClasses, aClass.getId(), (ClassInfo) aClass);
		ClassNameInfo theClassNameInfo = getClassNameInfo(aClass.getName());
		theClassNameInfo.addClass((ClassInfo) aClass);
	}
	
	protected ClassNameInfo getClassNameInfo(String aClassName)
	{
		ClassNameInfo theClassNameInfo = itsClassNameInfos.get(aClassName);
		if (theClassNameInfo == null)
		{
			theClassNameInfo = new ClassNameInfo();
			itsClassNameInfos.put(aClassName, theClassNameInfo);
		}
		return theClassNameInfo;
	}
	
	public ClassInfo getNewClass(String aName)
	{
		ClassInfo theClass = getClass(aName, false);
		if (theClass == null)
		{
			theClass = new ClassInfo(this, getClassNameInfo(aName), aName, itsIds.nextClassId());
			registerClass(theClass);
		}
		
		return theClass;
	}
	
	public BehaviorInfo getBehavior(int aId, boolean aFailIfAbsent)
	{
		BehaviorInfo theBehavior = Utils.listGet(itsBehaviors, aId);
		if (theBehavior == null && aFailIfAbsent) throw new RuntimeException("Behavior not found: "+aId);
		return theBehavior;
	}
	
	public IBehaviorInfo[] getBehaviors()
	{
		List<IBehaviorInfo> theBehaviors = new ArrayList<IBehaviorInfo>();
		for (IBehaviorInfo theBehavior : itsBehaviors)
		{
			if (theBehavior != null) theBehaviors.add(theBehavior);
		}
		return theBehaviors.toArray(new IBehaviorInfo[theBehaviors.size()]);
	}
	
	public void registerBehavior(IBehaviorInfo aBehavior)
	{
		Utils.listSet(itsBehaviors, aBehavior.getId(), (BehaviorInfo) aBehavior);
		if (DebugFlags.LOG_REGISTERED_BEHAVIORS) 
		{
			System.out.println(String.format(
					"Reg.b. %d: %s.%s",
					aBehavior.getId(),
					aBehavior.getType().getName(),
					Util.getFullName(aBehavior)));
		}
	}

	public ClassInfo getClass(int aId, boolean aFailIfAbsent)
	{
		ClassInfo theClass = Utils.listGet(itsClasses, aId);
		if (theClass == null && aFailIfAbsent) throw new RuntimeException("Class not found: "+aId);
		return theClass;
	}

	public FieldInfo getField(int aId, boolean aFailIfAbsent)
	{
		FieldInfo theField = Utils.listGet(itsFields, aId);
		if (theField == null && aFailIfAbsent) throw new RuntimeException("Field not found: "+aId);
		return theField;
	}
	
	public void registerField(IFieldInfo aField)
	{
		Utils.listSet(itsFields, aField.getId(), (FieldInfo) aField);
	}

	public ITypeInfo getNewType(String aName)
	{
		return getType(this, aName, true, false);
	}
	
	public ITypeInfo getType(String aName, boolean aFailIfAbsent)
	{
		return getType(this, aName, false, aFailIfAbsent);
	}
	
	public IArrayTypeInfo getArrayType(ITypeInfo aBaseType, int aDimensions)
	{
		return new ArrayTypeInfo(this, aBaseType, aDimensions);
	}


	public ITypeInfo getType(int aId, boolean aFailIfAbsent)
	{
		if (aId > 0 && aId <= PrimitiveTypeInfo.TYPES.length) return PrimitiveTypeInfo.get(aId);
		else return getClass(aId, aFailIfAbsent);
	}

	public static ITypeInfo getType(
			IMutableStructureDatabase aStructureDatabase, 
			String aName, 
			boolean aCreateIfAbsent, 
			boolean aFailIfAbsent)
	{
		Type theType = Type.getType(aName);
		switch(theType.getSort())
		{
		case Type.OBJECT:
		{ 
			String theClassName = theType.getClassName();
			return aCreateIfAbsent ? 
					aStructureDatabase.getNewClass(theClassName) 
					: aStructureDatabase.getClass(theClassName, aFailIfAbsent);
		}
			
		case Type.ARRAY:
		{
			ITypeInfo theElementType = getType(
					aStructureDatabase,
					theType.getElementType().getDescriptor(), 
					aCreateIfAbsent, 
					aFailIfAbsent);
			
			int theDimensions = theType.getDimensions();
			
			return new ArrayTypeInfo(
					null, // That should be safe... if there is a problem we'll see what we do
					theElementType,
					theDimensions);			
		}
		
		case Type.VOID: return PrimitiveTypeInfo.VOID;
		case Type.BOOLEAN: return PrimitiveTypeInfo.BOOLEAN;
		case Type.BYTE: return PrimitiveTypeInfo.BYTE;
		case Type.CHAR: return PrimitiveTypeInfo.CHAR;
		case Type.DOUBLE: return PrimitiveTypeInfo.DOUBLE;
		case Type.FLOAT: return PrimitiveTypeInfo.FLOAT;
		case Type.INT: return PrimitiveTypeInfo.INT;
		case Type.LONG: return PrimitiveTypeInfo.LONG;
		case Type.SHORT: return PrimitiveTypeInfo.SHORT;
			
		default:
			// This is not a "normal" failure, so always throw exception
			throw new RuntimeException("Not handled: "+theType);
		}
	}
	
	public IClassInfo[] getClasses()
	{
		List<IClassInfo> theClasses = new ArrayList<IClassInfo>();
		for (IClassInfo theClass : itsClasses)
		{
			if (theClass != null) theClasses.add(theClass);
		}
		return theClasses.toArray(new IClassInfo[theClasses.size()]);
	}
	

	
	public Stats getStats()
	{
		return new Stats(itsClasses.size(), itsBehaviors.size(), itsFields.size());
	}
	
	public int getBehaviorId(String aClassName, String aMethodName, String aMethodSignature)
	{
		return StructureDatabaseUtils.getBehaviorId(this, aClassName, aMethodName, aMethodSignature);
	}
	
	public ProbeInfo getProbeInfo(int aProbeId)
	{
		return itsProbes.get(aProbeId);
	}

	public int addProbe(int aBehaviorId, int aBytecodeIndex, int aAdviceSourceId)
	{
		itsProbes.add(new ProbeInfo(aBehaviorId, aBytecodeIndex, aAdviceSourceId));
		return itsProbes.size()-1;
	}
	
	public void setProbe(int aProbeId, int aBehaviorId, int aBytecodeIndex, int aAdviceSourceId)
	{
		itsProbes.set(aProbeId, new ProbeInfo(aBehaviorId, aBytecodeIndex, aAdviceSourceId));
	}

	public int getProbeCount()
	{
		return itsProbes.size();
	}
	
	public SourceRange getAdviceSource(int aAdviceId)
	{
		return itsAdviceSourceMap.get(aAdviceId);
	}
	
	public Map<String, AspectInfo> getAspectInfoMap()
	{
		return itsAspectInfoMap;
	}

	public void setAdviceSourceMap(Map<Integer, SourceRange> aMap)
	{
		for (Map.Entry<Integer, SourceRange> theEntry : aMap.entrySet())
		{
			int theId = theEntry.getKey();
			SourceRange theRange = theEntry.getValue();
			assert theRange != null;
			
			// Fill advice source map
			SourceRange thePrevious = itsAdviceSourceMap.put(theId, theRange);
			if (thePrevious != null && ! thePrevious.equals(theRange))
			{
				Utils.rtex(
						"Advice source inconsistency for id %d (prev.: %s, new: %s)", 
						theId,
						thePrevious,
						theRange);
			}
			
			// Fill aspect info map
			if (thePrevious == null)
			{
				AspectInfo theInfo = itsAspectInfoMap.get(theRange.sourceFile);
				if (theInfo == null)
				{
					theInfo = new AspectInfo(theRange.sourceFile);
					itsAspectInfoMap.put(theRange.sourceFile, theInfo);
				}
				theInfo.addAdviceId(theId);
			}
		}
	}

	/**
	 * This method is used to retrieve the value of transient fields on the remote side
	 * (see {@link RemoteStructureDatabase}).
	 */
	public byte[] _getClassBytecode(int aClassId)
	{
		return getClass(aClassId, true)._getBytecode();
	}
	
	/**
	 * This method is used to retrieve the value of transient fields on the remote side
	 * (see {@link RemoteStructureDatabase}).
	 */
	public Map<String, IMutableFieldInfo> _getClassFieldMap(int aClassId)
	{
		return getClass(aClassId, true)._getFieldsMap();
	}
	
	/**
	 * This method is used to retrieve the value of transient fields on the remote side
	 * (see {@link RemoteStructureDatabase}).
	 */
	public Map<String, IMutableBehaviorInfo> _getClassBehaviorsMap(int aClassId)
	{
		return getClass(aClassId, true)._getBehaviorsMap();
	}

	/**
	 * This method is used to retrieve the value of transient fields on the remote side
	 * (see {@link RemoteStructureDatabase}).
	 */
	public LocalVariableInfo[] _getBehaviorLocalVariableInfo(int aBehaviorId)
	{
		return getBehavior(aBehaviorId, true)._getLocalVariables();
	}
	
	/**
	 * This method is used to retrieve the value of transient fields on the remote side
	 * (see {@link RemoteStructureDatabase}).
	 */
	public LineNumberInfo[] _getBehaviorLineNumberInfo(int aBehaviorId)
	{
		return getBehavior(aBehaviorId, true)._getLineNumberTable();
	}
	
	/**
	 * This method is used to retrieve the value of transient fields on the remote side
	 * (see {@link RemoteStructureDatabase}).
	 */
	public TagMap _getBehaviorTagMap(int aBehaviorId)
	{
		return getBehavior(aBehaviorId, true)._getTagMap();
	}
	
	public IClassInfo _getBehaviorClass(int aBehaviorId, boolean aFailIfAbsent)
	{
		BehaviorInfo theBehavior = getBehavior(aBehaviorId, aFailIfAbsent);
		return theBehavior != null ? theBehavior.getType() : null;
	}

	public IClassInfo _getFieldClass(int aFieldId, boolean aFailIfAbsent)
	{
		FieldInfo theField = getField(aFieldId, aFailIfAbsent);
		return theField != null ? theField.getType() : null;
	}

	private static class Ids implements Serializable
	{
		private static final long serialVersionUID = -8031089051309554360L;
		
		/**
		 * Ids below FIRST_CLASS_ID are reserved; Ids 1 to 9 are for primitive types.
		 */
		private int itsNextFreeClassId = FIRST_CLASS_ID;
		private int itsNextFreeBehaviorId = 1;
		private int itsNextFreeFieldId = 1;

		public synchronized int nextClassId()
		{
			return itsNextFreeClassId++;
		}
		
		public synchronized int nextBehaviorId()
		{
			return itsNextFreeBehaviorId++;
		}
		
		public synchronized int nextFieldId()
		{
			return itsNextFreeFieldId++;
		}
	}
	
	/**
	 * Information associated to a class name. Note that several classes can share
	 * the same class name: there can be several versions of the same class.
	 * @author gpothier
	 */
	public class ClassNameInfo
	{
		/**
		 * Maps class checksum to class info.
		 */
		private HashMap<String, ClassInfo> itsChecksumToClassMap =
			new HashMap<String, ClassInfo>();
		
		/**
		 * List of classes in the order they were added to the database.
		 */
		private List<ClassInfo> itsChronologicalClasses =
			new ArrayList<ClassInfo>();
		
		/**
		 * Maps of the ids of all the members of this set of classes.
		 * It is used so that homonym classes all have the same ids
		 * for "compatible" members. 
		 */
		private Map<String, Integer> itsIdMap =
			new HashMap<String, Integer>();
		
		public void addClass(ClassInfo aClass)
		{
			itsChecksumToClassMap.put(aClass.getChecksum(), aClass);
			itsChronologicalClasses.add(aClass);
		}
		
		/**
		 * Gets the latest registered class for this name.
		 */
		public ClassInfo getLatest()
		{
			if (itsChronologicalClasses.isEmpty()) return null;
			return itsChronologicalClasses.get(itsChronologicalClasses.size()-1);
		}
		
		/**
		 * Returns all the classes registered with this name, in chronological order.
		 */
		public ClassInfo[] getAll()
		{
			return itsChronologicalClasses.toArray(new ClassInfo[itsChronologicalClasses.size()]);
		}
		
		/**
		 * Returns an id for a particular field.
		 * The ids map is first checked, and if no corresponding
		 * id exists a new one is created.
		 * @param aName Name of the field.
		 * @param aType Type of the field.
		 */
		public int getFieldId(String aName, ITypeInfo aType)
		{
			String theKey = ClassInfo.getFieldKey(aName, aType);
			Integer theId = itsIdMap.get(theKey);
			if (theId == null)
			{
				theId = itsIds.nextFieldId();
				itsIdMap.put(theKey, theId);
			}
			return theId;
		}
		
		/**
		 * Returns an id for a particular behavior.
		 * The ids map is first checked, and if no corresponding
		 * id exists a new one is created.
		 * @param aName Name of the behavior.
		 * @param aType Type of the behavior's arguments.
		 */
		public int getBehaviorId(String aName, ITypeInfo[] aArgumentTypes)
		{
			String theKey = ClassInfo.getBehaviorKey(aName, aArgumentTypes);
			Integer theId = itsIdMap.get(theKey);
			if (theId == null)
			{
				theId = itsIds.nextBehaviorId();
				itsIdMap.put(theKey, theId);
			}
			return theId;
		}
	}
}
