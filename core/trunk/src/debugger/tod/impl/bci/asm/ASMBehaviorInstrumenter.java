/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import tod.agent.AgentReady;
import tod.core.BehaviorCallType;
import tod.core.EventInterpreter;

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
	
	/**
	 * Index of the variable that stores the real return point of the method.
	 */
	private int itsReturnLocationVar;
	private int itsFirstFreeVar;
	
	private Label itsReturnHookLabel;
	private Label itsFinallyHookLabel;
	private Label itsCodeStartLabel;


	
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
		
		itsFirstFreeVar = itsMethodInfo.getMaxLocals();
		itsReturnLocationVar = itsFirstFreeVar;
		itsFirstFreeVar += 1;
	}
	
	/**
	 * Indicates if the given class has (or will have) tracing instrumentation.
	 */
	private boolean hasTrace(String aClassName)
	{
		return BCIUtils.acceptClass(aClassName, itsConfig.getGlobalSelector())
		&& BCIUtils.acceptClass(aClassName, itsConfig.getTraceSelector());
	}
	
	/**
	 * <li>Identifiable objects' id initialization</li>
	 * <li>Log behavior enter</li>
	 * <li>Insert return hooks at the beginning of the method body:
	 * 		<li>Log behavior exit</li>
	 * </li>
	 */
	public void insertEntryHooks()
	{
		itsReturnHookLabel = new Label();
		itsFinallyHookLabel = new Label();
		itsCodeStartLabel = new Label();


		// Call logBehaviorEnter
		// We suppose that if a class is instrumented all its descendants
		// are also instrumented, so we can't miss a super call
		if (LogBCIVisitor.TRACE_ENTRY)
		{
			behaviorEnter("<init>".equals(itsMethodInfo.getName()) ?
					BehaviorCallType.INSTANTIATION
					: BehaviorCallType.METHOD_CALL);
		}
		
		mv.visitJumpInsn(GOTO, itsCodeStartLabel);
		
		// -- Return hook
		mv.visitLabel(itsReturnHookLabel);

		// Call logBehaviorExit
		if (LogBCIVisitor.TRACE_EXIT)
		{
			behaviorExit();
		}

		// Insert RETURN
		Type theReturnType = Type.getReturnType(itsMethodInfo.getDescriptor());
		mv.visitInsn(theReturnType.getOpcode(IRETURN));
		
		// -- Finally hook
		mv.visitLabel(itsFinallyHookLabel);
		
		// Call logBehaviorExitWithException
		if (LogBCIVisitor.TRACE_EXIT)
		{
			behaviorExitWithException();
		}
		
		mv.visitInsn(ATHROW);

		mv.visitLabel(itsCodeStartLabel);
	}
	
	public void endHooks()
	{
		Label theCodeEndLabel = new Label();
		mv.visitLabel(theCodeEndLabel);
		mv.visitTryCatchBlock(itsCodeStartLabel, theCodeEndLabel, itsFinallyHookLabel, null);
	}

	public void doReturn(int aOpcode)
	{
		// Store location of this return into the variable
		Label l = new Label();
		mv.visitLabel(l);
		int theBytecodeIndex = l.getOffset();
		
		BCIUtils.pushInt(mv, theBytecodeIndex);
		mv.visitVarInsn(ISTORE, itsReturnLocationVar);
		
		mv.visitJumpInsn(GOTO, itsReturnHookLabel);
	}
	
	public void behaviorEnter(BehaviorCallType aCallType)
	{
		// Create arguments array
		int theArrayVar = itsFirstFreeVar;
		int theFirstArgVar = itsMethodInfo.isStatic() ? 0 : 1;
		Type[] theArgumentTypes = Type.getArgumentTypes(itsMethodInfo.getDescriptor());
		
		createArgsArray(theArgumentTypes, theArrayVar, theFirstArgVar, false);

		invokeLogBehaviorEnter(
				itsMethodId,
				aCallType,
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
	
	public void methodCall(
			int aOpcode,
			String aOwner, 
			String aName,
			String aDesc,
			BehaviorCallType aCallType)
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
			itsBehaviorCallInstrumenter.callLogBeforeBehaviorCallDry(aCallType);
			
			// Do the original call
			mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
			
			// Handle after method call
			itsBehaviorCallInstrumenter.callLogAfterBehaviorCallDry();						
		}
		else
		{
			// Handle before method call
			itsBehaviorCallInstrumenter.storeArgsToLocals();
			itsBehaviorCallInstrumenter.createArgsArray();
			itsBehaviorCallInstrumenter.callLogBeforeMethodCall(aCallType);
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

	public void arrayWrite(int aOpcode)
	{
		int theSort = BCIUtils.getSort(aOpcode);
		Type theType = BCIUtils.getType(theSort);
		
		Label l = new Label();
		mv.visitLabel(l);
		int theBytecodeIndex = l.getOffset();

		// :: array ref, index, value
		
		int theCurrentVar = itsFirstFreeVar;
		int theValueVar;
		int theIndexVar;
		int theTargetVar;
		
		theTargetVar = theCurrentVar++;
		theIndexVar = theCurrentVar++;
		theValueVar = theCurrentVar++;
		
		// Store parameters
		
		mv.visitVarInsn(theType.getOpcode(ISTORE), theValueVar);
		mv.visitVarInsn(ISTORE, theIndexVar);
		mv.visitVarInsn(ASTORE, theTargetVar);
		
		// Reload parameters
		
		mv.visitVarInsn(ALOAD, theTargetVar);
		mv.visitVarInsn(ILOAD, theIndexVar);
		mv.visitVarInsn(theType.getOpcode(ILOAD), theValueVar);
	
		// Perform store
		mv.visitInsn(aOpcode);
		
		// Call log method (if no exception occurred)
		invokeLogArrayWrite(
				theBytecodeIndex, 
				theTargetVar, 
				theIndexVar, 
				theType, 
				theValueVar);
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
	}
	
	public void invokeLogBeforeBehaviorCall(
			int aBytecodeIndex, 
			int aMethodId,
			BehaviorCallType aCallType,
			int aTargetVar,
			int aArgsArrayVar)
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
		pushStdLogArgs();
	
		// ->bytecode index
		BCIUtils.pushInt(mv, aBytecodeIndex);
		
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
		
		// ->call type
		mv.visitFieldInsn(
				GETSTATIC, 
				Type.getInternalName(BehaviorCallType.class),
				aCallType.name(), 
				Type.getDescriptor(BehaviorCallType.class));
	
		// ->target
		if (aTargetVar < 0) mv.visitInsn(ACONST_NULL);
		else mv.visitVarInsn(ALOAD, aTargetVar);
		
		// ->arguments
		mv.visitVarInsn(ALOAD, aArgsArrayVar);
	
		mv.visitMethodInsn(
				INVOKEVIRTUAL, 
				Type.getInternalName(EventInterpreter.class), 
				"logBeforeBehaviorCall", 
				"(II"+Type.getDescriptor(BehaviorCallType.class)+"Ljava/lang/Object;[Ljava/lang/Object;)V");
		
		mv.visitLabel(l);
	}

	public void invokeLogBeforeBehaviorCall(
			int aBytecodeIndex, 
			int aMethodId,
			BehaviorCallType aCallType)
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
		pushDryLogArgs();
		
		// ->bytecode index
		BCIUtils.pushInt(mv, aBytecodeIndex);
		
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);

		// ->call type
		mv.visitFieldInsn(
				GETSTATIC, 
				Type.getInternalName(BehaviorCallType.class),
				aCallType.name(), 
				Type.getDescriptor(BehaviorCallType.class));
		
		mv.visitMethodInsn(
				INVOKEVIRTUAL, 
				Type.getInternalName(EventInterpreter.class), 
				"logBeforeBehaviorCall", 
				"(II"+Type.getDescriptor(BehaviorCallType.class)+")V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogAfterBehaviorCall(
			int aBytecodeIndex, 
			int aMethodId,
			int aTargetVar,
			int aResultVar)
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
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
				INVOKEVIRTUAL, 
				Type.getInternalName(EventInterpreter.class), 
				"logAfterBehaviorCall", 
				"(IILjava/lang/Object;Ljava/lang/Object;)V");
		
		mv.visitLabel(l);
	}

	public void invokeLogAfterBehaviorCall()
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
		pushDryLogArgs();
		
		mv.visitMethodInsn(
				INVOKEVIRTUAL, 
				Type.getInternalName(EventInterpreter.class), 
				"logAfterBehaviorCall", 
				"()V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogAfterBehaviorCallWithException(
			int aBytecodeIndex, 
			int aMethodId,
			int aTargetVar,
			int aExceptionVar)
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
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
				INVOKEVIRTUAL, 
				Type.getInternalName(EventInterpreter.class), 
				"logAfterBehaviorCallWithException", 
				"(IILjava/lang/Object;Ljava/lang/Object;)V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogFieldWrite(
			int aBytecodeIndex, 
			int aFieldId,
			int aTargetVar,
			Type theType,
			int aValueVar)
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
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
				INVOKEVIRTUAL, 
				Type.getInternalName(EventInterpreter.class), 
				"logFieldWrite", 
				"(IILjava/lang/Object;Ljava/lang/Object;)V");
		
		mv.visitLabel(l);
	}

	public void invokeLogArrayWrite(
			int aBytecodeIndex, 
			int aTargetVar,
			int aIndexVar,
			Type theType,
			int aValueVar)
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
		pushStdLogArgs();
		
		// ->bytecode index
		BCIUtils.pushInt(mv, aBytecodeIndex);
		
		// ->target
		mv.visitVarInsn(ALOAD, aTargetVar);
		
		// ->index
		mv.visitVarInsn(ILOAD, aIndexVar);
		
		// ->value
		mv.visitVarInsn(theType.getOpcode(ILOAD), aValueVar);
		BCIUtils.wrap(mv, theType);
		
		mv.visitMethodInsn(
				INVOKEVIRTUAL, 
				Type.getInternalName(EventInterpreter.class), 
				"logArrayWrite", 
		"(ILjava/lang/Object;ILjava/lang/Object;)V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogLocalVariableWrite(
			int aBytecodeIndex, 
			int aVariableId,
			Type theType,
			int aValueVar)
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
		pushStdLogArgs();
		
		// ->bytecode index
		BCIUtils.pushInt(mv, aBytecodeIndex);
		
		// ->variable id
		BCIUtils.pushInt(mv, aVariableId);
		
		// ->value
		mv.visitVarInsn(theType.getOpcode(ILOAD), aValueVar);
		BCIUtils.wrap(mv, theType);
		
		mv.visitMethodInsn(
				INVOKEVIRTUAL, 
				Type.getInternalName(EventInterpreter.class), 
				"logLocalVariableWrite", 
				"(IILjava/lang/Object;)V");
		
		mv.visitLabel(l);
	}

	public void invokeLogBehaviorEnter (
			int aMethodId, 
			BehaviorCallType aCallType,
			int aTargetVar, 
			int aArgsArrayVar)
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
		pushStdLogArgs();
	
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
		
		// ->call type
		mv.visitFieldInsn(
				GETSTATIC, 
				Type.getInternalName(BehaviorCallType.class),
				aCallType.name(), 
				Type.getDescriptor(BehaviorCallType.class));
	
		// ->target
		if (LogBCIVisitor.ENABLE_VERIFY || aTargetVar < 0) mv.visitInsn(ACONST_NULL);
		else mv.visitVarInsn(ALOAD, aTargetVar);
		
		// ->arguments
		mv.visitVarInsn(ALOAD, aArgsArrayVar);
		
		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL, 
				Type.getInternalName(EventInterpreter.class), 
				"logBehaviorEnter", 
				"(I"+Type.getDescriptor(BehaviorCallType.class)+"Ljava/lang/Object;[Ljava/lang/Object;)V");	
	
		mv.visitLabel(l);
	}

	public void invokeLogBehaviorExit (int aMethodId, int aResultVar)
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
		pushStdLogArgs();
		
		// ->bytecode index
		mv.visitVarInsn(ILOAD, itsReturnLocationVar);
		
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
		
		// ->result
		mv.visitVarInsn(ALOAD, aResultVar);

		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL, 
				Type.getInternalName(EventInterpreter.class), 
				"logBehaviorExit", 
				"(IILjava/lang/Object;)V");	
	
		mv.visitLabel(l);
	}

	private void invokeLogBehaviorExitWithException (int aMethodId, int aExceptionVar)
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
		pushStdLogArgs();
		
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
		
		// ->exception
		mv.visitVarInsn(ALOAD, aExceptionVar);
		
		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL, 
				Type.getInternalName(EventInterpreter.class), 
				"logBehaviorExitWithException", 
				"(ILjava/lang/Object;)V");	
		
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
