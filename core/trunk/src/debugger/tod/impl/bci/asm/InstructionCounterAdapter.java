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
package tod.impl.bci.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * This method adapter counts the input instructions.
 * @author gpothier
 */
public class InstructionCounterAdapter extends MethodAdapter
{
	private int itsCount = 0;
	
	public InstructionCounterAdapter(MethodVisitor mv)
	{
		super(mv);
	}

	public int getCount()
	{
		return itsCount;
	}
	
	protected void insn(int aRank, int aPc)
	{
	}
	
	private void registerInsn()
	{
		Label l = new Label();
		super.visitLabel(l);
		insn(itsCount, l.getOffset());
		itsCount++;
	}

	@Override
	public void visitFieldInsn(int aOpcode, String aOwner, String aName, String aDesc)
	{
		super.visitFieldInsn(aOpcode, aOwner, aName, aDesc);
		registerInsn();
	}

	@Override
	public void visitIincInsn(int aVar, int aIncrement)
	{
		super.visitIincInsn(aVar, aIncrement);
		registerInsn();
	}

	@Override
	public void visitInsn(int aOpcode)
	{
		super.visitInsn(aOpcode);
		registerInsn();
	}

	@Override
	public void visitIntInsn(int aOpcode, int aOperand)
	{
		super.visitIntInsn(aOpcode, aOperand);
		registerInsn();
	}

	@Override
	public void visitJumpInsn(int aOpcode, Label aLabel)
	{
		super.visitJumpInsn(aOpcode, aLabel);
		registerInsn();
	}

	@Override
	public void visitLdcInsn(Object aCst)
	{
		super.visitLdcInsn(aCst);
		registerInsn();
	}

	@Override
	public void visitLookupSwitchInsn(Label aDflt, int[] aKeys, Label[] aLabels)
	{
		super.visitLookupSwitchInsn(aDflt, aKeys, aLabels);
		registerInsn();
	}

	@Override
	public void visitMethodInsn(int aOpcode, String aOwner, String aName, String aDesc)
	{
		super.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
		registerInsn();
	}

	@Override
	public void visitMultiANewArrayInsn(String aDesc, int aDims)
	{
		super.visitMultiANewArrayInsn(aDesc, aDims);
		registerInsn();
	}

	@Override
	public void visitTableSwitchInsn(int aMin, int aMax, Label aDflt, Label[] aLabels)
	{
		super.visitTableSwitchInsn(aMin, aMax, aDflt, aLabels);
		registerInsn();
	}

	@Override
	public void visitTypeInsn(int aOpcode, String aDesc)
	{
		super.visitTypeInsn(aOpcode, aDesc);
		registerInsn();
	}

	@Override
	public void visitVarInsn(int aOpcode, int aVar)
	{
		super.visitVarInsn(aOpcode, aVar);
		registerInsn();
	}
}
