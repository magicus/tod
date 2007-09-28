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

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.IStructureDatabase.IBehaviorListener;
import tod.core.database.structure.IStructureDatabase.Stats;
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
				System.err.println("[RemoteLocationsRepository] Could not fire change event");
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

	public IClassInfo getNewClass(String aName)
	{
		return itsDelegate.getNewClass(aName);
	}

	public Stats getStats()
	{
		return null;
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
	
	private static class MyDatabase extends UnicastRemoteObject
	implements IStructureDatabase, RIStructureDatabaseListener
	{
		private RIStructureDatabase itsDatabase;
		
		private List<IClassInfo> itsClasses = new ArrayList<IClassInfo>();
		private Map<String, IClassInfo> itsClassesMap = new HashMap<String, IClassInfo>();
		
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
				Utils.listSet(itsClasses, theClass.getId(), theClass);
				itsClassesMap.put(theClass.getName(), theClass);
				
				for (IBehaviorInfo theBehavior : theClass.getBehaviors())
				{
					Utils.listSet(itsBehaviors, theBehavior.getId(), theBehavior);
				}
				
				for (IFieldInfo theField : theClass.getFields())
				{
					Utils.listSet(itsFields, theField.getId(), theField);
				}
			}

			System.out.println("[RemoteLocationsRepository] Done.");
		}
		
		public String getId()
		{
			return itsId;
		}
		
		public void changed(Stats aStats)
		{
			if (aStats.nTypes != itsLastStats.nTypes) itsTypesUpToDate = false;
			if (aStats.nBehaviors != itsLastStats.nBehaviors) itsBehaviorsUpToDate = false;
			if (aStats.nFields != itsLastStats.nFields) itsFieldsUpToDate = false;
			itsLastStats = aStats;
		}
		
		private <T> T get(List<T> aList, int aIndex)
		{
			if (aList.size() > aIndex) return aList.get(aIndex);
			else return null;
		}

		public IBehaviorInfo getBehavior(int aBehaviorId, boolean aFailIfAbsent)
		{
			if (aBehaviorId <= 0) return null;
			
			try
			{
				IBehaviorInfo theBehavior = get(itsBehaviors, aBehaviorId);
				if (theBehavior == null)
				{
					theBehavior = itsDatabase.getBehavior(aBehaviorId, false);
					if (theBehavior != null)
					{
						assert theBehavior.getId() == aBehaviorId;
						Utils.listSet(itsBehaviors, aBehaviorId, theBehavior);
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
				IFieldInfo theField = get(itsFields, aFieldId);
				if (theField == null)
				{
					theField = itsDatabase.getField(aFieldId, false);
					if (theField != null)
					{
						assert theField.getId() == aFieldId;
						Utils.listSet(itsFields, aFieldId, theField);
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

		public IClassInfo getClass(int aClassId, boolean aFailIfAbsent)
		{
			try
			{
				IClassInfo theClass = get(itsClasses, aClassId);
				if (theClass == null)
				{
					theClass = itsDatabase.getClass(aClassId, false);
					if (theClass != null)
					{
						assert theClass.getId() == aClassId;
						Utils.listSet(itsClasses, aClassId, theClass);
						itsClassesMap.put(theClass.getName(), theClass);
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

		public IClassInfo getClass(String aName, boolean aFailIfAbsent)
		{
			try
			{
				IClassInfo theClass = itsClassesMap.get(aName);
				if (theClass == null)
				{
					theClass = itsDatabase.getClass(aName, false);
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
			throw new UnsupportedOperationException();
		}

		public IClassInfo[] getClasses(String aName)
		{
			return null;
		}

		public IClassInfo getNewClass(String aName)
		{
			throw new UnsupportedOperationException();
		}

		public ITypeInfo getNewType(String aName)
		{
			throw new UnsupportedOperationException();
		}
		
		
	}

}
