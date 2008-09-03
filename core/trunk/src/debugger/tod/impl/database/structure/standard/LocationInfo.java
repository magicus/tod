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

import java.io.Serializable;

import tod.core.database.structure.IMutableLocationInfo;
import tod.core.database.structure.IShareableStructureDatabase;
import zz.utils.PublicCloneable;

/**
 * Base class for aggregation of location information.
 * @author gpothier
 */
public abstract class LocationInfo extends PublicCloneable 
implements IMutableLocationInfo, Serializable
{
	private static final long serialVersionUID = 7811546902517644811L;
	/**
	 * A flag that can be used to check if this location info is local or remote. 
	 */
	private transient Boolean itsOriginal;
	private transient IShareableStructureDatabase itsDatabase;
	private final int itsId;
	private String itsName;
	
	private String itsSourceFile;

	
	public LocationInfo(IShareableStructureDatabase aDatabase, int aId)
	{
		itsOriginal = true;
		itsDatabase = aDatabase;
		itsId = aId;
	}

	public LocationInfo(IShareableStructureDatabase aDatabase, int aId, String aName)
	{
		itsOriginal = true;
		itsDatabase = aDatabase;
		itsId = aId;
		setName(aName);
	}
	
	/**
	 * Whether this location info is the original.
	 * @return True if original, false if remote version
	 */
	protected boolean isOriginal()
	{
		return itsOriginal != null;
	}
	
	public int getId()
	{
		return itsId;
	}
	
	public String getName()
	{
		return itsName;
	}

	public IShareableStructureDatabase getDatabase()
	{
		return itsDatabase;
	}
	
	public IShareableStructureDatabase _getMutableDatabase()
	{
		return getDatabase();
	}
	
	public void setDatabase(IShareableStructureDatabase aDatabase)
	{
		assert itsDatabase == null;
		itsDatabase = aDatabase;
	}
	
	/**
	 * This is used for defered type registration.
	 */ 
	public void setName(String aName)
	{
		assert itsName == null || itsName.equals(aName);
		itsName = aName;
	}
	
	protected void changeName(String aName)
	{
		itsName = aName;
	}

	public String getSourceFile()
	{
		return itsSourceFile;
	}

	public void setSourceFile(String aSourceFile)
	{
		itsSourceFile = aSourceFile;
	}

	@Override
	public final int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((itsDatabase == null) ? 0 : itsDatabase.hashCode());
		result = prime * result + itsId;
		return result;
	}

	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final LocationInfo other = (LocationInfo) obj;
		if (itsDatabase == null)
		{
			if (other.itsDatabase != null) return false;
		}
		else if (!itsDatabase.equals(other.itsDatabase)) return false;
		if (itsId != other.itsId) return false;
		return true;
	}
	
	
}
