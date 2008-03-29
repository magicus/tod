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
package tod.utils;

import java.util.Map;

import tod.core.config.TODConfig;
import tod.core.database.structure.IAdviceInfo;
import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.IAspectInfo;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMutableClassInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.SourceRange;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;

public class DummyStructureDatabase 
implements IMutableStructureDatabase
{
	private static DummyStructureDatabase INSTANCE = new DummyStructureDatabase();

	public static DummyStructureDatabase getInstance()
	{
		return INSTANCE;
	}

	private DummyStructureDatabase()
	{
	}
	
	public TODConfig getConfig()
	{
		return null;
	}

	public IBehaviorInfo getBehavior(int aId, boolean aFailIfAbsent)
	{
		return null;
	}

	public int getBehaviorId(String aClassName, String aMethodName, String aMethodSignature)
	{
		return 0;
	}

	public IBehaviorInfo[] getBehaviors()
	{
		return null;
	}

	public IMutableClassInfo getNewClass(String aName)
	{
		return null;
	}

	public IMutableClassInfo getClass(int aId, boolean aFailIfAbsent)
	{
		return null;
	}

	public IMutableClassInfo getClass(String aName, boolean aFailIfAbsent)
	{
		return null;
	}

	public IClassInfo getClass(String aName, String aChecksum, boolean aFailIfAbsent)
	{
		return null;
	}

	public IClassInfo getUnknownClass()
	{
		return null;
	}

	public IClassInfo[] getClasses(String aName)
	{
		return null;
	}

	public IFieldInfo getField(int aId, boolean aFailIfAbsent)
	{
		return null;
	}

	public String getId()
	{
		return "dummy";
	}

	public ITypeInfo getType(String aName, boolean aFailIfAbsent)
	{
		return null;
	}

	public IClassInfo[] getClasses()
	{
		return null;
	}

	public Stats getStats()
	{
		return null;
	}

	public ITypeInfo getNewType(String aName)
	{
		return null;
	}

	public IArrayTypeInfo getArrayType(ITypeInfo aBaseType, int aDimensions)
	{
		return null;
	}

	public ITypeInfo getType(int aId, boolean aFailIfAbsent)
	{
		return null;
	}

	public int addProbe(int aBehaviorId, int aBytecodeIndex, BytecodeRole aRole, int aAdviceSourceId)
	{
		return 0;
	}

	public void setProbe(int aProbeId, int aBehaviorId, int aBytecodeIndex, BytecodeRole aRole, int aAdviceSourceId)
	{
	}

	public ProbeInfo getProbeInfo(int aProbeId)
	{
		return null;
	}

	public int getProbeCount()
	{
		return 0;
	}

	public void setAdviceSourceMap(Map<Integer, SourceRange> aMap)
	{
	}

	public IAdviceInfo getAdvice(int aAdviceId)
	{
		return null;
	}

	public Map<String, IAspectInfo> getAspectInfoMap()
	{
		return null;
	}

	public ProbeInfo getNewExceptionProbe(int aBehaviorId, int aBytecodeIndex)
	{
		return null;
	}

}
