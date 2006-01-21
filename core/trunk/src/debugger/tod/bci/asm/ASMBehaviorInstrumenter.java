/*
 * Created on Nov 11, 2005
 */
package tod.bci.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import tod.core.ILogCollector;
import tod.session.ASMDebuggerConfig;

/**
 * Provides all the methods that perform the insertion
 * of operations logging.
 * @author gpothier
 */
public class ASMBehaviorInstrumenter implements Opcodes
{
	private final ASMBehaviorCallInstrumenter itsBehaviorCallInstrumenter;
	private final MethodVisitor mv;
	private final ASMLocationPool itsLocationPool;
	private final ASMMethodInfo itsMethodInfo;
	private final int itsMethodId;
	private final ASMDebuggerConfig itsConfig;
	
	private int itsCurrentThreadVar;
	private int itsFirstFreeVar;
	
	public ASMBehaviorInstrumenter(
			ASMDebuggerConfig aConfig,
			MethodVisitor mv, 
			ASMLocationPool aLocationPool,
			ASMMethodInfo aMethodInfo,
			int aMethodId)
	{
		itsConfig = aConfig;
		this.mv = mv;
		itsLocationPool = aLocationPool;
		itsMethodInfo = aMethodInfo;
		itsMethodId = aMethodId;
		itsBehaviorCallInstrumenter = new ASMBehaviorCallInstrumenter(mv, this);
		
		itsCurrentThreadVar = itsMethodInfo.getMaxLocals();
		itsFirstFreeVar = itsCurrentThreadVar+2;
	}
	
	/**
	 * Indicates if the given class has (or will have) the identity instrumentation.
	 */
	private boolean hasIdentity(String aClassName)
	{
		return BCIUtils.acceptClass(aClassName, itsConfig.getGlobalSelector())
			&& BCIUtils.acceptClass(aClassName, itsConfig.getIdSelector());
	}
	
	/**
	 * Indicates if the given class has (or will have) tracing instrumentation.
	 */
	private boolean hasTrace(String aClassName)
	{
		return BCIUtils.acceptClass(aClassName, itsConfig.getGlobalSelector())
		&& BCIUtils.acceptClass(aClassName, itsConfig.getTraceSelector());
	}
	
	public void behaviorEnter()
	{
		// Store thread id
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getId", "()J");
		mv.visitVarInsn(LSTORE, itsCurrentThreadVar);
		
		// Create arguments array
		int theArrayVar = itsFirstFreeVar;
		int theFirstArgVar = itsMethodInfo.isStatic() ? 0 : 1;
		Type[] theArgumentTypes = Type.getArgumentTypes(itsMethodInfo.getDescriptor());
		
		createArgsArray(theArgumentTypes, theArrayVar, theFirstArgVar, false);
		
		invokeLogBehaviorEnter(
				itsMethodId,
				itsMethodInfo.isStatic() ? -1 : 0,
				theArrayVar);
	}
	
	public void behaviorExit()
	{
		Type theReturnType = Type.getReturnType(itsMethodInfo.getDescriptor());

		// Wrap and store return value
		if (theReturnType.getSort() == Type.VOID)
		{
			mv.visitInsn(ACONST_NULL);
		}
		else
		{
			mv.visitInsn(theReturnType.getSize() == 2 ? DUP2 : DUP);
			BCIUtils.wrap(mv, theReturnType);
		}
		
		mv.visitVarInsn(ASTORE, itsFirstFreeVar);
		
		invokeLogBehaviorExit(itsMethodId, itsFirstFreeVar);
	}
	
