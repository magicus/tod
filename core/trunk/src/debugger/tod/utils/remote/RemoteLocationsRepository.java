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
import tod.core.database.structure.ILocationsRepository;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ILocationsRepository.Stats;
import zz.utils.Utils;

/**
 * Remote object that mimics a {@link ILocationsRepository}.
 * Use {@link #createRepository(RILocationsRepository)} on the client
 * to obtain the actual repository.
 * @author gpothier
 */
public class RemoteLocationsRepository extends UnicastRemoteObject
implements RILocationsRepository
{
	private ILocationsRepository itsDelegate;
	
	private List<RILocationsRepositoryListener> itsListeners = 
		new ArrayList<RILocationsRepositoryListener>();
	
	public RemoteLocationsRepository(ILocationsRepository aDelegate) throws RemoteException
	{
		itsDelegate = aDelegate;
		
		Thread theNotifierThread = new Thread("RemoteLocationsRepository.Notifier")
		{
			private ILocationsRepository.Stats itsLastStats;
			
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

	public void addListener(RILocationsRepositoryListener aListener) throws RemoteException
	{
		itsListeners.add(aListener);
	}
	
	protected void fireChanged(Stats aStats)
	{
		for (RILocationsRepositoryListener theListener : itsListeners)
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

	public IBehaviorInfo getBehavior(int aBehaviorId)
	{
		return itsDelegate.getBehavior(aBehaviorId);
	}

	public Iterable<IBehaviorInfo> getBehaviours()
	{
		return itsDelegate.getBehaviours();
	}

	public Iterable<ITypeInfo> getTypes()
	{
		return itsDelegate.getTypes();
	}

	public IFieldInfo getField(int aFieldId)
	{
		return itsDelegate.getField(aFieldId);
	}

	public Iterable<IFieldInfo> getFields()
	{
		return itsDelegate.getFields();
	}

	public Iterable<String> getFiles()
	{
		return itsDelegate.getFiles();
	}

	public ITypeInfo getType(int aId)
	{
		return itsDelegate.getType(aId);
	}

	public ITypeInfo getType(String aName)
	{
		return itsDelegate.getType(aName);
	}
	
	public Stats getStats()
	{
		return itsDelegate.getStats();
	}

	/**
	 * Creates a local locations repository that delegates to a remote one.
	 */
	public static ILocationsRepository createRepository(RILocationsRepository aRepository)
	{
		assert aRepository != null;
		try
		{
			return new MyRepository(aRepository);
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private static class MyRepository extends UnicastRemoteObject
	implements ILocationsRepository, RILocationsRepositoryListener
	{
		private RILocationsRepository itsRepository;
		
		private List<ITypeInfo> itsTypes = new ArrayList<ITypeInfo>();
		private Map<String, ITypeInfo> itsTypesMap = new HashMap<String, ITypeInfo>();
		
		private List<IBehaviorInfo> itsBehaviors = new ArrayList<IBehaviorInfo>();
		private List<IFieldInfo> itsFields = new ArrayList<IFieldInfo>();

		private Stats itsLastStats = new Stats(0, 0, 0); 
		private boolean itsTypesUpToDate = false;
		private boolean itsBehaviorsUpToDate = false;
		private boolean itsFieldsUpToDate = false;
		
		public MyRepository(RILocationsRepository aRepository) throws RemoteException
		{
			itsRepository = aRepository;
			itsRepository.addListener(this);
			
			// Init types
			System.out.println("[RemoteLocationsRepository] Fecthing types...");
			for(ITypeInfo theType : itsRepository.getTypes())
			{
				if (theType == null) continue;
				
				Utils.listSet(itsTypes, theType.getId(), theType);
				itsTypesMap.put(theType.getName(), theType);
				
				if (theType instanceof IClassInfo)
				{
					IClassInfo theClass = (IClassInfo) theType;
					
					for (IBehaviorInfo theBehavior : theClass.getBehaviors())
					{
						Utils.listSet(itsBehaviors, theBehavior.getId(), theBehavior);
					}
					
					for (IFieldInfo theField : theClass.getFields())
					{
						Utils.listSet(itsFields, theField.getId(), theField);
					}
				}
			}

			System.out.println("[RemoteLocationsRepository] Done.");
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

		public IBehaviorInfo getBehavior(int aBehaviorId)
		{
			if (aBehaviorId <= 0) return null;
			
			try
			{
				IBehaviorInfo theBehavior = get(itsBehaviors, aBehaviorId);
				if (theBehavior == null)
				{
					theBehavior = itsRepository.getBehavior(aBehaviorId);
					assert theBehavior.getId() == aBehaviorId;
					Utils.listSet(itsBehaviors, aBehaviorId, theBehavior);
				}
				
				return theBehavior;
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public IFieldInfo getField(int aFieldId)
		{
			try
			{
				IFieldInfo theField = get(itsFields, aFieldId);
				if (theField == null)
				{
					theField = itsRepository.getField(aFieldId);
					assert theField.getId() == aFieldId;
					Utils.listSet(itsFields, aFieldId, theField);
				}
				
				return theField;
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public ITypeInfo getType(int aTypeId)
		{
			try
			{
				ITypeInfo theType = get(itsTypes, aTypeId);
				if (theType == null)
				{
					theType = itsRepository.getType(aTypeId);
					assert theType.getId() == aTypeId;
					Utils.listSet(itsTypes, aTypeId, theType);
					itsTypesMap.put(theType.getName(), theType);
				}
				
				return theType;
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		public ITypeInfo getType(String aName)
		{
			try
			{
				ITypeInfo theType = itsTypesMap.get(aName);
				if (theType == null)
				{
					theType = itsRepository.getType(aName);
					assert theType.getName().equals(aName);
					Utils.listSet(itsTypes, theType.getId(), theType);
					itsTypesMap.put(theType.getName(), theType);
				}
				
				return theType;
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}

		public Iterable<ILocationInfo> getLocations()
		{
			throw new UnsupportedOperationException();
		}

		public Iterable<IFieldInfo> getFields()
		{
			throw new UnsupportedOperationException();
		}

		public Iterable<String> getFiles()
		{
			throw new UnsupportedOperationException();
		}

		public Iterable<IBehaviorInfo> getBehaviours()
		{
			throw new UnsupportedOperationException();
		}

		public Iterable<ITypeInfo> getTypes()
		{
			throw new UnsupportedOperationException();
		}


		public Stats getStats()
		{
			return itsLastStats;
		}		
	}

}
