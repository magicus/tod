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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import tod.Util;
import tod.agent.AgentConfig;
import tod.agent.AgentReady;
import tod.agent.BehaviorCallType;
import tod.agent.EventCollector;
import tod.agent.ExceptionGeneratedReceiver;
import tod.agent.TracedMethods;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMutableBehaviorInfo;
import tod.core.database.structure.IMutableClassInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.IBehaviorInfo.BytecodeTagType;
import tod.core.database.structure.IBehaviorInfo.HasTrace;
import tod.impl.bci.asm.ASMInstrumenter.CodeRange;
import tod.impl.bci.asm.ASMInstrumenter.RangeManager;
import tod.impl.bci.asm.ProbesManager.TmpProbeInfo;
import tod.impl.database.structure.standard.TagMap;

/**
 * Provides all the methods that perform the insertion
 * of operations logging.
 * @author gpothier
 */
public class ASMBehaviorInstrumenter implements Opcodes
{
	private final IMutableStructureDatabase itsStructureDatabase;
	private final ProbesManager itsProbesManager;
	private final IMutableBehaviorInfo itsBehavior;
	private final ASMBehaviorCallInstrumenter itsBehaviorCallInstrumenter;
	private final MethodVisitor mv;
	private final ASMMethodInfo itsMethodInfo;
	private final ASMDebuggerConfig itsConfig;
	
	/**
	 * Index of the variable that stores the real return point of the method.
	 * The stored value is a probe id (int)
	 */
	private int itsReturnLocationVar;
	
	/**
	 * Index of the variable that stores the event interpreter.
	 */
	private int itsCollectorVar;
	private int itsFirstFreeVar;
	
	private Label itsReturnHookLabel;
	private Label itsFinallyHookLabel;
	private Label itsCodeStartLabel;

	/**
	 * A list of code ranges that corresponds to instrumentation instructions
	 * added by TOD.
	 */
	private final RangeManager itsInstrumentationRanges;
	
	public ASMBehaviorInstrumenter(
			ASMDebuggerConfig aConfig,
			MethodVisitor mv,
			IMutableBehaviorInfo aBehavior,
			ASMMethodInfo aMethodInfo)
	{
		itsConfig = aConfig;
		this.mv = mv;
		itsBehavior = aBehavior;
		
		itsInstrumentationRanges = new RangeManager(mv);
		
		// TODO: _getMutableDatabase is a workaround for a jdk compiler bug
		itsStructureDatabase = itsBehavior._getMutableDatabase();
		itsProbesManager = new ProbesManager(itsStructureDatabase);
		itsMethodInfo = aMethodInfo;
		itsBehaviorCallInstrumenter = new ASMBehaviorCallInstrumenter(mv, this, itsBehavior.getId());
		
		itsFirstFreeVar = itsMethodInfo.getMaxLocals();
		
		// Allocate space for return var
		itsReturnLocationVar = itsFirstFreeVar;
		itsFirstFreeVar += 1;
		
		// Allocate space for interpreter var
		itsCollectorVar = itsFirstFreeVar;
		itsFirstFreeVar += 1;
	}
	
	/**
	 * Fills the provided tag map with the instrumentation tags
	 */
	public void fillTagMap(TagMap aTagMap)
	{
		System.out.println("----------");
		System.out.println(itsBehavior);
		
		for (CodeRange theRange : itsInstrumentationRanges.getRanges())
		{
			System.out.println("Range: "+theRange.start.getOffset()+"-"+theRange.end.getOffset());
			aTagMap.putTagRange(
					BytecodeTagType.ROLE, 
					BytecodeRole.TOD_CODE, 
					theRange.start.getOffset(),
					theRange.end.getOffset());
		}

		System.out.println("----------");
		System.out.println();
	}
	
