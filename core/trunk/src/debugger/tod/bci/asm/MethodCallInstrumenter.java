/*
 * Created on Oct 25, 2005
 */
package tod.bci.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MethodCallInstrumenter implements Opcodes
{
	private MethodVisitor mv;
	private final int itsMethodId;
	private final boolean itsStatic;
	private Type[] itsArgTypes;
	private Type itsReturnType;

	private int itsBytecodeIndex;
	
	private final int itsFirstVar;
	private int itsAfterArgumentsVar;
	private int itsTargetVar; 
	private int itsArrayVar;

	public MethodCallInstrumenter(
			MethodVisitor aVisitor, 
			int aMaxLocals,
			int aMethodId, 
			String aDesc, 
			boolean aStatic)
	{
		mv = aVisitor;
		itsFirstVar = aMaxLocals;
		itsMethodId = aMethodId;
		itsStatic = aStatic;
		itsArgTypes = Type.getArgumentTypes(aDesc);
		itsReturnType = Type.getReturnType(aDesc);
		
		Label l = new Label();
		mv.visitLabel(l);
		itsBytecodeIndex = l.getOffset();
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
		if (itsArgTypes.length > 0)
		{
			mv.visitIntInsn(BIPUSH, itsArgTypes.length);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
			
			// :: array
			mv.visitVarInsn(ASTORE, itsArrayVar);
			
			// ::
			
			int theCurrentVar = itsAfterArgumentsVar;
			short theIndex = 0;
			for (Type theType : itsArgTypes)
			{
				theCurrentVar -= theType.getSize();
				
				mv.visitVarInsn(ALOAD, itsArrayVar); // :: array
				BCIUtils.pushInt(mv, theIndex++); // :: array, index
				mv.visitVarInsn(theType.getOpcode(ILOAD), theCurrentVar); // :: array, index, val
				BCIUtils.wrap(mv, theType);
				mv.visitInsn(AASTORE); // :: 
			}
		}
		else
		{
			mv.visitInsn(ACONST_NULL); // :: null
			mv.visitVarInsn(ASTORE, itsArrayVar); // ::
		}
	}
	
	/**
	 * Generates the code that calls 
	 * {@link tod.core.ILogCollector#logBeforeMethodCall(long, long, int, int, Object, Object[])}
	 */
	public void callLogBeforeMethodCall()
	{
		BCIUtils.invokeLogBeforeMethodCall(
				mv, 
				itsBytecodeIndex, 
				itsMethodId, 
				itsStatic ? -1 : itsTargetVar, 
				itsArrayVar);
	}
	
	/**
	 * Generates the code that pushes stred arguments back to the stack
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
	 * {@link tod.core.ILogCollector#logAfterMethodCall(long, long, int, int, Object, Object)}
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
		
		BCIUtils.invokeLogAfterMethodCall(
				mv, 
				itsBytecodeIndex, 
				itsMethodId, 
				itsStatic ? -1 : itsTargetVar, 
				itsArrayVar);
	}

	
}
