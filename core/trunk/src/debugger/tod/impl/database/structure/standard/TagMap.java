/*
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.database.structure.IBehaviorInfo.BytecodeTagType;
import zz.utils.Utils;

/**
 * Manages the association between bytecode and tags.
 * There are various types of tags (see {@link BytecodeTagType}),
 * and each bytecode can have a tag for each type.
 * @author gpothier
 */
public class TagMap implements Serializable
{
	private static final long serialVersionUID = 1045425445284384491L;
	
	private final Map<String, List> itsTagsMap = new HashMap<String, List>();

	/**
	 * Returns the tag associated to a given bytecode.
	 */
	public <T> T getTag(BytecodeTagType<T> aType, int aBytecodeIndex)
	{
		List<T> theTags = itsTagsMap.get(aType.getName());
		return theTags != null ? 
				Utils.listGet(theTags, aBytecodeIndex)
				: null;
	}

	private <T> List<T> getTags(BytecodeTagType<T> aType)
	{
		List<T> theTags = itsTagsMap.get(aType.getName());
		if (theTags == null)
		{
			theTags = new ArrayList<T>();
			itsTagsMap.put(aType.getName(), theTags);
		}

		return theTags;
	}
	
	/**
	 * Adds a tag to a specific bytecode.
	 */
	public <T> void putTag(BytecodeTagType<T> aType, T aTag, int aBytecodeIndex)
	{
		Utils.listSet(getTags(aType), aBytecodeIndex, aTag);
	}
	
	/**
	 * Adds a tag to a range of bytecode.
	 * @param aType The tag type for which the tag is being set.
	 * @param aTag The tag value
	 * @param aStart The start of the range, inclusive.
	 * @param aEnd The end of the range, exclusive.
	 */
	public <T> void putTagRange(BytecodeTagType<T> aType, T aTag, int aStart, int aEnd)
	{
		List<T> theTags = getTags(aType);
		for(int i=aStart;i<aEnd;i++) Utils.listSet(theTags, i, aTag);
	}

}
