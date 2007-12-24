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
package tod.utils.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationsRepository;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.IMutableClassInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ILocationInfo.ISerializableLocationInfo;
import tod.core.database.structure.IStructureDatabase.Stats;
import tod.impl.database.structure.standard.ArrayTypeInfo;
import tod.impl.database.structure.standard.ClassInfo;
import tod.impl.database.structure.standard.PrimitiveTypeInfo;
import tod.impl.database.structure.standard.StructureDatabase;
import zz.utils.Utils;

/**
 * Remote object that mimics a {@link ILocationsRepository}.
 * Use {@link #createRepository(RIStructureDatabase)} on the client
 * to obtain the actual repository.
 * @author gpothier
 */
public class RemoteStructureDatabase extends UnicastRemoteObject
implements RIStructureDatabase
{
	private IStructureDatabase itsDelegate;
	
	private List<RIStructureDatabaseListener> itsListeners = 
		new ArrayList<RIStructureDatabaseListener>();
	
	public RemoteStructureDatabase(IStructureDatabase aDelegate) throws RemoteException
	{
		itsDelegate = aDelegate;
		
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
						Stats theStats = itsDelegate.getStats();
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
	
	public IBehaviorInfo getBehavior(int aId, boolean aFailIfAbsent)
	{
		return itsDelegate.getBehavior(aId, aFailIfAbsent);
	}

	public IClassInfo getClass(int aId, boolean aFailIfAbsent)
	{
		return itsDelegate.getClass(aId, aFailIfAbsent);
	}

	public IClassInfo getClass(String aName, boolean aFailIfAbsent)
	{
		return itsDelegate.getClass(aName, aFailIfAbsent);
	}

	public IClassInfo getClass(String aName, String aChecksum, boolean aFailIfAbsent)
	{
		return itsDelegate.getClass(aName, aChecksum, aFailIfAbsent);
	}

	public IClassInfo[] getClasses(String aName)
	{
		return itsDelegate.getClasses(aName);
	}

	public IClassInfo[] getClasses() throws RemoteException
	{
		return itsDelegate.getClasses();
	}

	public IFieldInfo getField(int aId, boolean aFailIfAbsent)
	{
		return itsDelegate.getField(aId, aFailIfAbsent);
	}

	public String getId()
	{
		return itsDelegate.getId();
	}

	public Stats getStats()
	{
		return itsDelegate.getStats();
	}

	public ITypeInfo getType(String aName, boolean aFailIfAbsent)
	{
		return null;
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
	 * Implementation of {@link IStructureDatabase} that fetches information
	 * from the remote structure database.
	 * @author gpothier
	 */
	private static class MyDatabase extends UnicastRemoteObject
	implements IMutableStructureDatabase, RIStructureDatabaseListener
	{
		private RIStructureDatabase itsDatabase;
		
		private List<IMutableClassInfo> itsClasses = new ArrayList<IMutableClassInfo>();
		private Map<String, IMutableClassInfo> itsClassesMap = new HashMap<String, IMutableClassInfo>();
		
		private IClassInfo itsUnknownClass = new ClassInfo(this, null, "Unknown", -1);
		
		private List<IBehaviorInfo> itsBehaviors = new ArrayList<IBehaviorInfo>();
		private List<IFieldInfo> itsFields = new ArrayList<IFieldInfo>();

		private Stats itsLastStats = new Stats(0, 0, 0); 
		private boolean itsTypesUpToDate = false;
		private boolean itsBehaviorsUpToDate = false;
		private boolean itsFieldsUpToDate = false;
		
		private final String itsId;
		
		public MyDatabase(RIStructureDatabase aDatabase) throws RemoteException
		{
			itsDatabase = aDatabase;
			itsDatabase.addListener(this);
			
			itsId = aDatabase.getId();
			
			// Load existing classes
			System.out.println("[RemoteStructureDatabase] Fecthing classes...");
			for(IClassInfo theClass : itsDatabase.getClasses())
			{
				cacheClass((IMutableClassInfo) theClass);
			}

			System.out.println("[RemoteStructureDatabase] Done.");
		}
		
		private void cacheClass(IMutableClassInfo aClass)
		{
			// Rebind the class to this database if necessary.
			if (aClass instanceof ISerializableLocationInfo)
			{
				ISerializableLocationInfo theLocation = (ISerializableLocationInfo) aClass;
				theLocation.setDatabase(this);
			}
			
			// If a version of the class is already cached, purge it.
			IClassInfo theCachedClass = Utils.listGet(itsClasses, aClass.getId());
			if (theCachedClass != null)
			{
				System.out.println("[RemoteStructureDatabase] Class already cached: "+theCachedClass);
				for (IBehaviorInfo theBehavior : theCachedClass.getBehaviors())
				{
					System.out.println("[RemoteStructureDatabase] Purging: "+theBehavior);
					Utils.listSet(itsBehaviors, theBehavior.getId(), null);
				}
				
				for (IFieldInfo theField : theCachedClass.getFields())
				{
					System.out.println("[RemoteStructureDatabase] Purging: "+theField);
					Utils.listSet(itsFields, theField.getId(), null);
				}
			}

			Utils.listSet(itsClasses, aClass.getId(), aClass);
			itsClassesMap.put(aClass.getName(), aClass);
			
			for (IBehaviorInfo theBehavior : aClass.getBehaviors())
			{
				Utils.listSet(itsBehaviors, theBehavior.getId(), theBehavior);
			}
			
			for (IFieldInfo theField : aClass.getFields())
			{
				Utils.listSet(itsFields, theField.getId(), theField);
			}
		}
		
		/**
		 * Caches the whole class containg the given member.
		 */
		private void cacheMember(IMemberInfo aMember)
		{
			IMutableClassInfo theClass = (IMutableClassInfo) aMember.getType();
			cacheClass(theClass);
		}
		
		public String getId()
		{
			return itsId;
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
		
		public IBehaviorInfo getBehavior(int aBehaviorId, boolean aFailIfAbsent)
		{
			if (aBehaviorId <= 0) return null;
			
			try
			{
				IBehaviorInfo theBehavior = Utils.listGet(itsBehaviors, aBehaviorId);
				if (theBehavior == null)
				{
					theBehavior = itsDatabase.getBehavior(aBehaviorId, false);
					if (theBehavior != null)
					{
						assert theBehavior.getId() == aBehaviorId;
						cacheMember(theBehavior);
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
					theField = itsDatabase.getField(aFieldId, false);
					if (theField != null)
					{
						assert theField.getId() == aFieldId;
						cacheMember(theField);
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
						cacheClass(theClass);
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
		
		public void addBehaviorListener(IBehaviorListener aListener)
		{
			throw new UnsupportedOperationException();
		}
		
		public void removeBehaviorListener(IBehaviorListener aListener)
		{
			throw new UnsupportedOperationException();
		}

		public IBehaviorInfo[] getBehaviors()
		{
			throw new UnsupportedOperationException();
		}

		public IMutableClassInfo getClass(String aName, boolean aFailIfAbsent)
		{
			try
			{
				IMutableClassInfo theClass = itsClassesMap.get(aName);
				if (theClass == null)
				{
					theClass = (IMutableClassInfo) itsDatabase.getClass(aName, false);
					if (theClass != null)
					{
						Utils.listSet(itsClasses, theClass.getId(), theClass);
						itsClassesMap.put(theClass.getName(), theClass);
					}
				}
				
				if (theClass == null && aFailIfAbsent) throw new RuntimeException("Class not found: "+aName);
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
			throw new UnsupportedOperationException();
		}

		public ITypeInfo getNewType(String aName)
		{
			throw new UnsupportedOperationException();
		}
		
		
	}

}
