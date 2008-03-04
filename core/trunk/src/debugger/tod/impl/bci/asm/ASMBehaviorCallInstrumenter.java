/*
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

import tod.agent.BehaviorCallType;

public class ASMBehaviorCallInstrumenter implements Opcodes
{
	private final MethodVisitor mv;
	private final ASMBehaviorInstrumenter itsInstrumenter;
	
	private int itsMethodId;
	private boolean itsStatic;
	private Type[] itsArgTypes;
	private Type itsReturnType;

	private int itsOperationBehaviorId;
	
	/**
	 * The label that corresponds to the actual call instruction.
	 * The client should provide a label to the call instrumenter,
	 * and visit the label just prior to the actual call.
	 */
	private Label itsOriginalInstructionLabel;
	
	private int itsFirstVar;
	private int itsAfterArgumentsVar;
	private int itsTargetVar; 
	private int itsArrayVar;

	
	public ASMBehaviorCallInstrumenter(
			MethodVisitor mv, 
			ASMBehaviorInstrumenter aInstrumenter,
			int aOperationBehaviorId)
	{
		this.mv = mv;
		itsInstrumenter = aInstrumenter;
		itsOperationBehaviorId = aOperationBehaviorId;
	}

	public void setup(
			Label aLabel,
			int aMaxLocals,
			int aMethodId, 
			String aDesc, 
			boolean aStatic)
	{
		itsFirstVar = aMaxLocals;
		itsMethodId = aMethodId;
		itsStatic = aStatic;
		itsArgTypes = Type.getArgumentTypes(aDesc);
		itsReturnType = Type.getReturnType(aDesc);
		
		itsOriginalInstructionLabel = aLabel;
		
		itsAfterArgumentsVar = -1;
		itsTargetVar = -1;
		itsArrayVar = -1;
	}
	
	/**
	 * Generates the bytecode that stores method parameters to local variables
	 * Stack: [target], arg1, ..., argN => . 
	 */
	public void storeArgsToLocals()
	{
		int theCurrentVar = itsFirstVar;
		for (int i=itsArgTypes.length-1;i>=0;i--)
		{
			Type theType = itsArgTypes[i];
			mv.visitVarInsn(theType.getOpcode(ISTORE), theCurrentVar);
			theCurrentVar += theType.getSize();
		}
		
		itsAfterArgumentsVar = theCurrentVar; // The variable after all argument vars
		
		if (itsStatic) 
		{
			itsTargetVar = -1;
			itsArrayVar = theCurrentVar;				
		}
		else
		{
			itsTargetVar = theCurrentVar;
			itsArrayVar = itsTargetVar+1;
			mv.visitVarInsn(ASTORE, itsTargetVar);
		}
	}
	
	/**
	 * Generates the bytecode that creates the arguments array and stores
	 * it into a variable.
	 * Stack: . => .
	 */
	public void createArgsArray()
	{
		itsInstrumenter.createArgsArray(itsArgTypes, itsArrayVar, itsAfterArgumentsVar, true);
	}
	
	/**
	 * Generates the code that calls 
	 * {@link tod.core.ILogCollector#logBeforeBehaviorCall(long, long, int, int, Object, Object[])}
	 */
	public void callLogBeforeMethodCall(BehaviorCallType aCallType)
	{
		itsInstrumenter.invokeLogBeforeBehaviorCall(
				itsOriginalInstructionLabel, 
				itsMethodId, 
				aCallType,
				itsStatic ? -1 : itsTargetVar, 
				itsArrayVar);
	}
	
	/**
	 * Generates the code that calls 
	 * {@link tod.core.ILogCollector#logBeforeBehaviorCall(long, long, int, int, Object, Object[])}
	 */
	public void callLogBeforeBehaviorCallDry(BehaviorCallType aCallType)
	{
		itsInstrumenter.invokeLogBeforeBehaviorCallDry(
				itsOriginalInstructionLabel, 
				itsMethodId,
				aCallType);
	}
	
	/**
	 * Generates the code that pushes stored arguments back to the stack
	 * Stack: . => [target], arg1, ..., argN
	 */
	public void pushArgs()
	{
		// Push target & arguments back to the stack
		if (! itsStatic) mv.visitVarInsn(ALOAD, itsTargetVar);
		
		int theCurrentVar = itsAfterArgumentsVar;
		for (Type theType : itsArgTypes)
		{
			theCurrentVar -= theType.getSize();
			mv.visitVarInsn(theType.getOpcode(ILOAD), theCurrentVar);
		}
	}
	
	/**
	 * Generates the code that calls 
	 * {@link tod.core.ILogCollector#logAfterBehaviorCall(long, long, int, int, Object, Object)}
	 */
	public void callLogAfterMethodCall()
	{
		// :: [result]
		
		int theSort = itsReturnType.getSort();
		if (theSort == Type.VOID)
		{
			mv.visitInsn(ACONST_NULL);
		}
		else
		{
			mv.visitInsn(itsReturnType.getSize() == 2 ? DUP2 : DUP);
			BCIUtils.wrap(mv, itsReturnType);
		}
		
		mv.visitVarInsn(ASTORE, itsArrayVar);
		
		// :: [result]
		
		itsInstrumenter.invokeLogAfterBehaviorCall(
				itsOriginalInstructionLabel, 
				itsMethodId, 
				itsStatic ? -1 : itsTargetVar, 
				itsArrayVar);
	}

	/**
	 * Generates the code that calls 
	 * {@link tod.core.ILogCollector#logAfterBehaviorCall(long, long, int, int, Object, Object)}
	 */
	public void callLogAfterMethodCallWithException()
	{
		// :: [exception]
		
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, itsArrayVar);
		
		// :: [exception]
		
		itsInstrumenter.invokeLogAfterBehaviorCallWithException(
				itsOriginalInstructionLabel, 
				itsMethodId, 
				itsStatic ? -1 : itsTargetVar, 
				itsArrayVar);
	}
	
	/**
	 * Generates the code that calls 
	 * {@link tod.core.ILogCollector#logAfterBehaviorCall(long, long, int, int, Object, Object)}
	 */
	public void callLogAfterBehaviorCallDry()
	{
		itsInstrumenter.invokeLogAfterBehaviorCallDry();
	}
	
	
}
