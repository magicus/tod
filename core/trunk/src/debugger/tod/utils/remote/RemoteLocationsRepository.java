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

import tod.core.ILocationRegistrer.Stats;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.TypeInfo;

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
	
	public RemoteLocationsRepository(ILocationsRepository aDelegate) throws RemoteException
	{
		itsDelegate = aDelegate;
	}

	public ITypeInfo[] getArgumentTypes(String aSignature)
	{
		return itsDelegate.getArgumentTypes(aSignature);
	}
	
	public TypeInfo getReturnType(String aSignature) 
	{
		return itsDelegate.getReturnType(aSignature);
	}

	public IBehaviorInfo getBehavior(int aBehaviorId)
	{
		return itsDelegate.getBehavior(aBehaviorId);
	}

	public IBehaviorInfo getBehavior(ITypeInfo aType, String aName, String aSignature, boolean aSearchAncestors)
	{
		return itsDelegate.getBehavior(aType, aName, aSignature, aSearchAncestors);
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

	public IFieldInfo getField(ITypeInfo aType, String aName, boolean aSearchAncestors)
	{
		return itsDelegate.getField(aType, aName, aSearchAncestors);
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

	public Iterable<ILocationInfo> getLocations() 
	{
		return itsDelegate.getLocations();
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
	public static ILocationsRepository createRepository(final RILocationsRepository aRepository)
	{
		return new ILocationsRepository()
		{
			public ITypeInfo[] getArgumentTypes(String aSignature)
			{
				try
				{
					return aRepository.getArgumentTypes(aSignature);
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}
			
			public TypeInfo getReturnType(String aSignature)
			{
				try
				{
					return aRepository.getReturnType(aSignature);
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}

			public IBehaviorInfo getBehavior(int aBehaviorId)
			{
				try
				{
					return aRepository.getBehavior(aBehaviorId);
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}

			public IBehaviorInfo getBehavior(ITypeInfo aType, String aName, String aSignature, boolean aSearchAncestors)
			{
				try
				{
					return aRepository.getBehavior(aType, aName, aSignature, aSearchAncestors);
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}

			public Iterable<IBehaviorInfo> getBehaviours()
			{
				try
				{
					return aRepository.getBehaviours();
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}

			public Iterable<ITypeInfo> getTypes()
			{
				try
				{
					return aRepository.getTypes();
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
					return aRepository.getField(aFieldId);
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}

			public IFieldInfo getField(ITypeInfo aType, String aName, boolean aSearchAncestors)
			{
				try
				{
					return aRepository.getField(aType, aName, aSearchAncestors);
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}

			public Iterable<IFieldInfo> getFields()
			{
				try
				{
					return aRepository.getFields();
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}

			public Iterable<String> getFiles()
			{
				try
				{
					return aRepository.getFiles();
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}

			public ITypeInfo getType(int aId)
			{
				try
				{
					return aRepository.getType(aId);
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}
			
			public Iterable<ILocationInfo> getLocations()
			{
				try
				{
					return aRepository.getLocations();
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
					return aRepository.getType(aName);
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}

			public Stats getStats()
			{
				try
				{
					return aRepository.getStats();
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}
			
			
		};
	}

}
