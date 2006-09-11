/*
 * Created on Sep 11, 2006
 */
package tod.utils.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ITypeInfo;

/**
 * A clone of {@link ILocationsRepository} that is used to create a remotely
 * accessible locations repository.
 * @author gpothier
 */
public interface RILocationsRepository extends Remote
{
	public ITypeInfo getType(int aId) throws RemoteException;
	public ITypeInfo getType(String aName) throws RemoteException;
	public IFieldInfo getField(int aFieldId) throws RemoteException;
	public IFieldInfo getField(ITypeInfo aType, String aName, boolean aSearchAncestors) throws RemoteException;
	public IBehaviorInfo getBehavior(int aBehaviorId) throws RemoteException;
	public IBehaviorInfo getBehavior(
			ITypeInfo aType, 
			String aName, 
			String aSignature, 
			boolean aSearchAncestors) throws RemoteException;
	public ITypeInfo[] getArgumentTypes(String aSignature) throws RemoteException;
	public Iterable<IClassInfo> getClasses() throws RemoteException;
	public Iterable<IBehaviorInfo> getBehaviours() throws RemoteException;
	public Iterable<IFieldInfo> getFields() throws RemoteException;
	public Iterable<String> getFiles() throws RemoteException;
}
