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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;

import tod.impl.bci.asm.attributes.SootAttribute;
import tod.impl.database.structure.standard.TagMap;
import zz.utils.Utils;


/**
 * This visitor simply records information about each method.
 * It should be run in a first pass, before actual instrumentation
 * is done. 
 * @author gpothier
 */
public class InfoCollector extends ClassAdapter
{
	private List<ASMMethodInfo> itsMethodsInfo = new ArrayList<ASMMethodInfo>();
	private ASMMethodInfo itsCurrentMethodInfo;
	
	public InfoCollector()
	{
		super(new ClassWriter(0)); // The ClassWriter is necessary to obtain instructions' pcs.
	}

	public ASMMethodInfo getMethodInfo (int aIndex)
	{
		return itsMethodsInfo.get(aIndex);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String aName, String aDesc, String aSignature, String[] aExceptions)
	{
		MethodVisitor mv = super.visitMethod(access, aName, aDesc, aSignature, aExceptions);
		itsCurrentMethodInfo = new ASMMethodInfo(aName, aDesc, BCIUtils.isStatic(access));
		itsMethodsInfo.add(itsCurrentMethodInfo);
		return new JSRAnalyser(new MethodAttributesCollector(new MyCounter(mv)));
	}
	
	private class MethodAttributesCollector extends MethodAdapter
	{
		private List<SootAttribute> itsSootAttributes = new ArrayList<SootAttribute>();
		private int itsCodeSize;
		
		public MethodAttributesCollector(MethodVisitor mv)
		{
			super(mv);
		}

		@Override
		public void visitAttribute(Attribute aAttr)
		{
			if (aAttr instanceof SootAttribute)
			{
				SootAttribute theAttribute = (SootAttribute) aAttr;
				itsSootAttributes.add(theAttribute);
			}

			super.visitAttribute(aAttr);
		}
		
		@Override
		public void visitMaxs(int aMaxStack, int aMaxLocals)
		{
			super.visitMaxs(aMaxStack, aMaxLocals);
			Label theLabel = new Label();
			mv.visitLabel(theLabel);
			itsCodeSize = theLabel.getOffset();
		}
		
		@Override
		public void visitEnd()
		{
			super.visitEnd();
			TagMap theTagMap = new TagMap();
			
			for (SootAttribute theAttribute : itsSootAttributes)
			{
				theAttribute.fillTagMap(theTagMap, itsCodeSize);
			}
			
			itsCurrentMethodInfo.setTagMap(theTagMap);
		}
	}
	
	private class MyCounter extends InstructionCounterAdapter
	{
		private List<Integer> itsPcs = new ArrayList<Integer>();
		
		public MyCounter(MethodVisitor aMv)
		{
			super(aMv);
		}

		@Override
		protected void insn(int aRank, int aPc)
		{
			Utils.listSet(itsPcs, aRank, aPc);
		}
		
		@Override
		public void visitEnd()
		{
			super.visitEnd();
			int[] theOriginalPcs = new int[itsPcs.size()];
			for (int i=0;i<itsPcs.size();i++) theOriginalPcs[i] = itsPcs.get(i);
			itsCurrentMethodInfo.setOriginalPc(theOriginalPcs);
		}
	}
	
	/**
	 * This method visitor is used to perform a first pass over a method
	 * to register JSR targets, so that the following astore is not
	 * taken into account as a local variable store.
	 * @author gpothier
	 */
	private class JSRAnalyser extends MethodAdapter implements Opcodes
	{
		private Label itsLastLabel;
		
		private Map<Label, StoreInfo> itsLabelToStoreInfo = new HashMap<Label, StoreInfo>();
		private List<StoreInfo> itsStoreInfos = new ArrayList<StoreInfo>();

		public JSRAnalyser(MethodVisitor mv)
		{
			super(mv);
		}

		@Override
		public void visitMaxs(int aMaxStack, int aMaxLocals)
		{
			itsCurrentMethodInfo.setMaxLocals(aMaxLocals);
			super.visitMaxs(aMaxStack, aMaxLocals);
		}


