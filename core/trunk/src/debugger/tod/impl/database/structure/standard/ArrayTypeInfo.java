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

import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IShareableStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ILocationInfo.ISerializableLocationInfo;

/**
 * Note: This class is not a {@link ISerializableLocationInfo}
 * because it must be recreated at the destination rather than passed
 * through the wire.
 * @author gpothier
 */
public class ArrayTypeInfo extends TypeInfo implements IArrayTypeInfo
{
	private static final long serialVersionUID = 1415897267440123250L;
	private final ITypeInfo itsElementType;
	private final int itsDimensions;
	
	public ArrayTypeInfo(IShareableStructureDatabase aDatabase, ITypeInfo aElementType, int aDimensions)
	{
		super(aDatabase, -1, aElementType.getName()+getBrackets(aDimensions));
		itsElementType = aElementType;
		itsDimensions = aDimensions;
	}
	
	public String getJvmName()
	{
		throw new UnsupportedOperationException();
	}
	
	private static String getBrackets(int aDimensions)
	{
		StringBuilder theBuilder = new StringBuilder();
		for(int i=0;i<aDimensions;i++) theBuilder.append("[]");
		return theBuilder.toString();
	}

	public int getDimensions()
	{
		return itsDimensions;
	}

	public ITypeInfo getElementType()
	{
		return itsElementType;
	}

	public int getSize()
	{
		return 1;
	}

	public boolean isArray()
	{
		return true;
	}

	public boolean isPrimitive()
	{
		return false;
	}

	public boolean isVoid()
	{
		return false;
	}
	
}
