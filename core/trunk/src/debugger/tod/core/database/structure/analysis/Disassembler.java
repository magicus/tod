/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
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

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.analysis.DisassembledBehavior.Instruction;
import tod.impl.database.structure.standard.BehaviorInfo;
import tod.impl.database.structure.standard.ClassInfo;
import zz.utils.Utils;

public class Disassembler
{
	public static void main(String[] args) throws IOException
	{
		File f = new File("bin/dummy/Dummy.class");
		byte[] theBytecode = Utils.readInputStream_byte(new FileInputStream(f));
		
		ClassInfo theClass = new ClassInfo(null, null, "dummy.Dummy", 1);
		theClass.setBytecode(theBytecode);
		
		BehaviorInfo theBehavior = new BehaviorInfo(
				null, 
				2,
				theClass, 
				"foo",
				"(Ljava/lang/Object;J)I",
				null,
				null);
		
		DisassembledBehavior theDisassembledBehavior = disassemble(theBehavior);
		System.out.println(theDisassembledBehavior);
	}
	
	public static DisassembledBehavior disassemble(IBehaviorInfo aBehavior)
	{
		IClassInfo theClass = aBehavior.getType();
		byte[] theBytecode = theClass.getBytecode();
		
		if (theBytecode == null) return null;
		
		ClassReader cr = new ClassReader(theBytecode);
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);
		
		// Search the requested method
		MethodNode theMethodNode = null;
//		String theSig = LocationUtils.getDescriptor(aBehavior);
		String theSig = aBehavior.getSignature();
		for (Iterator theIterator = cn.methods.iterator(); theIterator.hasNext();)
		{
			MethodNode theMethod = (MethodNode) theIterator.next();
			if (! theMethod.name.equals(aBehavior.getName())) continue;
			if (! theMethod.desc.equals(theSig)) continue;
			theMethodNode = theMethod;
			break;
		}
		
		MyTraceMethodVisitor theVisitor = new MyTraceMethodVisitor();
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
		
		public MyTraceMethodVisitor()
		{
			super (new ClassWriter(0).visitMethod(0, "", "", "", null));
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