		@Override
		public void visitEnd()
		{
//			System.out.println("Ignore for: "+itsCurrentMethodInfo.getName());
			boolean[] theIgnoreStores = new boolean[itsStoreInfos.size()];
			for(int i=0;i<itsStoreInfos.size();i++)
			{
				theIgnoreStores[i] = itsStoreInfos.get(i).ignore;
//				System.out.println(" "+i+": "+theIgnoreStores[i]);
			}
			itsCurrentMethodInfo.setIgnoreStores(theIgnoreStores);
			super.visitEnd();
		}
		
		@Override
		public void visitLabel(Label aLabel)
		{
			itsLastLabel = aLabel;
			super.visitLabel(aLabel);
		}

		@Override
		public void visitFieldInsn(int aOpcode, String aOwner, String aName, String aDesc)
		{
			itsLastLabel = null;
			super.visitFieldInsn(aOpcode, aOwner, aName, aDesc);
		}

		@Override
		public void visitIincInsn(int aVar, int aIncrement)
		{
			itsLastLabel = null;
			super.visitIincInsn(aVar, aIncrement);
		}

		@Override
		public void visitInsn(int aOpcode)
		{
			itsLastLabel = null;
			super.visitInsn(aOpcode);
		}

		@Override
		public void visitIntInsn(int aOpcode, int aOperand)
		{
			itsLastLabel = null;
			super.visitIntInsn(aOpcode, aOperand);
		}

		@Override
		public void visitLdcInsn(Object aCst)
		{
			itsLastLabel = null;
			super.visitLdcInsn(aCst);
		}

		@Override
		public void visitLookupSwitchInsn(Label aDflt, int[] aKeys, Label[] aLabels)
		{
			itsLastLabel = null;
			super.visitLookupSwitchInsn(aDflt, aKeys, aLabels);
		}

		@Override
		public void visitMethodInsn(int aOpcode, String aOwner, String aName, String aDesc)
		{
			itsLastLabel = null;
			super.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
		}

		@Override
		public void visitMultiANewArrayInsn(String aDesc, int aDims)
		{
			itsLastLabel = null;
			super.visitMultiANewArrayInsn(aDesc, aDims);
		}

		@Override
		public void visitTableSwitchInsn(int aMin, int aMax, Label aDflt, Label[] aLabels)
		{
			itsLastLabel = null;
			super.visitTableSwitchInsn(aMin, aMax, aDflt, aLabels);
		}

		@Override
		public void visitTypeInsn(int aOpcode, String aDesc)
		{
			itsLastLabel = null;
			super.visitTypeInsn(aOpcode, aDesc);
		}

		@Override
		public void visitVarInsn(int aOpcode, int aVar)
		{
			if (aOpcode >= ISTORE && aOpcode < IASTORE)
			{
				registerStore(itsLastLabel);
			}
			super.visitVarInsn(aOpcode, aVar);
		}
		
		@Override
		public void visitJumpInsn(int aOpcode, Label aLabel)
		{
			if (aOpcode == JSR)
			{
				ignoreStore(aLabel);
			}
			super.visitJumpInsn(aOpcode, aLabel);
		}
		
	    /**
	     * Identifies a store instruction to be ignored.
	     * @param aLabel Label preceding the store to ignore
	     */
	    private void ignoreStore(Label aLabel)
	    {
	    	StoreInfo theStoreInfo = itsLabelToStoreInfo.get(aLabel);
	    	if (theStoreInfo == null)
	    	{
	    		theStoreInfo = new StoreInfo();
	    		itsLabelToStoreInfo.put(aLabel, theStoreInfo);
	    	}
	    	theStoreInfo.ignore = true;
	    }
	    
	    /**
	     * Registers an actual store instruction.
	     * The moment this method is called defines the store's rank.
	     * @param aLabel Label preceding the store
	     */
	    private void registerStore(Label aLabel)
	    {
	    	StoreInfo theStoreInfo = itsLabelToStoreInfo.get(aLabel);
	    	if (theStoreInfo == null)
	    	{
	    		theStoreInfo = new StoreInfo();
	    		itsLabelToStoreInfo.put(aLabel, theStoreInfo);
	    	}
	    	itsStoreInfos.add(theStoreInfo);
	    }
	}
	
	/**
	 * Info about a xSTORE instruction
	 * @author gpothier
	 */
	public static class StoreInfo
	{
		public boolean ignore = false;
	}
	
	
}
