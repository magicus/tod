/*
 * Created on Sep 11, 2006
 */
package tod.utils.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ITypeInfo;

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

	public Iterable<IClassInfo> getClasses()
	{
		return itsDelegate.getClasses();
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

	public ITypeInfo getType(String aName)
	{
		return itsDelegate.getType(aName);
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

			public Iterable<IClassInfo> getClasses()
			{
				try
				{
					return aRepository.getClasses();
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
		};
	}

}
