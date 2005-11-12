/*
 * Created on Oct 25, 2005
 */
package tod.bci.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ASMBehaviorCallInstrumenter implements Opcodes
{
	private final MethodVisitor mv;
	private final ASMBehaviorInstrumenter itsInstrumenter;
	
	private int itsMethodId;
	private boolean itsStatic;
	private Type[] itsArgTypes;
	private Type itsReturnType;

	private int itsBytecodeIndex;
	
	private int itsFirstVar;
	private int itsAfterArgumentsVar;
	private int itsTargetVar; 
	private int itsArrayVar;

	
	public ASMBehaviorCallInstrumenter(MethodVisitor mv, ASMBehaviorInstrumenter aInstrumenter)
	{
		this.mv = mv;
		itsInstrumenter = aInstrumenter;
	}

	public void setup(
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
		
		Label l = new Label();
		mv.visitLabel(l);
		itsBytecodeIndex = l.getOffset();
		
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
	public void callLogBeforeMethodCall()
	{
		itsInstrumenter.invokeLogBeforeBehaviorCall(
				itsBytecodeIndex, 
				itsMethodId, 
				itsStatic ? -1 : itsTargetVar, 
				itsArrayVar);
	}
	
	/**
	 * Generates the code that calls 
	 * {@link tod.core.ILogCollector#logBeforeBehaviorCall(long, long, int, int, Object, Object[])}
	 */
	public void callLogBeforeMethodCallDry()
	{
		itsInstrumenter.invokeLogBeforeBehaviorCall(
				itsBytecodeIndex, 
				itsMethodId);
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
				itsBytecodeIndex, 
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
				itsBytecodeIndex, 
				itsMethodId, 
				itsStatic ? -1 : itsTargetVar, 
				itsArrayVar);
	}
	
	/**
	 * Generates the code that calls 
	 * {@link tod.core.ILogCollector#logAfterBehaviorCall(long, long, int, int, Object, Object)}
	 */
	public void callLogAfterMethodCallDry()
	{
		itsInstrumenter.invokeLogAfterBehaviorCall();
	}
	
	
}
