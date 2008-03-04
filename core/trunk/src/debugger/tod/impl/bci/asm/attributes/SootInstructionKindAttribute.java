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
package tod.impl.bci.asm.attributes;

import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.IBehaviorInfo.BytecodeTagType;
import tod.impl.database.structure.standard.TagMap;

/**
 * This attribute determines the role of each bytecode
 * @author gpothier
 */
public class SootInstructionKindAttribute extends SootIntAttribute
{
	public static final String NAME = "ca.mcgill.sable.InstructionKind";

	public SootInstructionKindAttribute(Entry<Integer>[] aEntries)
	{
		super(NAME, aEntries);
	}

	public SootInstructionKindAttribute()
	{
		super(NAME);
	}

	@Override
	protected void fillTagMap(TagMap aTagMap, int aStart, int aEnd, Integer aValue)
	{
		aTagMap.putTagRange(BytecodeTagType.ROLE, getRole(aValue), aStart, aEnd);
	}
	
	/**
	 * Returns the TOD {@link BytecodeRole} that corresponds to the given soot instruction kind
	 * constant.
	 */
	private static BytecodeRole getRole(int aSootConstant) 
	{
		// Constants come from abc.weaving.tagkit.InstructionKindTag
		switch (aSootConstant)
		{
		case 0: return BytecodeRole.BASE_CODE;
		case 1: return BytecodeRole.ADVICE_EXECUTE;
		case 2: return BytecodeRole.ADVICE_ARG_SETUP;
		case 4: return BytecodeRole.AFTER_THROWING_HANDLER;
		case 5: return BytecodeRole.EXCEPTION_SOFTENER;
		case 3: return BytecodeRole.ADVICE_TEST;
		case 36: return BytecodeRole.INLINED_ADVICE;
		case 41: return BytecodeRole.CONTEXT_EXPOSURE;
		case 42: return BytecodeRole.PARAMETER_BACKUP;
		default: return BytecodeRole.UNKNOWN;
		}
	}

}
