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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import tod.core.config.TODConfig;
import tod.core.database.structure.IAdviceInfo;
import tod.core.database.structure.IAspectInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IMutableBehaviorInfo;
import tod.core.database.structure.IMutableFieldInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.IStructureDatabase.LineNumberInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.core.database.structure.IStructureDatabase.Stats;
import tod.impl.database.structure.standard.TagMap;

/**
 * A clone of {@link IStructureDatabase} that is used to create a remotely
 * accessible locations repository.
 * @author gpothier
 */
public interface RIStructureDatabase extends Remote
{
	public void addListener(RIStructureDatabaseListener aListener) throws RemoteException;
	
	public TODConfig getConfig() throws RemoteException;
	public String getId() throws RemoteException;
	public IClassInfo getClass(String aName, String aChecksum, boolean aFailIfAbsent) throws RemoteException;
	public IClassInfo[] getClasses(String aName) throws RemoteException;
	public IClassInfo[] getClasses() throws RemoteException;
	public IClassInfo getClass(String aName, boolean aFailIfAbsent) throws RemoteException;
	public IClassInfo getClass(int aId, boolean aFailIfAbsent) throws RemoteException;
	public IClassInfo getNewClass(String aName) throws RemoteException;
	public ITypeInfo getType(String aName, boolean aFailIfAbsent) throws RemoteException;
	public Stats getStats() throws RemoteException;
	public ProbeInfo[] getProbeInfos(int aAvailableCount) throws RemoteException;
	public int getNewExceptionProbeInfo(int aBehaviorId, int aBytecodeIndex) throws RemoteException;
	public IAdviceInfo getAdvice(int aAdviceId) throws RemoteException;
	public Map<String, IAspectInfo> getAspectInfoMap() throws RemoteException;
	
	public byte[] _getClassBytecode(int aClassId) throws RemoteException;
	public byte[] _getClassOriginalBytecode(int aClassId) throws RemoteException;
	public String _getClassSMAP(int aClassId) throws RemoteException;
	public Map<String, IMutableFieldInfo> _getClassFieldMap(int aClassId) throws RemoteException;
	public Map<String, IMutableBehaviorInfo> _getClassBehaviorsMap(int aClassId) throws RemoteException;
	public List<LocalVariableInfo> _getBehaviorLocalVariableInfo(int aBehaviorId) throws RemoteException;
	public LineNumberInfo[] _getBehaviorLineNumberInfo(int aBehaviorId) throws RemoteException;
	public TagMap _getBehaviorTagMap(int aBehaviorId) throws RemoteException;
	public List<ProbeInfo> _getBehaviorProbes(int aBehaviorId) throws RemoteException;
	public IClassInfo _getBehaviorClass(int aBehaviorId, boolean aFailIfAbsent) throws RemoteException;
	public IClassInfo _getFieldClass(int aFieldId, boolean aFailIfAbsent) throws RemoteException;
}
