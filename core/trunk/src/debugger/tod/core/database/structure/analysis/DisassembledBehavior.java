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
package tod.core.database.structure.analysis;

import tod.core.database.structure.IBehaviorInfo;

/**
 * Utility class that provides a disassembled version of a behavior's
 * bytecode.
 * @author gpothier
 */
public class DisassembledBehavior
{
	private final IBehaviorInfo itsBehavior;
	private final Instruction[] itsInstructions;

	public DisassembledBehavior(IBehaviorInfo aBehavior, Instruction[] aInstructions)
	{
		itsBehavior = aBehavior;
		itsInstructions = aInstructions;
	}
	
	public IBehaviorInfo getBehavior()
	{
		return itsBehavior;
	}

	public Instruction[] getInstructions()
	{
		return itsInstructions;
	}
	
	/**
	 * Information regarding a single bytecode instruction.
	 * @author gpothier
	 */
	public static class Instruction
	{
		/**
		 * Address of the instruction in the behavior's bytecode.
		 */
		public final int pc;
		
		/**
		 * Bytecode mnemonic and arguments in human-readable form
		 */
		public final String text;
		
		/**
		 * Whether this instruction is actually a label.
		 */
		public final boolean label;

		public Instruction(int aPc, String aText, boolean aLabel)
		{
			pc = aPc;
			text = aText;
			label = aLabel;
		}
		
		@Override
		public String toString()
		{
			return pc + " " + text;
		}
	}


}
