/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.bci.asm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import tod.Util;
import tod.agent.BehaviorCallType;
import tod.core.config.TODConfig;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IMutableBehaviorInfo;
import tod.core.database.structure.IMutableClassInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.IBehaviorInfo.BytecodeTagType;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.impl.bci.asm.attributes.AspectInfoAttribute;
import tod.impl.bci.asm.attributes.DummyLabelsAttribute;
import tod.impl.bci.asm.attributes.SootAttribute;
import tod.impl.database.structure.standard.TagMap;
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
	
	/**
	 * We keep a list of all the method visitors we created
	 * for the labels adjustment pass.
	 */
	private final List<BCIMethodVisitor> itsMethodVisitors = new ArrayList<BCIMethodVisitor>();

	
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
		itsTrace = itsConfig.isInScope(aName);
			
		itsClassInfo.setup(itsInterface, itsTrace, itsChecksum, theInterfaces, itsSuperclass);
		
		if (! itsInterface && itsTrace) markModified();
		
		super.visit(aVersion, access, aName, aSignature, aSuperName, aInterfaces);
	}
	
	@Override
	public void visitAttribute(Attribute aAttr)
	{
		if (aAttr instanceof AspectInfoAttribute)
		{
			AspectInfoAttribute theAttribute = (AspectInfoAttribute) aAttr;
			itsDatabase.setAdviceSourceMap(theAttribute.getAdviceMap());
		}
		else super.visitAttribute(aAttr);
	}

	@Override
	public void visitSource(String aSource, String aDebug)
	{
		itsClassInfo.setSourceFile(aSource);
		super.visitSource(aSource, aDebug);
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
			BCIMethodVisitor theMethodVisitor = new BCIMethodVisitor(mv, theMethodInfo);
			itsMethodVisitors.add(theMethodVisitor);
			
			InstructionCounterAdapter theCounter = new InstructionCounterAdapter(
					new InstantiationAnalyserVisitor (theMethodVisitor, theMethodInfo));	
			
			theMethodVisitor.setCounter(theCounter);
			
			return theCounter;
		}
		else return mv;
	}
	
	/**
	 * Sets up the behavior info for all visited methods.
	 */
	public void storeBehaviorInfos()
	{
		for (BCIMethodVisitor theVisitor : itsMethodVisitors)
		{
			theVisitor.storeBehaviorInfo();
		}
	}
	
	private class BCIMethodVisitor extends MethodAdapter
	{
		private InstructionCounterAdapter itsCounter;
		private ASMMethodInfo itsMethodInfo;
		private ASMBehaviorInstrumenter itsInstrumenter;
		
		private IMutableBehaviorInfo itsBehavior;
		private int itsStoreIndex = 0;
		
		private List<SootAttribute> itsSootAttributes = new ArrayList<SootAttribute>();
		private List<Handler> itsExceptionHandlers = new ArrayList<Handler>();
		
		private DummyLabelsAttribute itsLabelsAttribute = new DummyLabelsAttribute();
		
		public BCIMethodVisitor(MethodVisitor mv, ASMMethodInfo aMethodInfo)
		{
			super (mv);
			itsMethodInfo = aMethodInfo;
			itsBehavior = itsClassInfo.getNewBehavior(
					itsMethodInfo.getName(),
					itsMethodInfo.getDescriptor(), 
					itsMethodInfo.isStatic());
			
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
		
		public void setCounter(InstructionCounterAdapter aCounter)
		{
			itsCounter = aCounter;
		}
		
		@Override
		public void visitAttribute(Attribute aAttr)
		{
			if (aAttr instanceof SootAttribute)
			{
				SootAttribute theAttribute = (SootAttribute) aAttr;
				itsSootAttributes.add(theAttribute);
			}
			else
			{
				super.visitAttribute(aAttr);
			}
		}
		
		/**
		 * Check if we must insert entry hooks. 
		 */
		@Override
		public void visitCode()
		{
			mv.visitAttribute(itsLabelsAttribute);
			
			// Note: this method can be called if the currently visited type
			// is an interface (<clinit>).
			if (itsTrace && TRACE_ENVELOPPE) itsInstrumenter.insertEntryHooks();
			super.visitCode();
		}
		
		
		@Override
		public void visitTypeInsn(int aOpcode, String aDesc)
		{
			if (itsTrace && aOpcode == ANEWARRAY && TRACE_ARRAY)
			{
				ITypeInfo theType = itsDatabase.getNewType('L'+aDesc+';');
				itsInstrumenter.newArray(ASMBehaviorInstrumenter.createNewArrayClosure(aDesc), theType.getId());
			}
			else if (itsTrace && aOpcode == INSTANCEOF)
			{
				int theRank = itsCounter.getCount();
				BytecodeRole theRole = itsMethodInfo.getTag(BytecodeTagType.ROLE, theRank);
				if (theRole == BytecodeRole.ADVICE_TEST)
				{
					// This instanceof is part of a residue evaluation
					ITypeInfo theType = itsDatabase.getNewType('L'+aDesc+';');
					itsInstrumenter.instanceOf(aDesc, theType);
				}
				else mv.visitTypeInsn(aOpcode, aDesc);
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
		
		@Override
		public void visitLineNumber(int aLine, Label aStart)
		{
			itsMethodInfo.addLineNumber(new ASMLineNumberInfo(aStart, aLine));
			mv.visitLineNumber(aLine, aStart);
			itsLabelsAttribute.add(aStart);
		}
		
		@Override
		public void visitLocalVariable(String aName, String aDesc, String aSignature, Label aStart, Label aEnd, int aIndex)
		{
			itsMethodInfo.addLocalVariable(new ASMLocalVariableInfo(aStart, aEnd, aName, aDesc, aIndex));
			mv.visitLocalVariable(aName, aDesc, aSignature, aStart, aEnd, aIndex);
			itsLabelsAttribute.add(aStart);
			itsLabelsAttribute.add(aEnd);
		}

		@Override
		public void visitTryCatchBlock(Label aStart, Label aEnd, Label aHandler, String aType)
		{
			itsExceptionHandlers.add(new Handler(aStart, aEnd, aHandler, aType));
		}
		
		/**
		 * Post-processing of exception handlers to ensure they are in the correct order.
		 */
		private void processExceptionHandlers()
		{
			Collections.sort(itsExceptionHandlers);
			for (Handler theHandler : itsExceptionHandlers)
			{
				mv.visitTryCatchBlock(theHandler.start, theHandler.end, theHandler.handler, theHandler.type);
			}
		}
		
		@Override
		public void visitMaxs(int aMaxStack, int aMaxLocals)
		{
			processExceptionHandlers();
			
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
		

		/**
		 * Sets up the {@link IBehaviorInfo}.
		 */
		public void storeBehaviorInfo()
		{
			// Prepare tags
			TagMap theTagMap = new TagMap();
			if ("fetchNext".equals(itsBehavior.getName()))
			{
				System.out.println("BCIMethodVisitor.storeBehaviorInfo()");
			}
			
			if (itsConfig.getTODConfig().get(TODConfig.WITH_ASPECTS))
			{
				for (SootAttribute theAttribute : itsSootAttributes)
				{
					theAttribute.fillTagMap(theTagMap, itsMethodInfo.getCodeSize());
				}
			}
			
			if (itsConfig.getTODConfig().get(TODConfig.WITH_BYTECODE))
			{
				itsInstrumenter.fillTagMap(theTagMap);
			}
			
			itsInstrumenter.updateProbes(theTagMap);
			
			// Setup behavior info
			itsBehavior.setup(
					itsTrace,
					itsMethodInfo.getKind(),
					itsMethodInfo.getCodeSize(),
					itsMethodInfo.createLineNumberTable(), 
					theTagMap);
			
			for (LocalVariableInfo theInfo : itsMethodInfo.createLocalVariableTable())
			{
				itsBehavior.addLocalVariableInfo(theInfo);
			}
		}
	}
	
	/**
	 * Represents an exception handler.
	 * @author gpothier
	 */
	private static class Handler implements Comparable<Handler>
	{
		public final Label start;
		public final Label end;
		public final Label handler;
		public final String type;
		
		public Handler(Label aStart, Label aEnd, Label aHandler, String aType)
		{
			start = aStart;
			end = aEnd;
			handler = aHandler;
			type = aType;
		}
		
		/**
		 * Whether this handler contains the specified handlers.
		 */
		public boolean contains(Handler h)
		{
			int mi1 = start.getOffset();
			int mi2 = end.getOffset();
			int hi1 = h.start.getOffset();
			int hi2 = h.end.getOffset();
			return mi1 <= hi1 && hi2 <= mi2;
		}

		public int compareTo(Handler h)
		{
			int ms = end.getOffset()-start.getOffset();
			int hs = h.end.getOffset()-h.start.getOffset();
			return ms-hs;
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
							"[LogBCIVisitor] Type mismatch in %s.%s (found %d, expected %d)",
							itsClassInfo.getName(),
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

