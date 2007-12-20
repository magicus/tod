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
package tod.impl.bci.asm;

import java.util.List;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import tod.Util;
import tod.core.BehaviorCallType;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IMutableBehaviorInfo;
import tod.core.database.structure.IMutableClassInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import zz.utils.ArrayStack;
import zz.utils.Stack;

/**
 * The ASM visitor that instruments the classes.
 * @author gpothier
 */
public class LogBCIVisitor extends ClassAdapter implements Opcodes
{
	private static final int LOG = 1;
	
	private static final boolean TRACE_FIELD = true;
	private static final boolean TRACE_VAR = true;
	private static final boolean TRACE_CALL = true;
	private static final boolean TRACE_ENVELOPPE = true;
	private static final boolean TRACE_ARRAY = true;
	public static final boolean TRACE_ENTRY = true;
	public static final boolean TRACE_EXIT = true;
	
	public static final boolean ENABLE_READY_CHECK = false;
	
	/**
	 * If set to true, features that prevent bytecode verification will
	 * not be used.
	 */
	public static final boolean ENABLE_VERIFY = false;

	private boolean itsModified = false;
	
	private boolean itsTrace;
	private boolean itsInterface;
	
	private IMutableClassInfo itsClassInfo;
	private IClassInfo itsSuperclass;
	private String itsChecksum;
	

	private final InfoCollector itsInfoCollector;
	private int itsCurrentMethodIndex = 0;

	private final IMutableStructureDatabase itsDatabase;
	private final ASMDebuggerConfig itsConfig;

	/**
	 * This list will be filled with the ids of traced methods.
	 */
	private final List<Integer> itsTracedMethods;


	
	public LogBCIVisitor(
			IMutableStructureDatabase aDatabase,
			ASMDebuggerConfig aConfig,
			InfoCollector aInfoCollector, 
			ClassVisitor aVisitor, 
			String aChecksum,
			List<Integer> aTracedMethods)
	{
		super(aVisitor);
		itsDatabase = aDatabase;
		itsChecksum = aChecksum;
		itsInfoCollector = aInfoCollector;
		itsConfig = aConfig;
		itsTracedMethods = aTracedMethods;
	}

	/**
	 * Indicates if this visitor modified the original class.
	 */
	public boolean isModified()
	{
		return itsModified;
	}
	
	private void markModified()
	{
		itsModified = true;
	}
	
	@Override
	public void visit(
			int aVersion, 
			int access, 
			String aName, 
			String aSignature, 
			String aSuperName, 
			String[] aInterfaces)
	{
		itsInterface = BCIUtils.isInterface(access);
		itsClassInfo = itsDatabase.getNewClass(Util.jvmToScreen(aName));
		itsSuperclass = itsInterface || aSuperName == null ? 
				null
				: itsDatabase.getNewClass(Util.jvmToScreen(aSuperName));
		
		IClassInfo[] theInterfaces = new IClassInfo[aInterfaces != null ? aInterfaces.length : 0];
		if (aInterfaces != null) for (int i = 0; i < aInterfaces.length; i++)
		{
			String theInterface = aInterfaces[i];
			theInterfaces[i] = itsDatabase.getNewClass(Util.jvmToScreen(theInterface));
		}
		
		// Check if we should trace operations in the class
		itsTrace = BCIUtils.acceptClass(aName, itsConfig.getGlobalSelector())
				&& BCIUtils.acceptClass(aName, itsConfig.getTraceSelector());
			
		itsClassInfo.setup(itsInterface, itsTrace, itsChecksum, theInterfaces, itsSuperclass);
		
		if (! itsInterface && itsTrace) markModified();
		
		super.visit(aVersion, access, aName, aSignature, aSuperName, aInterfaces);
	}
	
	public IMutableClassInfo getClassInfo()
	{
		return itsClassInfo;
	}
	
	@Override
	public MethodVisitor visitMethod(
			int access, 
			String aName, 
			String aDesc, 
			String aSignature, 
			String[] aExceptions)
	{
		ASMMethodInfo theMethodInfo = 
			itsInfoCollector.getMethodInfo(itsCurrentMethodIndex++);
		
		MethodVisitor mv = super.visitMethod(access, aName, aDesc, aSignature, aExceptions);
		if (mv != null
				&& (access & ACC_NATIVE) == 0
				/*&& (access & ACC_ABSTRACT) == 0*/) 
		{
			return new InstantiationAnalyserVisitor (
					new BCIMethodVisitor(mv, theMethodInfo), 
					theMethodInfo);			
		}
		else return mv;
	}
	
	private class BCIMethodVisitor extends MethodAdapter
	{
		private ASMMethodInfo itsMethodInfo;
		private ASMBehaviorInstrumenter itsInstrumenter;
		
		private IMutableBehaviorInfo itsBehavior;
		private int itsStoreIndex = 0;

