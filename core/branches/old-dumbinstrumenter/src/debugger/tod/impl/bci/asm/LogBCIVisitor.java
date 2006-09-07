/*
 * Created on Oct 24, 2005
 */
package tod.impl.bci.asm;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import zz.utils.ArrayStack;
import zz.utils.Stack;

/**
 * The ASM visitor that instruments the classes.
 * @author gpothier
 */
public class LogBCIVisitor extends ClassAdapter implements Opcodes
{
	private static final boolean LOG = true;

	private boolean itsModified = false;
	
	private boolean itsTrace = false;
	
	private String itsTypeName;
	private int itsSupertypeId;
	private int itsTypeId;

	private final InfoCollector itsInfoCollector;
	private int itsCurrentMethodIndex = 0;

	private final ASMDebuggerConfig itsConfig;

	
	public LogBCIVisitor(ASMDebuggerConfig aConfig, InfoCollector aInfoCollector, ClassVisitor aVisitor)
	{
		super(aVisitor);
		itsInfoCollector = aInfoCollector;
		itsConfig = aConfig;
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
	
	private ASMLocationPool getLocationPool()
	{
		return itsConfig.getLocationPool();
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
		// Register type
		itsTypeName = aName;
		itsTypeId = getLocationPool().getTypeId(aName);
		
		itsSupertypeId = BCIUtils.isInterface(access) || aSuperName == null ? 
				-1
				: getLocationPool().getTypeId(aSuperName);
		
		int[] theInterfaceIds = new int[aInterfaces != null ? aInterfaces.length : 0];
		if (aInterfaces != null) for (int i = 0; i < aInterfaces.length; i++)
		{
			String theInterface = aInterfaces[i];
			theInterfaceIds[i] = getLocationPool().getTypeId(theInterface);
		}
		
		getLocationPool().registerType(itsTypeId, itsTypeName, itsSupertypeId, theInterfaceIds);
		
		// Check if we should trace operations in the class
		if (! BCIUtils.isInterface(access)
				&& BCIUtils.acceptClass(aName, itsConfig.getTraceSelector()))
		{
			itsTrace = true;
			markModified();
		}
		
		super.visit(aVersion, access, aName, aSignature, aSuperName, aInterfaces);
	}
	
	@Override
	public MethodVisitor visitMethod(
			int access, 
			String aName, 
			String aDesc, 
			String aSignature, 
			String[] aExceptions)
	{
		ASMMethodInfo theMethodInfo = itsInfoCollector.getMethodInfo(itsCurrentMethodIndex++);
		MethodVisitor mv = super.visitMethod(access, aName, aDesc, aSignature, aExceptions);
		if (mv == null) return null;
		else return new InstantiationAnalyserVisitor (new BCIMethodVisitor(mv, theMethodInfo), theMethodInfo);
//		else return new BCIMethodVisitor(mv, theMethodInfo);
	}
	
	private class BCIMethodVisitor extends MethodAdapter
	{
		private ASMMethodInfo itsMethodInfo;
		private ASMBehaviorInstrumenter itsInstrumenter;
		
		private Label itsReturnHookLabel;
		private Label itsFinallyHookLabel;
		private Label itsCodeStartLabel;

		private int itsMethodId;

		public BCIMethodVisitor(MethodVisitor mv, ASMMethodInfo aMethodInfo)
		{
			super (mv);
			itsMethodInfo = aMethodInfo;
			itsMethodId = getLocationPool().getBehaviorId(
								itsTypeId, 
								itsMethodInfo.getName(), 
								itsMethodInfo.getDescriptor());
			
			itsInstrumenter = new ASMBehaviorInstrumenter(
					itsConfig,
					mv, 
					getLocationPool(), 
					itsMethodInfo,
					itsMethodId);
			
			if (LOG) System.out.println("Processing method "+itsMethodInfo.getName()+itsMethodInfo.getDescriptor());
		}

		@Override
		public void visitTypeInsn(int aOpcode, String aDesc)
		{
			mv.visitTypeInsn(aOpcode, aDesc);
		}
		
		public void visitSuperOrThisCallInsn(int aOpcode, String aOwner, String aName, String aDesc)
		{
			if (itsTrace) itsInstrumenter.constructorChainingCall(aOpcode, aOwner, aName, aDesc);
			else mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
		}
		
		public void visitConstructorCallInsn(int aOpcode, String aOwner, int aCalledTypeId, String aName, String aDesc)
		{
			if (itsTrace) itsInstrumenter.constructorCall(aOpcode, aOwner, aCalledTypeId, aName, aDesc);
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
			
			if (itsTrace) itsInstrumenter.methodCall(aOpcode, aOwner, aName, aDesc);
			else mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
		}
		
		@Override
		public void visitFieldInsn(int aOpcode, String aOwner, String aName, String aDesc)
		{
			if (itsTrace && (aOpcode == PUTFIELD || aOpcode == PUTSTATIC))
			{
				itsInstrumenter.fieldWrite(aOpcode, aOwner, aName, aDesc);
			}
			else mv.visitFieldInsn(aOpcode, aOwner, aName, aDesc);
		}
		
		@Override
		public void visitVarInsn(int aOpcode, int aVar)
		{
			if (itsTrace && aOpcode >= ISTORE && aOpcode < IASTORE)
			{
				itsInstrumenter.variableWrite(aOpcode, aVar);
			}
			else mv.visitVarInsn(aOpcode, aVar);
		}
		
		@Override
		public void visitIincInsn(int aVar, int aIncrement)
		{
			if (itsTrace)
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
			if (itsTrace && aOpcode >= IRETURN && aOpcode <= RETURN) 
				mv.visitJumpInsn(GOTO, itsReturnHookLabel);
			else super.visitInsn(aOpcode);
		}
		
		/**
		 * <li>Identifiable objects' id initialization</li>
		 * <li>Log behavior enter</li>
		 * <li>Insert return hooks at the beginning of the method body:
		 * 		<li>Log behavior exit</li>
		 * </li>
		 */
		private void insertEntryHooks()
		{
			if (itsTrace)
			{
				itsReturnHookLabel = new Label();
				itsFinallyHookLabel = new Label();
				itsCodeStartLabel = new Label();
				
				// Call logBehaviorEnter
				itsInstrumenter.behaviorEnter();
				
				mv.visitJumpInsn(GOTO, itsCodeStartLabel);
				
				// -- Return hook
				mv.visitLabel(itsReturnHookLabel);
	
				// Call logBehaviorExit
				itsInstrumenter.behaviorExit();
	
				// Insert RETURN
				Type theReturnType = Type.getReturnType(itsMethodInfo.getDescriptor());
				mv.visitInsn(theReturnType.getOpcode(IRETURN));
				
				// -- Finally hook
				mv.visitLabel(itsFinallyHookLabel);
				
				// Call logBehaviorExitWithException
				itsInstrumenter.behaviorExitWithException();
				
				mv.visitInsn(ATHROW);

				mv.visitLabel(itsCodeStartLabel);
			}
		}
		
		/**
		 * Check if we must insert entry hooks. 
		 */
		@Override
		public void visitCode()
		{
			insertEntryHooks();
			super.visitCode();
		}
		
		@Override
		public void visitMaxs(int aMaxStack, int aMaxLocals)
		{
			if (itsTrace)
			{
				Label theCodeEndLabel = new Label();
				mv.visitLabel(theCodeEndLabel);
				mv.visitTryCatchBlock(itsCodeStartLabel, theCodeEndLabel, itsFinallyHookLabel, null);
			}
			
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
			getLocationPool().registerBehaviorAttributes(itsMethodId, itsMethodInfo);
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
			itsInstantiatedTypeId = getLocationPool().getTypeId(aTypeDesc);
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
			if (aOpcode == INVOKESPECIAL 
					&& "<init>".equals(aName))
			{
				// We are invoking a constructor.
				
				int theCalledTypeId = getLocationPool().getTypeId(aOwner);
				
				if ("<init>".equals(itsMethodInfo.getName()))
				{
					// We are in a constructor
					if (theCalledTypeId == itsSupertypeId || theCalledTypeId == itsTypeId)
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
					throw new RuntimeException("Type mismatch");
				
				mv.visitConstructorCallInsn(aOpcode, aOwner, theCalledTypeId, aName, aDesc);
				return;
			}
			
			mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
		}
	}
}
