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
package tod.utils.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.config.TODConfig;
import tod.core.database.structure.IAdviceInfo;
import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.IAspectInfo;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IMutableBehaviorInfo;
import tod.core.database.structure.IMutableClassInfo;
import tod.core.database.structure.IMutableFieldInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IShareableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.SourceRange;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.ILocationInfo.ISerializableLocationInfo;
import tod.core.database.structure.IStructureDatabase.LineNumberInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.core.database.structure.IStructureDatabase.Stats;
import tod.impl.database.structure.standard.ArrayTypeInfo;
import tod.impl.database.structure.standard.ClassInfo;
import tod.impl.database.structure.standard.PrimitiveTypeInfo;
import tod.impl.database.structure.standard.StructureDatabase;
import tod.impl.database.structure.standard.StructureDatabaseUtils;
import tod.impl.database.structure.standard.TagMap;
import zz.utils.Utils;

/**
 * Remote object that mimics a {@link IStructureDatabase}.
 * Use {@link #createDatabase(RIStructureDatabase)} on the client
 * to obtain the actual repository.
 * @author gpothier
 */
public class RemoteStructureDatabase extends UnicastRemoteObject
implements RIStructureDatabase
{
	private IShareableStructureDatabase itsSource;
	
	/**
	 * Whether mutations are authorized.
	 */
	private boolean itsMutable;
	
	private List<RIStructureDatabaseListener> itsListeners = 
		new ArrayList<RIStructureDatabaseListener>();
	
	private RemoteStructureDatabase(IShareableStructureDatabase aSource, boolean aMutable) throws RemoteException
	{
		itsSource = aSource;
		itsMutable = aMutable;
		
		Thread theNotifierThread = new Thread("RemoteStructureDatabase.Notifier")
		{
			private Stats itsLastStats;
			
			@Override
			public void run()
			{
				try
				{
					while(true)
					{
						Stats theStats = itsSource.getStats();
						if (! theStats.equals(itsLastStats)) fireChanged(theStats);
						itsLastStats = theStats;
						sleep(1000);
					}
				}
				catch (InterruptedException e)
				{
					throw new RuntimeException(e);
				}
			}
		};
		theNotifierThread.setDaemon(true);
		theNotifierThread.start();
	}
	
	/**
	 * Creates a {@link RemoteStructureDatabase} for a local structure database.
	 * @param aMutable Whether the remote client should be able to perform mutation operations. 
	 */
	public static RemoteStructureDatabase create(
			IShareableStructureDatabase aStructureDatabase,
			boolean aMutable) throws RemoteException
	{
		return new RemoteStructureDatabase(aStructureDatabase, aMutable);
	}

	public void addListener(RIStructureDatabaseListener aListener) throws RemoteException
	{
		itsListeners.add(aListener);
		aListener.changed(null);
	}
	
	protected void fireChanged(Stats aStats)
	{
		for (RIStructureDatabaseListener theListener : itsListeners)
		{
			try
			{
				theListener.changed(aStats);
			}
			catch (RemoteException e)
			{
				System.err.println("[RemoteStructureDatabase] Could not fire change event:");
				e.printStackTrace();
			}
		}
	}
	
	public IClassInfo getClass(int aId, boolean aFailIfAbsent)
	{
		return itsSource.getClass(aId, aFailIfAbsent);
	}

	public IClassInfo getClass(String aName, boolean aFailIfAbsent)
	{
		return itsSource.getClass(aName, aFailIfAbsent);
	}
	
	public IClassInfo getNewClass(String aName)
	{
		if (! itsMutable) throw new UnsupportedOperationException("Not mutable");
		return itsSource.getNewClass(aName);
	}

	public IClassInfo getClass(String aName, String aChecksum, boolean aFailIfAbsent)
	{
		return itsSource.getClass(aName, aChecksum, aFailIfAbsent);
	}

	public IClassInfo[] getClasses(String aName)
	{
		return itsSource.getClasses(aName);
	}

	public IClassInfo[] getClasses() throws RemoteException
	{
		return itsSource.getClasses();
	}

	public TODConfig getConfig() 
	{
		return itsSource.getConfig();
	}

	public String getId()
	{
		return itsSource.getId();
	}

	public Stats getStats()
	{
		return itsSource.getStats();
	}

	public ITypeInfo getType(String aName, boolean aFailIfAbsent)
	{
		return null;
	}

	/**
	 * Returns the missing probe infos, given that we already have some of them.
	 */
	public ProbeInfo[] getProbeInfos(int aAvailableCount) 
	{
		int theCount = itsSource.getProbeCount();
		int theMissing = theCount-aAvailableCount;
		if (theMissing == 0) return null;
		
		ProbeInfo[] theResult = new ProbeInfo[theMissing];
		for (int i=0;i<theMissing;i++) theResult[i] = itsSource.getProbeInfo(i+aAvailableCount);
		
		return theResult;
	}
	
	public int getNewExceptionProbeInfo(int aBehaviorId, int aBytecodeIndex)
	{
		if (! itsMutable) throw new UnsupportedOperationException("Not mutable");
		ProbeInfo theProbeInfo = itsSource.getNewExceptionProbe(aBehaviorId, aBytecodeIndex);
		return theProbeInfo.id;
	}

	
	public IAdviceInfo getAdvice(int aAdviceId) throws RemoteException
	{
		return itsSource.getAdvice(aAdviceId);
	}

	public Map<String, IAspectInfo> getAspectInfoMap() 
	{
		return itsSource.getAspectInfoMap();
	}

	public LineNumberInfo[] _getBehaviorLineNumberInfo(int aBehaviorId)
	{
		return itsSource._getBehaviorLineNumberInfo(aBehaviorId);
	}

	public List<LocalVariableInfo> _getBehaviorLocalVariableInfo(int aBehaviorId) 
	{
		return itsSource._getBehaviorLocalVariableInfo(aBehaviorId);
	}

	public TagMap _getBehaviorTagMap(int aBehaviorId) 
	{
		return itsSource._getBehaviorTagMap(aBehaviorId);
	}

	public Map<String, IMutableBehaviorInfo> _getClassBehaviorsMap(int aClassId) 
	{
		return itsSource._getClassBehaviorsMap(aClassId);
	}

	public byte[] _getClassBytecode(int aClassId) 
	{
		return itsSource._getClassBytecode(aClassId);
	}

	public Map<String, IMutableFieldInfo> _getClassFieldMap(int aClassId) 
	{
		return itsSource._getClassFieldMap(aClassId);
	}

	public IClassInfo _getBehaviorClass(int aBehaviorId, boolean aFailIfAbsent)
	{
		return itsSource._getBehaviorClass(aBehaviorId, aFailIfAbsent);
	}
	
	public IClassInfo _getFieldClass(int aFieldId, boolean aFailIfAbsent) throws RemoteException
	{
		return itsSource._getFieldClass(aFieldId, aFailIfAbsent);
	}

	/**
	 * Creates a local locations repository that delegates to a remote one.
	 */
	public static IStructureDatabase createDatabase(RIStructureDatabase aDatabase)
	{
		assert aDatabase != null;
		try
		{
			return new MyDatabase(aDatabase);
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Creates a local locations repository that delegates to a remote one.
	 */
	public static IMutableStructureDatabase createMutableDatabase(RIStructureDatabase aDatabase)
	{
		assert aDatabase != null;
		try
		{
			return new MyDatabase(aDatabase);
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Implementation of {@link IStructureDatabase} that fetches information
	 * from the remote structure database.
	 * @author gpothier
	 */
	private static class MyDatabase extends UnicastRemoteObject
	implements IShareableStructureDatabase, RIStructureDatabaseListener
	{
		private RIStructureDatabase itsDatabase;
		
		private List<IMutableClassInfo> itsClasses = new ArrayList<IMutableClassInfo>();
		private Map<String, IMutableClassInfo> itsClassesMap = new HashMap<String, IMutableClassInfo>();
		
		private IClassInfo itsUnknownClass = new ClassInfo(this, null, "Unknown", -1);
		
		private List<IMutableBehaviorInfo> itsBehaviors = new ArrayList<IMutableBehaviorInfo>();
		private List<IFieldInfo> itsFields = new ArrayList<IFieldInfo>();
		
		private List<IAdviceInfo> itsAdvices = new ArrayList<IAdviceInfo>();

		private Stats itsLastStats = new Stats(0, 0, 0, 0); 
		private boolean itsTypesUpToDate = false;
		private boolean itsBehaviorsUpToDate = false;
		private boolean itsFieldsUpToDate = false;
		
		private List<ProbeInfo> itsProbes = new ArrayList<ProbeInfo>();
		
		private Map<String, IAspectInfo> itsAspectInfoMap;
		
		private final TODConfig itsConfig;
		private final String itsId;
		
		public MyDatabase(RIStructureDatabase aDatabase) throws RemoteException
		{
			itsDatabase = aDatabase;
			itsDatabase.addListener(this);
			
			itsConfig = aDatabase.getConfig();
			itsId = aDatabase.getId();
			
//			// Load existing classes
//			System.out.println("[RemoteStructureDatabase] Fecthing classes...");
//			for(IClassInfo theClass : itsDatabase.getClasses())
//			{
//				cacheClass((IMutableClassInfo) theClass);
//			}

			System.out.println("[RemoteStructureDatabase] Done.");
		}
		
		/**
		 * Rebinds the given location info to this database,
		 * @param aLocation
		 */
		private void rebind(ILocationInfo aLocation)
		{
			if (aLocation instanceof ISerializableLocationInfo)
			{
				ISerializableLocationInfo theLocation = (ISerializableLocationInfo) aLocation;
				assert aLocation.getDatabase() == null;
				theLocation.setDatabase(this, false);
			}
			else assert false;
		}
		
		private void cacheClass(IMutableClassInfo aClass, boolean aCacheMembers)
		{
			Utils.listSet(itsClasses, aClass.getId(), aClass);
			itsClassesMap.put(aClass.getName(), aClass);
			rebind(aClass);
			
			if (aCacheMembers)
			{
				aClass.getBehaviors();
				aClass.getFields();
			}
		}
		
		private void cacheBehavior(IMutableBehaviorInfo aBehavior)
		{
			IMutableBehaviorInfo theBehavior = Utils.listGet(itsBehaviors, aBehavior.getId());
			
			Utils.listSet(itsBehaviors, aBehavior.getId(), aBehavior);
			rebind(aBehavior);
		}

		private void cacheField(IFieldInfo aField)
		{
			IFieldInfo theField = Utils.listGet(itsFields, aField.getId());
			
			Utils.listSet(itsFields, aField.getId(), aField);
			rebind(aField);
		}
		
		private void cacheAdvice(IAdviceInfo aAdvice)
		{
			IAdviceInfo theAdvice = Utils.listGet(itsAdvices, aAdvice.getId());
			
			Utils.listSet(itsAdvices, aAdvice.getId(), aAdvice);
			rebind(aAdvice);
		}
		
		
		public String getId()
		{
			return itsId;
		}
		
		public TODConfig getConfig()
		{
			return itsConfig;
		}

		public void changed(Stats aStats)
		{
			if (aStats == null) return; // Just for testing 
			if (aStats.nTypes != itsLastStats.nTypes) itsTypesUpToDate = false;
			if (aStats.nBehaviors != itsLastStats.nBehaviors) itsBehaviorsUpToDate = false;
			if (aStats.nFields != itsLastStats.nFields) itsFieldsUpToDate = false;
			itsLastStats = aStats;
		}
		
		private void updateStats()
		{
			try
			{
				itsLastStats = itsDatabase.getStats();
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		public IMutableBehaviorInfo getBehavior(int aBehaviorId, boolean aFailIfAbsent)
		{
			if (aBehaviorId <= 0) return null;
			
			try
			{
				IMutableBehaviorInfo theBehavior = Utils.listGet(itsBehaviors, aBehaviorId);
				if (theBehavior == null)
				{
					IMutableClassInfo theClass = (IMutableClassInfo) itsDatabase._getBehaviorClass(aBehaviorId, aFailIfAbsent);
					if (theClass != null)
					{
						cacheClass(theClass, true);
						theBehavior = Utils.listGet(itsBehaviors, aBehaviorId);
					}
				}
				
				if (theBehavior == null && aFailIfAbsent) throw new RuntimeException("Behavior not found: "+aBehaviorId);
				return theBehavior;
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public IFieldInfo getField(int aFieldId, boolean aFailIfAbsent)
		{
			try
			{
				IFieldInfo theField = Utils.listGet(itsFields, aFieldId);
				if (theField == null)
				{
					IMutableClassInfo theClass = (IMutableClassInfo) itsDatabase._getFieldClass(aFieldId, aFailIfAbsent);
					if (theClass != null)
					{
						cacheClass(theClass, true);
						theField = Utils.listGet(itsFields, aFieldId);
					}
				}
				
				if (theField == null && aFailIfAbsent) throw new RuntimeException("Field not found: "+aFieldId);
				return theField;
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public IMutableClassInfo getClass(int aClassId, boolean aFailIfAbsent)
		{
			try
			{
				IMutableClassInfo theClass = Utils.listGet(itsClasses, aClassId);
				if (theClass == null)
				{
					theClass = (IMutableClassInfo) itsDatabase.getClass(aClassId, false);
					if (theClass != null)
					{
						assert theClass.getId() == aClassId;
						cacheClass(theClass, false);
					}
				}
				
				if (theClass == null && aFailIfAbsent) throw new RuntimeException("Class not found: "+aClassId);
				return theClass;
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		public IClassInfo getUnknownClass()
		{
			return itsUnknownClass;
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

		public ITypeInfo getType(String aName, boolean aFailIfAbsent)
		{
			return StructureDatabase.getType(this, aName, false, aFailIfAbsent);
		}


		public Stats getStats()
		{
			return itsLastStats;
		}		
		
		public IBehaviorInfo[] getBehaviors()
		{
			updateStats();
			List<IBehaviorInfo> theBehaviors = new ArrayList<IBehaviorInfo>();
			for (int i=1;i<getStats().nBehaviors;i++)
			{
				IBehaviorInfo theBehavior = getBehavior(i, false);
				if (theBehavior != null) theBehaviors.add(theBehavior);
			}
			return theBehaviors.toArray(new IBehaviorInfo[theBehaviors.size()]);
		}

		public IMutableClassInfo getClass(String aName, boolean aFailIfAbsent)
		{
			try
			{
				IMutableClassInfo theClass = itsClassesMap.get(aName);
				if (theClass == null)
				{
					theClass = (IMutableClassInfo) itsDatabase.getClass(aName, false);
					if (theClass != null) cacheClass(theClass, false);
				}
				
				if (theClass == null && aFailIfAbsent) throw new RuntimeException("Class not found in database: "+aName);
				return theClass;
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public IClassInfo getClass(String aName, String aChecksum, boolean aFailIfAbsent)
		{
			throw new UnsupportedOperationException();
		}

		public IClassInfo[] getClasses()
		{
			updateStats();
			List<IClassInfo> theClasses = new ArrayList<IClassInfo>();
			for (int i=StructureDatabase.FIRST_CLASS_ID;i<getStats().nTypes;i++)
			{
				IMutableClassInfo theClass = getClass(i, false);
				if (theClass != null) theClasses.add(theClass);
			}
			return theClasses.toArray(new IClassInfo[theClasses.size()]);
		}

		public IClassInfo[] getClasses(String aName)
		{
			return null;
		}

		public IMutableClassInfo getNewClass(String aName)
		{
			try
			{
				IMutableClassInfo theClass = itsClassesMap.get(aName);
				if (theClass == null)
				{
					theClass = (IMutableClassInfo) itsDatabase.getNewClass(aName);
					cacheClass(theClass, false);
				}
				return theClass;
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		public IMutableClassInfo addClass(int aId, String aName)
		{
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public ITypeInfo getNewType(String aName)
		{
			throw new UnsupportedOperationException();
		}
		
		public int getBehaviorId(String aClassName, String aMethodName, String aMethodSignature)
		{
			return StructureDatabaseUtils.getBehaviorId(this, aClassName, aMethodName, aMethodSignature);
		}
		
		public ProbeInfo getProbeInfo(int aProbeId)
		{
			if (aProbeId >= itsProbes.size())
			{
				// Fetch missing probes
				try
				{
					ProbeInfo[] theProbeInfos = itsDatabase.getProbeInfos(itsProbes.size());
					for (ProbeInfo theProbeInfo : theProbeInfos) itsProbes.add(theProbeInfo);
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}
			return itsProbes.get(aProbeId);
		}

		public int getProbeCount()
		{
			throw new UnsupportedOperationException();
		}

		public int addProbe(int aBehaviorId, int aBytecodeIndex, BytecodeRole aRole, int aAdviceSourceId)
		{
			throw new UnsupportedOperationException();
		}

		public void addProbe(
				int aId,
				int aBehaviorId,
				int aBytecodeIndex,
				BytecodeRole aRole,
				int aAdviceSourceId)
		{
			throw new UnsupportedOperationException();
		}

		public void setProbe(int aProbeId, int aBehaviorId, int aBytecodeIndex, BytecodeRole aRole, int aAdviceSourceId)
		{
			throw new UnsupportedOperationException();
		}

		public ProbeInfo getNewExceptionProbe(int aBehaviorId, int aBytecodeIndex)
		{
			try
			{
				int theProbeId = itsDatabase.getNewExceptionProbeInfo(aBehaviorId, aBytecodeIndex);
				return getProbeInfo(theProbeId);
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void setAdviceSourceMap(Map<Integer, SourceRange> aMap)
		{
			throw new UnsupportedOperationException();
		}

		public IAdviceInfo getAdvice(int aAdviceId)
		{
			IAdviceInfo theAdvice = Utils.listGet(itsAdvices, aAdviceId);
			if (theAdvice == null)	
			{
				try
				{
					theAdvice = itsDatabase.getAdvice(aAdviceId);
					Utils.listSet(itsAdvices, aAdviceId, theAdvice);
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}
			return theAdvice;
		}
		
		public Map<String, IAspectInfo> getAspectInfoMap()
		{
			if (itsAspectInfoMap == null)
			{
				try
				{
					itsAspectInfoMap = itsDatabase.getAspectInfoMap();
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}
			return itsAspectInfoMap;
		}

		public LineNumberInfo[] _getBehaviorLineNumberInfo(int aBehaviorId)
		{
			try
			{
				System.out.println("Retrieving line number info for behavior: "+aBehaviorId);
				return itsDatabase._getBehaviorLineNumberInfo(aBehaviorId);
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public List<LocalVariableInfo> _getBehaviorLocalVariableInfo(int aBehaviorId)
		{
			try
			{
				System.out.println("Retrieving local variable info for behavior: "+aBehaviorId);
				return itsDatabase._getBehaviorLocalVariableInfo(aBehaviorId);
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public TagMap _getBehaviorTagMap(int aBehaviorId)
		{
			try
			{
				System.out.println("Retrieving tag map for behavior: "+aBehaviorId);
				return itsDatabase._getBehaviorTagMap(aBehaviorId);
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public Map<String, IMutableBehaviorInfo> _getClassBehaviorsMap(int aClassId)
		{
			try
			{
				System.out.println("Retrieving behavior map for class: "+aClassId);
				Map<String, IMutableBehaviorInfo> theMap = itsDatabase._getClassBehaviorsMap(aClassId);
				for (IMutableBehaviorInfo theBehavior : theMap.values()) cacheBehavior(theBehavior);
				return theMap;
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public Map<String, IMutableFieldInfo> _getClassFieldMap(int aClassId)
		{
			try
			{
				System.out.println("Retrieving field map for class: "+aClassId);
				Map<String, IMutableFieldInfo> theMap = itsDatabase._getClassFieldMap(aClassId);
				for (IMutableFieldInfo theField : theMap.values()) cacheField(theField);
				return theMap;
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public byte[] _getClassBytecode(int aClassId)
		{
			try
			{
				System.out.println("Retrieving bytecode for class: "+aClassId);
				return itsDatabase._getClassBytecode(aClassId);
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public IClassInfo _getBehaviorClass(int aBehaviorId, boolean aFailIfAbsent)
		{
			try
			{
				return itsDatabase._getBehaviorClass(aBehaviorId, aFailIfAbsent);
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public IClassInfo _getFieldClass(int aFieldId, boolean aFailIfAbsent)
		{
			try
			{
				return itsDatabase._getFieldClass(aFieldId, aFailIfAbsent);
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		
	}

}