		public BCIMethodVisitor(MethodVisitor mv, ASMMethodInfo aMethodInfo)
		{
			super (mv);
			itsMethodInfo = aMethodInfo;
			itsBehavior = itsClassInfo.getNewBehavior(
					itsMethodInfo.getName(),
					itsMethodInfo.getDescriptor());
			
			if (itsTrace)
			{
				itsTracedMethods.add(itsBehavior.getId());
			}
			
			itsInstrumenter = new ASMBehaviorInstrumenter(
					itsConfig,
					mv, 
					itsBehavior,
					itsMethodInfo);
			
			if (LOG>=2) System.out.println("Processing method "+itsMethodInfo.getName()+itsMethodInfo.getDescriptor());
		}
		
		@Override
		public void visitAttribute(Attribute aAttr)
		{
			super.visitAttribute(aAttr);
		}

		@Override
		public void visitTypeInsn(int aOpcode, String aDesc)
		{
			if (itsTrace && aOpcode == ANEWARRAY && TRACE_ARRAY)
			{
				ITypeInfo theType = itsDatabase.getNewType('L'+aDesc+';');
				itsInstrumenter.newArray(ASMBehaviorInstrumenter.createNewArrayClosure(aDesc), theType.getId());
			}
			else mv.visitTypeInsn(aOpcode, aDesc);
		}
		
		public void visitSuperOrThisCallInsn(int aOpcode, String aOwner, String aName, String aDesc)
		{
			if (itsTrace && TRACE_CALL && ! ENABLE_VERIFY) itsInstrumenter.methodCall(aOpcode, aOwner, aName, aDesc, BehaviorCallType.SUPER_CALL);
			else mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
		}
		
		public void visitConstructorCallInsn(int aOpcode, String aOwner, int aCalledTypeId, String aName, String aDesc)
		{
			if (itsTrace && TRACE_CALL && ! ENABLE_VERIFY) itsInstrumenter.methodCall(aOpcode, aOwner, aName, aDesc, BehaviorCallType.INSTANTIATION);
			else mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
		}
		
		/**
		 * Hook method calls to log before and after method call events.
		 */
		@Override
		public void visitMethodInsn(int aOpcode, String aOwner, String aName, String aDesc)
		{
			if (aOpcode == INVOKESPECIAL 
					&& "<init>".equals(aName))
			{
				throw new RuntimeException("Should have been filtered");
			}
			
			if (itsTrace && TRACE_CALL) itsInstrumenter.methodCall(aOpcode, aOwner, aName, aDesc, BehaviorCallType.METHOD_CALL);
			else mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
		}
		
		@Override
		public void visitFieldInsn(int aOpcode, String aOwner, String aName, String aDesc)
		{
			if (itsTrace && TRACE_FIELD && (aOpcode == PUTFIELD || aOpcode == PUTSTATIC))
			{
				itsInstrumenter.fieldWrite(aOpcode, aOwner, aName, aDesc);
			}
			else mv.visitFieldInsn(aOpcode, aOwner, aName, aDesc);
		}
		
		@Override
		public void visitVarInsn(int aOpcode, int aVar)
		{
			if (itsTrace && TRACE_VAR && aOpcode >= ISTORE && aOpcode < IASTORE)
			{
				if (! itsMethodInfo.shouldIgnoreStore(itsStoreIndex))
				{
					itsInstrumenter.variableWrite(aOpcode, aVar);
				}
				else 
				{
					mv.visitVarInsn(aOpcode, aVar);
				}
				
				itsStoreIndex++;
			}
			else mv.visitVarInsn(aOpcode, aVar);
		}
		
		@Override
		public void visitIincInsn(int aVar, int aIncrement)
		{
			if (itsTrace && TRACE_VAR)
			{
				itsInstrumenter.variableInc(aVar, aIncrement);
			}
			else mv.visitIincInsn(aVar, aIncrement);
		}
		
		/**
		 * <li>Replace RETURN bytecodes by GOTOs to the return hooks defined in
		 * {@link #visitCode()}</li>
		 */
		@Override
		public void visitInsn(int aOpcode)
		{
			if (aOpcode >= IRETURN && aOpcode <= RETURN && itsTrace && TRACE_ENVELOPPE)
			{
				itsInstrumenter.doReturn(aOpcode);
			}
			else if (aOpcode >= IASTORE && aOpcode <= SASTORE && itsTrace && TRACE_ARRAY)
			{
				itsInstrumenter.arrayWrite(aOpcode);
			}
			else super.visitInsn(aOpcode);
		}
		
		@Override
		public void visitIntInsn(int aOpcode, int aOperand)
		{
			if (itsTrace && aOpcode == NEWARRAY && TRACE_ARRAY)
			{
				ITypeInfo theType = BCIUtils.getPrimitiveType(aOperand);
				itsInstrumenter.newArray(ASMBehaviorInstrumenter.createNewArrayClosure(aOperand), theType.getId());
			}
			else super.visitIntInsn(aOpcode, aOperand);
		}
		