	/**
	 * Updates the probes used in the behavior:
	 * <li> Resolve bytecode indexes
	 * <li> Include advice source id information (if tagmap is specified).
	 */
	public void updateProbes(TagMap aTagMap)
	{
		int theBehaviorId = itsBehavior.getId();
		for (TmpProbeInfo theProbe : itsProbesManager.getProbes())
		{
			int theBytecodeIndex = theProbe.label.getOffset();
			
			Integer theAdviceSourceId = aTagMap != null ? 
					aTagMap.getTag(BytecodeTagType.ADVICE_SOURCE_ID, theBytecodeIndex) 
					: null;
					
			BytecodeRole theRole = aTagMap != null ? 
					aTagMap.getTag(BytecodeTagType.ROLE, theBytecodeIndex)
					: null;
			
			itsStructureDatabase.setProbe(
					theProbe.id, 
					theBehaviorId, 
					theBytecodeIndex, 
					theRole,
					theAdviceSourceId != null ? theAdviceSourceId : -1);
		}
	}
	
	/**
	 * Creates a new probe and generates an instruction that pushes its id
	 * on the stack.
	 */
	public void pushProbeId(Label aLabel)
	{
		int theId = itsProbesManager.createProbe(aLabel);
		BCIUtils.pushInt(mv, theId);
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
		itsInstrumentationRanges.start();
		
		itsReturnHookLabel = new Label();
		itsFinallyHookLabel = new Label();
		itsCodeStartLabel = new Label();

		// Obtain the event interpreter and store it into the interpreter var.
		mv.visitMethodInsn(
				INVOKESTATIC, 
				Type.getInternalName(AgentConfig.class), 
				"getCollector", 
				"()"+Type.getDescriptor(EventCollector.class));
		mv.visitVarInsn(ASTORE, itsCollectorVar);
		
		
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
		
		mv.visitMethodInsn(
				INVOKESTATIC, 
				Type.getInternalName(ExceptionGeneratedReceiver.class), 
				"ignoreNextException", 
				"()V");
		
		mv.visitInsn(ATHROW);

		mv.visitLabel(itsCodeStartLabel);
		
		itsInstrumentationRanges.end();
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
		
		pushProbeId(l);
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
				itsBehavior.getId(),
				"<clinit>".equals(itsBehavior.getName()),
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
		
		invokeLogBehaviorExit(
				itsBehavior.getId(), 
				"<clinit>".equals(itsBehavior.getName()),
				itsFirstFreeVar);
	}
	
	public void behaviorExitWithException()
	{
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, itsFirstFreeVar);
		
