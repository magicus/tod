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
package tod.utils.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import tod.core.config.TODConfig;
import tod.core.database.structure.IAdviceInfo;
import tod.core.database.structure.IAspectInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IMutableBehaviorInfo;
import tod.core.database.structure.IMutableFieldInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.SourceRange;
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
	public Map<String, IMutableFieldInfo> _getClassFieldMap(int aClassId) throws RemoteException;
	public Map<String, IMutableBehaviorInfo> _getClassBehaviorsMap(int aClassId) throws RemoteException;
	public LocalVariableInfo[] _getBehaviorLocalVariableInfo(int aBehaviorId) throws RemoteException;
	public LineNumberInfo[] _getBehaviorLineNumberInfo(int aBehaviorId) throws RemoteException;
	public TagMap _getBehaviorTagMap(int aBehaviorId) throws RemoteException;
	public IClassInfo _getBehaviorClass(int aBehaviorId, boolean aFailIfAbsent) throws RemoteException;
	public IClassInfo _getFieldClass(int aFieldId, boolean aFailIfAbsent) throws RemoteException;
}