		/**
		 * Check if we must insert entry hooks. 
		 */
		@Override
		public void visitCode()
		{
			// Note: this method can be called if the currently visited type
			// is an interface (<clinit>).
			if (itsTrace && TRACE_ENVELOPPE) itsInstrumenter.insertEntryHooks();
			super.visitCode();
		}
		
		@Override
		public void visitMaxs(int aMaxStack, int aMaxLocals)
		{
			Label theLabel = new Label();
			mv.visitLabel(theLabel);
			int theCodeSize = theLabel.getOffset();
			if (theCodeSize > 65535) 
			{
				System.err.println("Method size overflow: "+itsMethodInfo.getName());
				
				throw new RuntimeException(String.format(
						"[TOD] Fatal error: method %s.%s is too large to be instrumented. \n" +
						"Please exclude %s from scope and retry.",
						itsClassInfo.getName(),
						itsMethodInfo.getName(),
						itsClassInfo.getName()));
			}
			itsMethodInfo.setCodeSize(theCodeSize);
			
			if (itsTrace && TRACE_ENVELOPPE) itsInstrumenter.endHooks();			
			super.visitMaxs(aMaxStack, aMaxLocals);
		}
		
		@Override
		public void visitLineNumber(int aLine, Label aStart)
		{
			itsMethodInfo.addLineNumber(new ASMLineNumberInfo(aStart, aLine));
			mv.visitLineNumber(aLine, aStart);
		}
		
		@Override
		public void visitLocalVariable(String aName, String aDesc, String aSignature, Label aStart, Label aEnd, int aIndex)
		{
			itsMethodInfo.addLocalVariable(new ASMLocalVariableInfo(aStart, aEnd, aName, aDesc, aIndex));
			mv.visitLocalVariable(aName, aDesc, aSignature, aStart, aEnd, aIndex);
		}

		@Override
		public void visitEnd()
		{
			itsBehavior.setup(
					itsTrace,
					itsMethodInfo.getKind(),
					itsMethodInfo.getCodeSize(),
					itsMethodInfo.createLineNumberTable(), 
					itsMethodInfo.createLocalVariableTable());

			super.visitEnd();
		}
	}
	
	private class InstantiationInfo
	{
		/**
		 * Id of the instantiated type
		 */
		private int itsInstantiatedTypeId;
		
		public InstantiationInfo(String aTypeDesc)
		{
			IClassInfo theClass = itsDatabase.getNewClass(Util.jvmToScreen(aTypeDesc));
			itsInstantiatedTypeId = theClass.getId();
		}

		public int getInstantiatedTypeId()
		{
			return itsInstantiatedTypeId;
		}

		
	}

	/**
	 * This visitor acts as a filter before {@link BCIMethodVisitor}. It permits
	 * to detect if a NEW is followed by a DUP or not.
	 * @author gpothier
	 */
	private class InstantiationAnalyserVisitor extends MethodAdapter
	{
		private BCIMethodVisitor mv;
		private Stack<InstantiationInfo> itsInstantiationInfoStack = new ArrayStack<InstantiationInfo>();
		private final ASMMethodInfo itsMethodInfo;
		
		public InstantiationAnalyserVisitor(BCIMethodVisitor mv, ASMMethodInfo aMethodInfo)
		{
			super (mv);
			this.mv = mv;
			itsMethodInfo = aMethodInfo;
		}

		@Override
		public void visitTypeInsn(int aOpcode, String aDesc)
		{
			if (aOpcode == NEW)
			{
				itsInstantiationInfoStack.push(new InstantiationInfo(aDesc));
			}
			mv.visitTypeInsn(aOpcode, aDesc);
		}
		
		@Override
		public void visitMethodInsn(int aOpcode, String aOwner, String aName, String aDesc)
		{
			if (aOpcode == INVOKESPECIAL && "<init>".equals(aName))
			{
				// We are invoking a constructor.
				
				IClassInfo theClass = itsDatabase.getNewClass(Util.jvmToScreen(aOwner));
				int theCalledTypeId = theClass.getId();
				
				if ("<init>".equals(itsMethodInfo.getName()))
				{
					// We are in a constructor
					if (theCalledTypeId == itsClassInfo.getSupertype().getId() 
							|| theCalledTypeId == itsClassInfo.getId())
					{
						// Potential call to super or this
						InstantiationInfo theInfo = itsInstantiationInfoStack.peek();
						if (theInfo == null || theInfo.getInstantiatedTypeId() != theCalledTypeId)
						{
							mv.visitSuperOrThisCallInsn(aOpcode, aOwner, aName, aDesc);
							return;
						}
					}
				}
				
				InstantiationInfo theInfo = itsInstantiationInfoStack.pop();
				if (theInfo.getInstantiatedTypeId() != theCalledTypeId)
					throw new RuntimeException(String.format(
							"Type mismatch in %s (found %d, expected %d)",
							itsMethodInfo.getName(),
							theCalledTypeId,
							theInfo.getInstantiatedTypeId()));
				
				mv.visitConstructorCallInsn(aOpcode, aOwner, theCalledTypeId, aName, aDesc);
				return;
			}
			
			mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
		}
	}
	

}