		invokeLogBehaviorExitWithException(itsBehavior.getId(), itsFirstFreeVar);
	}
	
	/**
	 * Determines if the given behavior is traced.
	 */
	private HasTrace hasTrace(IBehaviorInfo aCalledBehavior)
	{
//		// If the target class is in scope, the method is traced.
//		String theClassName = aCalledBehavior.getType().getName();
//		if (itsConfig.isInScope(theClassName)) return HasTrace.YES;
		
		// Otherwise:
		// A class that has trace might inherit methods from
		// a non-traced superclass. Therefore we cannot be sure that
		// the called method is indeed traced. On the other hand if the
		// owner class has no trace, we are sure that the called method
		// is also traced, if the filters respect our requirement that
		// a traced class' subclasses must also be traced.
		return aCalledBehavior.hasTrace();
	}
	
	public void methodCall(
			int aOpcode,
			String aOwner, 
			String aName,
			String aDesc,
			BehaviorCallType aCallType)
	{
		boolean theStatic = aOpcode == INVOKESTATIC;
		
		IMutableClassInfo theOwner = itsStructureDatabase.getNewClass(Util.jvmToScreen(aOwner));
		IMutableBehaviorInfo theCalledBehavior = theOwner.getNewBehavior(aName, aDesc);

		Label theOriginalCallLabel = new Label();
		
		itsBehaviorCallInstrumenter.setup(
				theOriginalCallLabel,
				itsFirstFreeVar,
				theCalledBehavior.getId(),
				aDesc,
				theStatic);
		
		
		HasTrace theHasTrace = hasTrace(theCalledBehavior);
		
		Label theElse = new Label();
		Label theEndif = new Label();
		
		itsInstrumentationRanges.start();
		boolean theOriginalDone = false; // We must have exactly one original method call not tagged as TOD_CODE

		if (theHasTrace == HasTrace.UNKNOWN)
		{
			// Force class load
			
			mv.visitLdcInsn(Type.getObjectType(aOwner));
			mv.visitInsn(POP);
			
			// Runtime check for trace info
			
			BCIUtils.pushInt(mv, theCalledBehavior.getId());
			
			mv.visitMethodInsn(
					INVOKESTATIC, 
					Type.getInternalName(TracedMethods.class), 
					"isTraced", 
					"(I)Z");
			
			mv.visitJumpInsn(IFEQ, theElse);
		}
		
		if (theHasTrace == HasTrace.UNKNOWN || theHasTrace == HasTrace.YES)
		{
			// Handle before method call
			itsBehaviorCallInstrumenter.callLogBeforeBehaviorCallDry(aCallType);
			
			itsInstrumentationRanges.end();
			theOriginalDone = true;
			
			mv.visitLabel(theOriginalCallLabel);
			
			// Do the original call
			mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
			
			itsInstrumentationRanges.start();
			
			// Handle after method call
			itsBehaviorCallInstrumenter.callLogAfterBehaviorCallDry();						
		}
		
		if (theHasTrace == HasTrace.UNKNOWN)
		{
			mv.visitJumpInsn(GOTO, theEndif);
			mv.visitLabel(theElse);
		}
		
		if (theHasTrace == HasTrace.UNKNOWN || theHasTrace == HasTrace.NO)
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
			
			if (! theOriginalDone) 
			{
				itsInstrumentationRanges.end();
				mv.visitLabel(theOriginalCallLabel);
			}
			
			// Do the original call
			mv.visitMethodInsn(aOpcode, aOwner, aName, aDesc);
			
			if (! theOriginalDone) itsInstrumentationRanges.start();
			
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

		if (theHasTrace == HasTrace.UNKNOWN)
		{
			mv.visitLabel(theEndif);
		}
		
		itsInstrumentationRanges.end();
	}
	
	
	public void fieldWrite(
			int aOpcode, 
			String aOwner, 
			String aName, 
			String aDesc)
	{
		boolean theStatic = aOpcode == PUTSTATIC;

		IMutableClassInfo theOwner = itsStructureDatabase.getNewClass(Util.jvmToScreen(aOwner));

		ITypeInfo theType = itsStructureDatabase.getNewType(aDesc);
		IFieldInfo theField = theOwner.getNewField(aName, theType);
		
		Type theASMType = Type.getType(aDesc);
		
		Label theOriginalInstructionLabel = new Label();

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
		
		itsInstrumentationRanges.start();
		
		// :: [target], value
	
		// Store parameters
		
		mv.visitVarInsn(theASMType.getOpcode(ISTORE), theValueVar);
		if (! theStatic) mv.visitVarInsn(ASTORE, theTargetVar);
		
		// Call log method
		invokeLogFieldWrite(theOriginalInstructionLabel, theField.getId(), theTargetVar, theASMType, theValueVar);
		
		// Push parameters back to stack
		if (! theStatic) mv.visitVarInsn(ALOAD, theTargetVar);
		mv.visitVarInsn(theASMType.getOpcode(ILOAD), theValueVar);
		
		itsInstrumentationRanges.end();

		// Do the original operation
		mv.visitLabel(theOriginalInstructionLabel);
		mv.visitFieldInsn(aOpcode, aOwner, aName, aDesc);
	}
	
	public void variableWrite(
			int aOpcode, 
			int aVar)
	{
		int theSort = BCIUtils.getSort(aOpcode);
		
		Label theOriginalInstructionLabel = new Label();

		// :: value
	
		// Perform store
		mv.visitLabel(theOriginalInstructionLabel);
		mv.visitVarInsn(aOpcode, aVar);
		
		itsInstrumentationRanges.start();
		
		// Call log method
		invokeLogLocalVariableWrite(
				theOriginalInstructionLabel, 
				aVar, 
				BCIUtils.getType(theSort), 
				aVar);
		
		itsInstrumentationRanges.end();
	}
	
	public void variableInc(
			int aVar, 
			int aIncrement)
	{
		Label theOriginalInstructionLabel = new Label();

		// :: value
	
		// Perform store
		mv.visitLabel(theOriginalInstructionLabel);
		mv.visitIincInsn(aVar, aIncrement);
		
		itsInstrumentationRanges.start();
		
		// Call log method
		invokeLogLocalVariableWrite(
				theOriginalInstructionLabel, 
				aVar, 
				BCIUtils.getType(Type.INT), 
				aVar);
		
		itsInstrumentationRanges.end();
	}

	public void newArray(NewArrayClosure aClosure, int aBaseTypeId)
	{
		Label theOriginalInstructionLabel = new Label();

		itsInstrumentationRanges.start();
		
		// :: size
		
		int theCurrentVar = itsFirstFreeVar;
		int theSizeVar;
		int theTargetVar;
		
		theSizeVar = theCurrentVar++;
		theTargetVar = theCurrentVar++;
		
		// Store size
		mv.visitVarInsn(ISTORE, theSizeVar);
		
		// Reload size
		mv.visitVarInsn(ILOAD, theSizeVar);
	
		itsInstrumentationRanges.end();
		
		// Perform new array
		mv.visitLabel(theOriginalInstructionLabel);
		aClosure.proceed(mv);
		
		itsInstrumentationRanges.start();
		
		// :: array
		
		// Store target
		mv.visitVarInsn(ASTORE, theTargetVar);
		
		// Reload target
		mv.visitVarInsn(ALOAD, theTargetVar);

		// Call log method (if no exception occurred)
		invokeLogNewArray(
				theOriginalInstructionLabel, 
				theTargetVar, 
				aBaseTypeId, 
				theSizeVar);
		
		itsInstrumentationRanges.end();
	}
	
	public void arrayWrite(int aOpcode)
	{
		int theSort = BCIUtils.getSort(aOpcode);
		Type theType = BCIUtils.getType(theSort);
		
		Label theOriginalInstructionLabel = new Label();
		
		itsInstrumentationRanges.start();
		
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
		
		itsInstrumentationRanges.end();
		
		// Perform store
		mv.visitLabel(theOriginalInstructionLabel);
		mv.visitInsn(aOpcode);
		
		itsInstrumentationRanges.start();
		
		// Call log method (if no exception occurred)
		invokeLogArrayWrite(
				theOriginalInstructionLabel, 
				theTargetVar, 
				theIndexVar, 
				theType, 
				theValueVar);
		
		itsInstrumentationRanges.end();
	}
	
	public void instanceOf(String aDesc, ITypeInfo aType)
	{
		Label theOriginalInstructionLabel = new Label();
		
		itsInstrumentationRanges.start();
		
		// :: object
		
		int theCurrentVar = itsFirstFreeVar;
		int theObjectVar = theCurrentVar++;
		int theResultVar = theCurrentVar++;
		
		// Store parameters
		
		mv.visitVarInsn(ASTORE, theObjectVar);
		
		// Reload parameters
		mv.visitVarInsn(ALOAD, theObjectVar);
		
		itsInstrumentationRanges.end();
		
		// Perform original instanceof
		mv.visitLabel(theOriginalInstructionLabel);
		mv.visitTypeInsn(INSTANCEOF, aDesc);
		
		itsInstrumentationRanges.start();
		
		mv.visitVarInsn(ISTORE, theResultVar);
		
		// Call log method (if no exception occurred)
		invokeLogInstanceOf(theOriginalInstructionLabel, theObjectVar, aType, theResultVar);
		
		// Reload result
		mv.visitVarInsn(ILOAD, theResultVar);
		
		itsInstrumentationRanges.end();
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
		mv.visitVarInsn(ALOAD, itsCollectorVar);
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
		mv.visitVarInsn(ALOAD, itsCollectorVar);
	}
	
	public void invokeLogBeforeBehaviorCall(
			Label aOriginalInstructionLabel, 
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
	
		// ->operation location
		pushProbeId(aOriginalInstructionLabel);
		
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
				Type.getInternalName(EventCollector.class), 
				"logBeforeBehaviorCall", 
				"(II"+Type.getDescriptor(BehaviorCallType.class)+"Ljava/lang/Object;[Ljava/lang/Object;)V");
		
		mv.visitLabel(l);
	}

	public void invokeLogBeforeBehaviorCallDry(
			Label aOriginalInstructionLabel, 
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
		
		// ->operation location
		pushProbeId(aOriginalInstructionLabel);
		
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
				Type.getInternalName(EventCollector.class), 
				"logBeforeBehaviorCallDry", 
				"(II"+Type.getDescriptor(BehaviorCallType.class)+")V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogAfterBehaviorCall(
			Label aOriginalInstructionLabel, 
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
		
		// ->operation location
		pushProbeId(aOriginalInstructionLabel);
		
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
		
		// ->target
		if (aTargetVar < 0) mv.visitInsn(ACONST_NULL);
		else mv.visitVarInsn(ALOAD, aTargetVar);
		
		// ->result
		mv.visitVarInsn(ALOAD, aResultVar);
		
		mv.visitMethodInsn(
				INVOKEVIRTUAL, 
				Type.getInternalName(EventCollector.class), 
				"logAfterBehaviorCall", 
				"(IILjava/lang/Object;Ljava/lang/Object;)V");
		
		mv.visitLabel(l);
	}

	public void invokeLogAfterBehaviorCallDry()
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
				Type.getInternalName(EventCollector.class), 
				"logAfterBehaviorCallDry", 
				"()V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogAfterBehaviorCallWithException(
			Label aOriginalInstructionLabel, 
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
		
		// ->operation location
		pushProbeId(aOriginalInstructionLabel);
		
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
		
		// ->target
		if (aTargetVar < 0) mv.visitInsn(ACONST_NULL);
		else mv.visitVarInsn(ALOAD, aTargetVar);
		
		// ->result
		mv.visitVarInsn(ALOAD, aExceptionVar);
		
		mv.visitMethodInsn(
				INVOKEVIRTUAL, 
				Type.getInternalName(EventCollector.class), 
				"logAfterBehaviorCallWithException", 
				"(IILjava/lang/Object;Ljava/lang/Object;)V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogFieldWrite(
			Label aOriginalInstructionLabel, 
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
		
		// ->operation location
		pushProbeId(aOriginalInstructionLabel);
		
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
				Type.getInternalName(EventCollector.class), 
				"logFieldWrite", 
				"(IILjava/lang/Object;Ljava/lang/Object;)V");
		
		mv.visitLabel(l);
	}

	public void invokeLogNewArray(
			Label aOriginalInstructionLabel, 
			int aTargetVar,
			int aBaseTypeId,
			int aSizeVar)
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
		pushStdLogArgs();
		
		// ->operation location
		pushProbeId(aOriginalInstructionLabel);
		
		// ->target
		mv.visitVarInsn(ALOAD, aTargetVar);
		
		// ->type id
		BCIUtils.pushInt(mv, aBaseTypeId);
		
		// ->size
		mv.visitVarInsn(ILOAD, aSizeVar);
		
		mv.visitMethodInsn(
				INVOKEVIRTUAL, 
				Type.getInternalName(EventCollector.class), 
				"logNewArray", 
				"(ILjava/lang/Object;II)V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogArrayWrite(
			Label aOriginalInstructionLabel, 
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
		
		// ->operation location
		pushProbeId(aOriginalInstructionLabel);
		
		// ->target
		mv.visitVarInsn(ALOAD, aTargetVar);
		
		// ->index
		mv.visitVarInsn(ILOAD, aIndexVar);
		
		// ->value
		mv.visitVarInsn(theType.getOpcode(ILOAD), aValueVar);
		BCIUtils.wrap(mv, theType);
		
		mv.visitMethodInsn(
				INVOKEVIRTUAL, 
				Type.getInternalName(EventCollector.class), 
				"logArrayWrite", 
				"(ILjava/lang/Object;ILjava/lang/Object;)V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogInstanceOf(
			Label aOriginalInstructionLabel, 
			int aObjectVar,
			ITypeInfo aType,
			int aResultVar)
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
		pushStdLogArgs();
		
		// ->operation location
		pushProbeId(aOriginalInstructionLabel);
		
		// ->object
		mv.visitVarInsn(ALOAD, aObjectVar);
		
		// ->type id
		BCIUtils.pushInt(mv, aType.getId());
		
		// ->result
		mv.visitVarInsn(ILOAD, aResultVar);
		
		mv.visitMethodInsn(
				INVOKEVIRTUAL, 
				Type.getInternalName(EventCollector.class), 
				"logInstanceOf",
				"(ILjava/lang/Object;II)V");
		
		mv.visitLabel(l);
	}
	
	public void invokeLogLocalVariableWrite(
			Label aOriginalInstructionLabel, 
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
		
		// ->operation location
		pushProbeId(aOriginalInstructionLabel);
		
		// ->variable id
		BCIUtils.pushInt(mv, aVariableId);
		
		// ->value
		mv.visitVarInsn(theType.getOpcode(ILOAD), aValueVar);
		BCIUtils.wrap(mv, theType);
		
		mv.visitMethodInsn(
				INVOKEVIRTUAL, 
				Type.getInternalName(EventCollector.class), 
				"logLocalVariableWrite", 
				"(IILjava/lang/Object;)V");
		
		mv.visitLabel(l);
	}

	public void invokeLogBehaviorEnter (
			int aMethodId,
			boolean aClInit,
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
				Type.getInternalName(EventCollector.class), 
				aClInit ? "logClInitEnter" : "logBehaviorEnter", 
				"(I"+Type.getDescriptor(BehaviorCallType.class)+"Ljava/lang/Object;[Ljava/lang/Object;)V");	
	
		mv.visitLabel(l);
	}

	public void invokeLogBehaviorExit (
			int aMethodId, 
			boolean aClInit,
			int aResultVar)
	{
		Label l = new Label();
		if (LogBCIVisitor.ENABLE_READY_CHECK)
		{
			mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AgentReady.class), "READY", "Z");
			mv.visitJumpInsn(IFEQ, l);
		}
		
		pushStdLogArgs();
		
		// ->operation location
		mv.visitVarInsn(ILOAD, itsReturnLocationVar);
		
		// ->method id
		BCIUtils.pushInt(mv, aMethodId);
		
		// ->result
		mv.visitVarInsn(ALOAD, aResultVar);

		mv.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL, 
				Type.getInternalName(EventCollector.class), 
				aClInit ? "logClInitExit" : "logBehaviorExit", 
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
				Type.getInternalName(EventCollector.class), 
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

	/**
	 * Creates a {@link NewArrayClosure} for the NEWARRAY opcode.
	 */
	public static NewArrayClosure createNewArrayClosure(final int aOperand)
	{
		return new NewArrayClosure()
		{
			@Override
			public void proceed(MethodVisitor mv)
			{
				mv.visitIntInsn(NEWARRAY, aOperand);
			}
		};
	}
	
	/**
	 * Creates a {@link NewArrayClosure} for the ANEWARRAY opcode.
	 */
	public static NewArrayClosure createNewArrayClosure(final String aDesc)
	{
		return new NewArrayClosure()
		{
			@Override
			public void proceed(MethodVisitor mv)
			{
				mv.visitTypeInsn(ANEWARRAY, aDesc);
			}
		};
	}
	
	/**
	 * A closure for generating the bytecode for NEWARRAY instructions.
	 * @author gpothier
	 */
	public static abstract class NewArrayClosure
	{
		public abstract void proceed(MethodVisitor mv);
	}

}