	public void behaviorExitWithException()
	{
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, itsFirstFreeVar);
		invokeLogBehaviorExitWithException(itsMethodId, itsFirstFreeVar);
	}
	
	public void constructorCall(
			int aOpcode,
			String aOwner, 
			int aTypeId,
			String aName,
			String aDesc)
	{
		invokeLogInstantiation();
		methodCall(aOpcode, aOwner, aName, aDesc);
	}
	
	public void constructorChainingCall(
			int aOpcode,
			String aOwner, 
			String aName,
			String aDesc)
	{
		invokeLogConstructorChaining();
		methodCall(aOpcode, aOwner, aName, aDesc);
	}
	
	public void methodCall(
			int aOpcode,
			String aOwner, 
			String aName,
			String aDesc)
	{
		int theCalledTypeId = itsLocationPool.getTypeId(aOwner);
		int theCalledMethodId = itsLocationPool.getBehaviorId(theCalledTypeId, aName, aDesc);
		
		itsBehaviorCallInstrumenter.setup(
				itsFirstFreeVar,
				theCalledMethodId,
				aDesc,
				aOpcode == INVOKESTATIC);
		
		if (hasTrace(aOwner))
		{
			// Handle before method call
			itsBehaviorCallInstrumenter.callLogBeforeMethodCallDry();
			
			// Do the original call
			mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
			
			// Handle after method call
			itsBehaviorCallInstrumenter.callLogAfterMethodCallDry();						
		}
		else
		{
			// Handle before method call
			itsBehaviorCallInstrumenter.storeArgsToLocals();
			itsBehaviorCallInstrumenter.createArgsArray();
			itsBehaviorCallInstrumenter.callLogBeforeMethodCall();
			itsBehaviorCallInstrumenter.pushArgs();

			Label theBefore = new Label();
			Label theAfter = new Label();
			Label theHandler = new Label();
			Label theFinish = new Label();
			
			mv.visitLabel(theBefore);
			
			// Do the original call
			mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
			
			mv.visitLabel(theAfter);
			
			mv.visitJumpInsn(GOTO, theFinish);
			
			mv.visitLabel(theHandler);
			
			itsBehaviorCallInstrumenter.callLogAfterMethodCallWithException();
			mv.visitInsn(ATHROW);

			mv.visitLabel(theFinish);
			
			mv.visitTryCatchBlock(theBefore, theAfter, theHandler, null);

			// Handle after method call
			itsBehaviorCallInstrumenter.callLogAfterMethodCall();
		}
	}
	
	public void fieldWrite(
			int aOpcode, 
			String aOwner, 
			String aName, 
			String aDesc)
	{
		boolean theStatic = aOpcode == PUTSTATIC;
		int theTypeId = itsLocationPool.getTypeId(aOwner);
		int theFieldId = itsLocationPool.getFieldId(theTypeId, aName, aDesc);
		Type theType = Type.getType(aDesc);
		
		Label l = new Label();
		mv.visitLabel(l);
		int theBytecodeIndex = l.getOffset();

		int theCurrentVar = itsFirstFreeVar;
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
		invokeLogFieldWrite(theBytecodeIndex, theFieldId, theTargetVar, theType, theValueVar);
		
		// Push parameters back to stack
		if (! theStatic) mv.visitVarInsn(ALOAD, theTargetVar);
		mv.visitVarInsn(theType.getOpcode(ILOAD), theValueVar);

		mv.visitFieldInsn(aOpcode, aOwner, aName, aDesc);
	}
	
	public void variableWrite(
			int aOpcode, 
			int aVar)
	{
		int theSort = BCIUtils.getSort(aOpcode);
		
		Label l = new Label();
		mv.visitLabel(l);
		int theBytecodeIndex = l.getOffset();

		// :: value
	
		// Perform store
		mv.visitVarInsn(aOpcode, aVar);
		
		// Call log method
		invokeLogLocalVariableWrite(
				theBytecodeIndex, 
				aVar, 
				BCIUtils.getType(theSort), 
				aVar);
	}
	
	public void variableInc(
			int aVar, 
			int aIncrement)
	{
		Label l = new Label();
		mv.visitLabel(l);
		int theBytecodeIndex = l.getOffset();

		// :: value
	
		// Perform store
		mv.visitIincInsn(aVar, aIncrement);
		
		// Call log method
		invokeLogLocalVariableWrite(
				theBytecodeIndex, 
				aVar, 
				BCIUtils.getType(Type.INT), 
				aVar);
	}

	/**
	 * Pushes standard method log args onto the stack:
	 * <li>Target collector</li>
	 * <li>Timestamp</li>
	 * <li>Thread id</li>
	 */
	public void pushStdLogArgs()
	{
		// ->target collector
		BCIUtils.pushCollector(mv);
		
		// ->timestamp
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
//		mv.visitInsn(LCONST_0);
		
		// ->thread id
		mv.visitVarInsn(LLOAD, itsCurrentThreadVar);		
	}

	/**
	 * Pushes standard method log args onto the stack:
	 * <li>Target collector</li>
	 * <li>Timestamp</li>
	 * <li>Thread id</li>
	 */
	public void pushDryLogArgs()
	{
		// ->target collector
		BCIUtils.pushCollector(mv);
		
		// ->thread id
		mv.visitVarInsn(LLOAD, itsCurrentThreadVar);		
	}
	
	public void invokeLogBeforeBehaviorCall(
			int aBytecodeIndex, 
			int aMethodId,
			int aTargetVar,
			int aArgsArrayVar)
	{
		mv.visitFieldInsn(GETSTATIC, "tod/agent/AgentReady", "READY", "Z");
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		
		pushStdLogArgs();
	
		// ->bytecode index
		BCIUtils.pushInt(mv, aBytecodeIndex);
		
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
	
		// ->target
		if (aTargetVar < 0) mv.visitInsn(ACONST_NULL);
		else mv.visitVarInsn(ALOAD, aTargetVar);
		
		// ->arguments
		mv.visitVarInsn(ALOAD, aArgsArrayVar);
	
		mv.visitMethodInsn(
				INVOKEINTERFACE, 
				Type.getInternalName(ILogCollector.class), 
				"logBeforeBehaviorCall", 
				"(JJIILjava/lang/Object;[Ljava/lang/Object;)V");
		
		mv.visitLabel(l);
	}

	public void invokeLogBeforeBehaviorCall(
			int aBytecodeIndex, 
			int aMethodId)
	{
		mv.visitFieldInsn(GETSTATIC, "tod/agent/AgentReady", "READY", "Z");
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		
		pushDryLogArgs();
		
		// ->bytecode index
		BCIUtils.pushInt(mv, aBytecodeIndex);
		
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
		
		mv.visitMethodInsn(
				INVOKEINTERFACE, 
				Type.getInternalName(ILogCollector.class), 
				"logBeforeBehaviorCall", 
		"(JII)V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogAfterBehaviorCall(
			int aBytecodeIndex, 
			int aMethodId,
			int aTargetVar,
			int aResultVar)
	{
		mv.visitFieldInsn(GETSTATIC, "tod/agent/AgentReady", "READY", "Z");
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		
		pushStdLogArgs();
		
		// ->bytecode index
		BCIUtils.pushInt(mv, aBytecodeIndex);
		
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
		
		// ->target
		if (aTargetVar < 0) mv.visitInsn(ACONST_NULL);
		else mv.visitVarInsn(ALOAD, aTargetVar);
		
		// ->result
		mv.visitVarInsn(ALOAD, aResultVar);
		
		mv.visitMethodInsn(
				INVOKEINTERFACE, 
				Type.getInternalName(ILogCollector.class), 
				"logAfterBehaviorCall", 
				"(JJIILjava/lang/Object;Ljava/lang/Object;)V");
		
		mv.visitLabel(l);
	}

	public void invokeLogAfterBehaviorCall()
	{
		mv.visitFieldInsn(GETSTATIC, "tod/agent/AgentReady", "READY", "Z");
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		
		pushDryLogArgs();
		
		mv.visitMethodInsn(
				INVOKEINTERFACE, 
				Type.getInternalName(ILogCollector.class), 
				"logAfterBehaviorCall", 
		"(J)V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogAfterBehaviorCallWithException(
			int aBytecodeIndex, 
			int aMethodId,
			int aTargetVar,
			int aExceptionVar)
	{
		mv.visitFieldInsn(GETSTATIC, "tod/agent/AgentReady", "READY", "Z");
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		
		pushStdLogArgs();
		
		// ->bytecode index
		BCIUtils.pushInt(mv, aBytecodeIndex);
		
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
		
		// ->target
		if (aTargetVar < 0) mv.visitInsn(ACONST_NULL);
		else mv.visitVarInsn(ALOAD, aTargetVar);
		
		// ->result
		mv.visitVarInsn(ALOAD, aExceptionVar);
		
		mv.visitMethodInsn(
				INVOKEINTERFACE, 
				Type.getInternalName(ILogCollector.class), 
				"logAfterBehaviorCallWithException", 
		"(JJIILjava/lang/Object;Ljava/lang/Object;)V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogAfterBehaviorCallWithException()
	{
		mv.visitFieldInsn(GETSTATIC, "tod/agent/AgentReady", "READY", "Z");
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		
		pushDryLogArgs();
		
		mv.visitMethodInsn(
				INVOKEINTERFACE, 
				Type.getInternalName(ILogCollector.class), 
				"logAfterBehaviorCallWithException", 
				"(J)V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogFieldWrite(
			int aBytecodeIndex, 
			int aFieldId,
			int aTargetVar,
			Type theType,
			int aValueVar)
	{
		mv.visitFieldInsn(GETSTATIC, "tod/agent/AgentReady", "READY", "Z");
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		
		pushStdLogArgs();
		
		// ->bytecode index
		BCIUtils.pushInt(mv, aBytecodeIndex);
		
		// ->field id
		BCIUtils.pushInt(mv, aFieldId);
		
		// ->target
		if (aTargetVar < 0) mv.visitInsn(ACONST_NULL);
		else mv.visitVarInsn(ALOAD, aTargetVar);
		
		// ->value
		mv.visitVarInsn(theType.getOpcode(ILOAD), aValueVar);
		BCIUtils.wrap(mv, theType);
		
		mv.visitMethodInsn(
				INVOKEINTERFACE, 
				Type.getInternalName(ILogCollector.class), 
				"logFieldWrite", 
				"(JJIILjava/lang/Object;Ljava/lang/Object;)V");
		
		mv.visitLabel(l);
	}

	public void invokeLogLocalVariableWrite(
			int aBytecodeIndex, 
			int aVariableId,
			Type theType,
			int aValueVar)
	{
		mv.visitFieldInsn(GETSTATIC, "tod/agent/AgentReady", "READY", "Z");
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		
		pushStdLogArgs();
		
		// ->bytecode index
		BCIUtils.pushInt(mv, aBytecodeIndex);
		
		// ->variable id
		BCIUtils.pushInt(mv, aVariableId);
		
		// ->value
		mv.visitVarInsn(theType.getOpcode(ILOAD), aValueVar);
		BCIUtils.wrap(mv, theType);
		
		mv.visitMethodInsn(
				INVOKEINTERFACE, 
				Type.getInternalName(ILogCollector.class), 
				"logLocalVariableWrite", 
				"(JJIILjava/lang/Object;)V");
		
		mv.visitLabel(l);
	}

	public void invokeLogBehaviorEnter (int aMethodId, int aTargetVar, int aArgsArrayVar)
	{
		mv.visitFieldInsn(GETSTATIC, "tod/agent/AgentReady", "READY", "Z");
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		
		pushStdLogArgs();
	
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
	
		// ->target
		if (aTargetVar < 0) mv.visitInsn(ACONST_NULL);
		else mv.visitVarInsn(ALOAD, aTargetVar);
		
		// ->arguments
		mv.visitVarInsn(ALOAD, aArgsArrayVar);
		
		mv.visitMethodInsn(
				Opcodes.INVOKEINTERFACE, 
				Type.getInternalName(ILogCollector.class), 
				"logBehaviorEnter", 
				"(JJILjava/lang/Object;[Ljava/lang/Object;)V");	
	
		mv.visitLabel(l);
	}

	public void invokeLogBehaviorExit (int aMethodId, int aResultVar)
	{
		mv.visitFieldInsn(GETSTATIC, "tod/agent/AgentReady", "READY", "Z");
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		
		pushStdLogArgs();
		
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
		
		// ->result
		mv.visitVarInsn(ALOAD, aResultVar);

		mv.visitMethodInsn(
				Opcodes.INVOKEINTERFACE, 
				Type.getInternalName(ILogCollector.class), 
				"logBehaviorExit", 
				"(JJILjava/lang/Object;)V");	
	
		mv.visitLabel(l);
	}

	public void invokeLogBehaviorExitWithException (int aMethodId, int aExceptionVar)
	{
		mv.visitFieldInsn(GETSTATIC, "tod/agent/AgentReady", "READY", "Z");
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		
		pushStdLogArgs();
		
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
		
		// ->exception
		mv.visitVarInsn(ALOAD, aExceptionVar);
		
		mv.visitMethodInsn(
				Opcodes.INVOKEINTERFACE, 
				Type.getInternalName(ILogCollector.class), 
				"logBehaviorExitWithException", 
				"(JJILjava/lang/Object;)V");	
		
		mv.visitLabel(l);
	}
	
	public void invokeLogInstantiation()
	{
		mv.visitFieldInsn(GETSTATIC, "tod/agent/AgentReady", "READY", "Z");
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		
		pushDryLogArgs();
	
		mv.visitMethodInsn(
				INVOKEINTERFACE, 
				Type.getInternalName(ILogCollector.class), 
				"logInstantiation", 
				"(J)V");
		
		mv.visitLabel(l);
	}

	public void invokeLogConstructorChaining()
	{
		mv.visitFieldInsn(GETSTATIC, "tod/agent/AgentReady", "READY", "Z");
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		
		pushDryLogArgs();
		
		mv.visitMethodInsn(
				INVOKEINTERFACE, 
				Type.getInternalName(ILogCollector.class), 
				"logConstructorChaining", 
		"(J)V");
		
		mv.visitLabel(l);
	}
	
	/**
	 * Generates the bytecode that creates the arguments array and stores
	 * it into a variable.
	 * Stack: . => .
	 */
	public void createArgsArray(Type[] aArgTypes, int aArrayVar, int aFirstArgVar, boolean aReverse)
	{
		if (aArgTypes.length > 0)
		{
			mv.visitIntInsn(BIPUSH, aArgTypes.length);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
			
			// :: array
			mv.visitVarInsn(ASTORE, aArrayVar);
			
			// ::
			
			int theCurrentVar = aFirstArgVar;
			short theIndex = 0;
			for (Type theType : aArgTypes)
			{
				if (aReverse) theCurrentVar -= theType.getSize();
				
				mv.visitVarInsn(ALOAD, aArrayVar); // :: array
				BCIUtils.pushInt(mv, theIndex++); // :: array, index
				mv.visitVarInsn(theType.getOpcode(ILOAD), theCurrentVar); // :: array, index, val
				BCIUtils.wrap(mv, theType);
				mv.visitInsn(AASTORE); // ::
				
				if (! aReverse) theCurrentVar += theType.getSize();
			}
		}
		else
		{
			mv.visitInsn(ACONST_NULL); // :: null
			mv.visitVarInsn(ASTORE, aArrayVar); // ::
		}
	}
	

}
