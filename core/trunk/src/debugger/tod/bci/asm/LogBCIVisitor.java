/*
 * Created on Oct 24, 2005
 */
package tod.bci.asm;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import tod.core.IIdentifiableObject;
import tod.core.IdGenerator;
import tod.session.ASMDebuggerConfig;
import zz.utils.ArrayStack;
import zz.utils.Stack;

/**
 * The ASM visitor that instruments the classes.
 * @author gpothier
 */
public class LogBCIVisitor extends ClassAdapter implements Opcodes
{
	private static final boolean LOG = true;
	private static final String ID_FIELD_NAME = "__log_uid";
	private static final String ID_METHOD_NAME = "__log_uid";

	
	private boolean itsModified = false;
	
	private boolean itsIdentifiable = false;
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
		
		// Check if the class should implement IIdentifiableObject
		if (! BCIUtils.isInterface(access) 
				&& "java/lang/Object".equals(aSuperName) 
				&& BCIUtils.acceptClass(aName, itsConfig.getIdSelector()))
		{
			itsIdentifiable = true;
			markModified();
			
			aInterfaces = BCIUtils.addToArray(aInterfaces, Type.getInternalName(IIdentifiableObject.class));
		}
		
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
	public void visitEnd()
	{
		// If the class implements IIdentifiableObject, add field and getter
		if (itsIdentifiable)
		{
			cv.visitField(ACC_PRIVATE | ACC_TRANSIENT, ID_FIELD_NAME, "J", null, null);
			
			MethodVisitor mv;
			{
				mv = cv.visitMethod(ACC_PUBLIC, ID_METHOD_NAME, "()J", null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, itsTypeName, ID_FIELD_NAME, "J");
				mv.visitInsn(LRETURN);
				mv.visitMaxs(2, 1);
				mv.visitEnd();
			}
		}
		
		super.visitEnd();
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
		
		private Label itsReturnHookLabel = new Label();

		private int itsMethodId;

		public BCIMethodVisitor(MethodVisitor mv, ASMMethodInfo aMethodInfo)
		{
			super (mv);
			itsMethodInfo = aMethodInfo;
			itsMethodId = getLocationPool().getMethodId(
								itsTypeId, 
								itsMethodInfo.getName(), 
								itsMethodInfo.getDescriptor());
			
			if (LOG) System.out.println("Processing method "+itsMethodInfo.getName()+itsMethodInfo.getDescriptor());
		}

		@Override
		public void visitTypeInsn(int aOpcode, String aDesc)
		{
			mv.visitTypeInsn(aOpcode, aDesc);
			if (itsTrace && aOpcode == NEW) mv.visitInsn(DUP);
		}
		
		public void visitSuperOrThisCallInsn(int aOpcode, String aOwner, String aName, String aDesc)
		{
			mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
		}
		
		public void visitConstructorCallInsn(int aOpcode, String aOwner, int aCalledTypeId, String aName, String aDesc)
		{
			int theBytecodeIndex = -1;
			
			if (itsTrace) 
			{
				Label l = new Label();
				mv.visitLabel(l);
				theBytecodeIndex = l.getOffset();
			}
			
			// Call constructor
			mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
			
			if (itsTrace) 
			{
				int theInstanceVar = itsMethodInfo.getMaxLocals();
				mv.visitVarInsn(ASTORE, theInstanceVar);
				
				BCIUtils.invokeLogInstantiation(mv, theBytecodeIndex, aCalledTypeId, theInstanceVar);
			}
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
			
			int theCalledTypeId = -1;
			int theCalledMethodId = -1;
			MethodCallInstrumenter theInstrumenter = null;
			
			// Init variables
			if (itsTrace)
			{
				theCalledTypeId = getLocationPool().getTypeId(aOwner);
				theCalledMethodId = getLocationPool().getMethodId(theCalledTypeId, aName, aDesc);
			}
			
			// Handle before method call
			if (itsTrace)
			{
				theInstrumenter = new MethodCallInstrumenter(
						mv,
						itsMethodInfo.getMaxLocals(),
						theCalledMethodId,
						aDesc,
						aOpcode == INVOKESTATIC);
				
				theInstrumenter.storeArgsToLocals();
				theInstrumenter.createArgsArray();
				theInstrumenter.callLogBeforeMethodCall();
				theInstrumenter.pushArgs();
			}
			
			// Do the original call
			mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
			
			// Handle after method call
			if (itsTrace)
			{
				theInstrumenter.callLogAfterMethodCall();
			}
		}
		
		@Override
		public void visitFieldInsn(int aOpcode, String aOwner, String aName, String aDesc)
		{
			if (itsTrace && (aOpcode == PUTFIELD || aOpcode == PUTSTATIC))
			{
				boolean theStatic = aOpcode == PUTSTATIC;
				int theTypeId = getLocationPool().getTypeId(aOwner);
				int theFieldId = getLocationPool().getFieldId(theTypeId, aName, aDesc);
				Type theType = Type.getType(aDesc);
				
				Label l = new Label();
				mv.visitLabel(l);
				int theBytecodeIndex = l.getOffset();

				int theCurrentVar = itsMethodInfo.getMaxLocals();
				int theValueVar;
				int theTargetVar;
				
				if (theStatic)
				{
					theTargetVar = -1;
					theValueVar = theCurrentVar++;
				}
				else
				{
					theTargetVar = theCurrentVar++;
					theValueVar = theCurrentVar++;
				}
				
				// :: [target], value
			
				// Store parameters
				
				mv.visitVarInsn(theType.getOpcode(ISTORE), theValueVar);
				if (! theStatic) mv.visitVarInsn(ASTORE, theTargetVar);
				
				// Call log method
				BCIUtils.invokeLogFieldWrite(mv, theBytecodeIndex, theFieldId, theTargetVar, theType, theValueVar);
				
				// Push parameters back to stack
				if (! theStatic) mv.visitVarInsn(ALOAD, theTargetVar);
				mv.visitVarInsn(theType.getOpcode(ILOAD), theValueVar);
			}
			mv.visitFieldInsn(aOpcode, aOwner, aName, aDesc);
		}
		
		@Override
		public void visitVarInsn(int aOpcode, int aVar)
		{
			if (itsTrace && aOpcode >= ISTORE && aOpcode < IASTORE)
			{
				int theSort = BCIUtils.getSort(aOpcode);
				boolean theStatic = itsMethodInfo.isStatic();
				
				Label l = new Label();
				mv.visitLabel(l);
				int theBytecodeIndex = l.getOffset();

				// :: value
			
				// Perform store
				mv.visitVarInsn(aOpcode, aVar);
				
				// Call log method
				BCIUtils.invokeLogLocalVariableWrite(
						mv, 
						theBytecodeIndex, 
						aVar, 
						theStatic ? -1 : 0, 
						BCIUtils.getType(theSort), 
						aVar);
				
			}
			else mv.visitVarInsn(aOpcode, aVar);
		}
		
		/**
		 * <li>Replace RETURN bytecodes by GOTOs to the return hooks defined in
		 * {@link #visitCode()}</li>
		 */
		@Override
		public void visitInsn(int aOpcode)
		{
			if (aOpcode >= IRETURN && aOpcode <= RETURN) mv.visitJumpInsn(GOTO, itsReturnHookLabel);
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
			// If the class implements IIdentifiableObject, add initialization code 
			if (itsIdentifiable && "<init>".equals(itsMethodInfo.getName()))
			{
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, itsTypeName, ID_FIELD_NAME, "J");
				mv.visitInsn(LCONST_0);
				mv.visitInsn(LCMP);
				Label l = new Label();
				mv.visitJumpInsn(IFNE, l);
				
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(IdGenerator.class), "createLongId", "()J");
				mv.visitFieldInsn(PUTFIELD, itsTypeName, ID_FIELD_NAME, "J");
				
				mv.visitLabel(l);
			}
			
			// Call logBehaviorEnter
			if (itsTrace) BCIUtils.invokeLogBehaviorEnter(mv, itsMethodId);
			
			// Goto to real code.
			Label theCodeLabel = new Label();
			mv.visitJumpInsn(GOTO, theCodeLabel);
			
			// Insert return hooks
			mv.visitLabel(itsReturnHookLabel);
			

			// Call logBehaviorEnter
			if (itsTrace) BCIUtils.invokeLogBehaviorExit(mv, itsMethodId);

			// Insert RETURN
			Type theReturnType = Type.getReturnType(itsMethodInfo.getDescriptor());
			mv.visitInsn(theReturnType.getOpcode(IRETURN));

			mv.visitLabel(theCodeLabel);
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

