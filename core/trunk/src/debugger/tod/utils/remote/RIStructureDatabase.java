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

import java.rmi.Remote;
import java.rmi.RemoteException;

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.IStructureDatabase.Stats;

/**
 * A clone of {@link IStructureDatabase} that is used to create a remotely
 * accessible locations repository.
 * @author gpothier
 */
public interface RIStructureDatabase extends Remote
{
	public void addListener(RIStructureDatabaseListener aListener) throws RemoteException;
	
	public String getId() throws RemoteException;
	public IClassInfo getClass(String aName, String aChecksum, boolean aFailIfAbsent) throws RemoteException;
	public IClassInfo[] getClasses(String aName) throws RemoteException;
	public IClassInfo[] getClasses() throws RemoteException;
	public IClassInfo getClass(String aName, boolean aFailIfAbsent) throws RemoteException;
	public IClassInfo getClass(int aId, boolean aFailIfAbsent) throws RemoteException;
	public ITypeInfo getType(String aName, boolean aFailIfAbsent) throws RemoteException;
	public IFieldInfo getField(int aId, boolean aFailIfAbsent) throws RemoteException;
	public IBehaviorInfo getBehavior(int aId, boolean aFailIfAbsent) throws RemoteException;
	public Stats getStats() throws RemoteException;
}
