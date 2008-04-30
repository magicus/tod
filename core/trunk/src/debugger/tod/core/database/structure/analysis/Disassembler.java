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
package tod.core.database.structure.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceMethodVisitor;

import tod.core.config.TODConfig;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IMutableBehaviorInfo;
import tod.core.database.structure.analysis.DisassembledBehavior.Instruction;
import tod.impl.database.structure.standard.BehaviorInfo;
import tod.impl.database.structure.standard.ClassInfo;
import tod.impl.database.structure.standard.StructureDatabase;
import zz.utils.Utils;

public class Disassembler
{
	public static void main(String[] args) throws IOException
	{
		File f = new File("/home/gpothier/tmp/Fixtures.class");
		byte[] theBytecode = Utils.readInputStream_byte(new FileInputStream(f));
		
		StructureDatabase theStructureDatabase = StructureDatabase.create(new TODConfig());
		ClassInfo theClass = theStructureDatabase.getNewClass("dummy.Dummy");
		theClass.setBytecode(theBytecode);
		
		IMutableBehaviorInfo theBehavior = theClass.getNewBehavior(
				"checkCondition",
				"(Ltod/impl/database/IBidiIterator;Ltod/impl/evdbng/queries/EventCondition;Ltod/impl/evdbng/EventGenerator;I)I");
		
		DisassembledBehavior theDisassembledBehavior = disassemble(theBehavior);
		System.out.println(theDisassembledBehavior);
	}
	
	public static DisassembledBehavior disassemble(IBehaviorInfo aBehavior)
	{
		IClassInfo theClass = aBehavior.getType();
		MethodNode theMethodNode = null;
		ClassReader cr = null;
		
		byte[] theBytecode = theClass.getBytecode();
		if (theBytecode != null)
		{
			cr = new ClassReader(theBytecode);
			ClassNode cn = new ClassNode();
			cr.accept(cn, 0);
			
			// Search the requested method
			String theSig = aBehavior.getSignature();
			for (Iterator theIterator = cn.methods.iterator(); theIterator.hasNext();)
			{
				MethodNode theMethod = (MethodNode) theIterator.next();
				if (! theMethod.name.equals(aBehavior.getName())) continue;
				if (! theMethod.desc.equals(theSig)) continue;
				theMethodNode = theMethod;
				break;
			}
		}
		
		if (theMethodNode == null) return null;
		
		MyTraceMethodVisitor theVisitor = new MyTraceMethodVisitor(cr);
		theMethodNode.accept(theVisitor);
		
		return new DisassembledBehavior(aBehavior, theVisitor.getInstructions());

	}
	
	/**
	 * We subclass ASM's {@link TraceMethodVisitor} so as to capture disassembled
	 * bytecodes.
	 * @author gpothier
	 */
	private static class MyTraceMethodVisitor extends TraceMethodVisitor
	{
		private List<Instruction> itsInstructions = new ArrayList<Instruction>();

		public Instruction[] getInstructions()
		{
			return itsInstructions.toArray(new Instruction[itsInstructions.size()]);
		}
		
		public MyTraceMethodVisitor(ClassReader aReader)
		{
			super (new ClassWriter(aReader, 0).visitMethod(0, "", "", "", null));
		}

		/**
		 * Returns the current bytecode offset.
		 */
		private int getPc()
		{
			Label l = new Label();
			mv.visitLabel(l);
			return l.getOffset();
		}
		
		private void addInstruction(int aPc)
		{
			itsInstructions.add(new Instruction(aPc, buf.toString().trim(), false));
		}
		
		private void addLabel(int aPc)
		{
			itsInstructions.add(new Instruction(aPc, buf.toString().trim(), true));
		}
		
		@Override
		public void visitFieldInsn(int aOpcode, String aOwner, String aName, String aDesc)
		{
			int thePc = getPc();
			super.visitFieldInsn(aOpcode, aOwner, aName, aDesc);
			addInstruction(thePc);
		}

		@Override
		public void visitIincInsn(int aVar, int aIncrement)
		{
			int thePc = getPc();
			super.visitIincInsn(aVar, aIncrement);
			addInstruction(thePc);
		}

		@Override
		public void visitInsn(int aOpcode)
		{
			int thePc = getPc();
			super.visitInsn(aOpcode);
			addInstruction(thePc);
		}

		@Override
		public void visitIntInsn(int aOpcode, int aOperand)
		{
			int thePc = getPc();
			super.visitIntInsn(aOpcode, aOperand);
			addInstruction(thePc);
		}

		@Override
		public void visitJumpInsn(int aOpcode, Label aLabel)
		{
			int thePc = getPc();
			super.visitJumpInsn(aOpcode, aLabel);
			addInstruction(thePc);
		}

		@Override
		public void visitLdcInsn(Object aCst)
		{
			int thePc = getPc();
			super.visitLdcInsn(aCst);
			addInstruction(thePc);
		}

		@Override
		public void visitLookupSwitchInsn(Label aDflt, int[] aKeys, Label[] aLabels)
		{
			int thePc = getPc();
			super.visitLookupSwitchInsn(aDflt, aKeys, aLabels);
			addInstruction(thePc);
		}

		@Override
		public void visitMethodInsn(int aOpcode, String aOwner, String aName, String aDesc)
		{
			int thePc = getPc();
			super.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
			addInstruction(thePc);
		}

		@Override
		public void visitMultiANewArrayInsn(String aDesc, int aDims)
		{
			int thePc = getPc();
			super.visitMultiANewArrayInsn(aDesc, aDims);
			addInstruction(thePc);
		}

		@Override
		public void visitTableSwitchInsn(int aMin, int aMax, Label aDflt, Label[] aLabels)
		{
			int thePc = getPc();
			super.visitTableSwitchInsn(aMin, aMax, aDflt, aLabels);
			addInstruction(thePc);
		}

		@Override
		public void visitTypeInsn(int aOpcode, String aDesc)
		{
			int thePc = getPc();
			super.visitTypeInsn(aOpcode, aDesc);
			addInstruction(thePc);
		}

		@Override
		public void visitVarInsn(int aOpcode, int aVar)
		{
			int thePc = getPc();
			super.visitVarInsn(aOpcode, aVar);
			addInstruction(thePc);
		}

		@Override
		public void visitLabel(Label aLabel)
		{
			int thePc = getPc();
			super.visitLabel(aLabel);
			addLabel(thePc);
		}
		
		
		
	}
}
