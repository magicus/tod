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
package tod.impl.database.structure.standard;

import tod.core.ILogCollector;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.IShareableStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ILocationInfo.ISerializableLocationInfo;


/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a type member (method, constructor, field).
 * @author gpothier
 */
public abstract class MemberInfo extends LocationInfo 
implements IMemberInfo, ISerializableLocationInfo
{
	private static final long serialVersionUID = 1781954680024875732L;

	/**
	 * We keep the type id instead of actual type in order to simplify
	 * the handling of remote structure databases.
	 */
	private int itsTypeId;
	
	private boolean itsStatic;
	
	public MemberInfo(
			IShareableStructureDatabase aDatabase, 
			int aId, 
			ITypeInfo aType, 
			String aName, 
			boolean aStatic)
	{
		super(aDatabase, aId, aName);
		itsTypeId = aType.getId();
		itsStatic = aStatic;
	}
	
	public ITypeInfo getType()
	{
		return getDatabase().getType(itsTypeId, true);
	}
	
	public boolean isStatic()
	{
		return itsStatic;
	}
}
