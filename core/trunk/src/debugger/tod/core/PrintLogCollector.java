/*
 * Created on Oct 9, 2004
 */
package tod.core;

import java.io.PrintStream;
import java.util.Arrays;

import tod.core.database.structure.IBehaviorInfo;
import zz.utils.ArrayStack;
import zz.utils.Stack;


/**
 * @author gpothier
 */
public class PrintLogCollector extends LocationRegistrer implements ILogCollector
{
	private PrintStream itsOutput;
	private boolean itsPrintEvents = true;
	private boolean itsPrintRegistrations = false;
	
	private Stack<IBehaviorInfo> itsBehaviorsStack = new ArrayStack<IBehaviorInfo>();

	public PrintLogCollector()
	{
		this (System.out);
	}
	
	public PrintLogCollector(PrintStream aOutput)
	{
		itsOutput = aOutput;
	}

	public void logBeforeBehaviorCall(
			long aTimestamp, 
			long aThreadId,
			int aOperationBytecodeIndex,
			int aBehaviorLocationId,
			Object aTarget,
			Object[] aArguments)
	{
		IBehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);
		if (itsPrintEvents) itsOutput.println(String.format(
                "[LOG] calling method: %s.%s (%d) on %s with (%s)",
				theBehavior.getType().getName(),
                theBehavior.getName(),
                aBehaviorLocationId,
                aTarget,
                Arrays.asList(aArguments)));
	}
	
	public void logAfterBehaviorCall(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aBehaviorLocationId,
			Object aTarget, 
			Object aResult)
	{
		IBehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);
		if (itsPrintEvents) itsOutput.println(String.format(
                "[LOG] method called: %s.%s (%d) on %s result: %s",
				theBehavior.getType().getName(),
                theBehavior.getName(),
                aBehaviorLocationId,
                aTarget,
                aResult));
	}

	public void logAfterBehaviorCallWithException(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aBehaviorLocationId,
			Object aTarget, 
			Object aException)
	{
		IBehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);
		if (itsPrintEvents) itsOutput.println(String.format(
				"[LOG] method returned with exception: %s.%s (%d) on %s exception: %s",
				theBehavior.getType().getName(),
				theBehavior.getName(),
				aBehaviorLocationId,
				aTarget,
				aException));
	}
	

	public void logBehaviorEnter(
			long aTimestamp,
			long aThreadId, 
			int aBehaviorLocationId, 
			Object aObject,
			Object[] aArguments)
	{
		IBehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);
		if (itsPrintEvents) itsOutput.println(String.format(
				"[LOG] entering: %s.%s (%d) on %s with %s",
				theBehavior.getType().getName(),
				theBehavior.getName(),
				aBehaviorLocationId,
				aObject,
				Arrays.asList(aArguments)));
		itsBehaviorsStack.push(theBehavior);
	}

	public void logBehaviorExit(
			long aTimestamp,
			long aThreadId,
			int aBehaviorLocationId,
			Object aResult)
	{
		IBehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);
		if (itsPrintEvents) itsOutput.println(String.format(
				"[LOG] exiting: %s.%s (%d), result: %s",
				theBehavior.getType().getName(),
				theBehavior.getName(),
				aBehaviorLocationId,
				aResult));
		if (itsBehaviorsStack.pop() != theBehavior) throw new RuntimeException();
	}
	
	public void logBehaviorExitWithException(
			long aTimestamp, 
			long aThreadId, 
			int aBehaviorLocationId, 
			Object aException)
	{
		IBehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);
		if (itsPrintEvents) itsOutput.println(String.format(
				"[LOG] exiting with exception: %s.%s (%d)",
				theBehavior.getType().getName(),
				theBehavior.getName(),
				aBehaviorLocationId));
		if (itsBehaviorsStack.pop() != theBehavior) throw new RuntimeException();
	}
	
	public void logExceptionGenerated(
			long aTimestamp, 
			long aThreadId, 
			int aBehaviorLocationId, 
			int aOperationBytecodeIndex, 
			Object aException)
	{
		IBehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);
		if (itsPrintEvents) itsOutput.println(String.format(
				"[LOG] exception generated in: %s.%s (%d)",
				theBehavior.getType().getName(),
				theBehavior.getName(),
				aBehaviorLocationId));
	}
	
	public void logExceptionGenerated(
			long aTimestamp,
			long aThreadId,
			String aMethodName, 
			String aMethodSignature,
			String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex, 
			Object aException)
	{
		if (itsPrintEvents) itsOutput.println(String.format(
				"[LOG] exception generated in: %s.%s: %s",
				aMethodDeclaringClassSignature,
				aMethodName,
				aException));
	}

	public void logFieldWrite(
			long aTimestamp,
			long aThreadId,
			int aOperationBytecodeIndex,
			int aFieldLocationId,
			Object aTarget,
			Object aValue)
	{
		if (itsPrintEvents) itsOutput.println(String.format(
				"[LOG] field written: %s (%d), on: %s, value: %s",
				getField(aFieldLocationId).getName(),
				aFieldLocationId,
				aTarget,
				aValue));
	}

	public void logInstantiation(long aThreadId)
	{
		if (itsPrintEvents) itsOutput.println("[LOG] instantiating");
	}

	public void logAfterBehaviorCall(long aThreadId)
	{
		if (itsPrintEvents) itsOutput.println("[LOG] after method call (dry)");
	}

	public void logBeforeBehaviorCall(
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aBehaviorLocationId)
	{
		IBehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);
		if (itsPrintEvents) itsOutput.println(String.format(
                "[LOG] before behavior call (dry): %s.%s (%d)",
				theBehavior.getType().getName(),
                theBehavior.getName(),
                aBehaviorLocationId));
	}

	public void logConstructorChaining(long aThreadId)
	{
		if (itsPrintEvents) itsOutput.println("[LOG] constructor chaining");
	}

	public void logLocalVariableWrite(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aVariableId, 
			Object aValue)
	{
		IBehaviorInfo theInfo = itsBehaviorsStack.peek();
        
        String theName;
//        if (aVariableId >= 0)
//        {
//    		LocalVariableInfo theLocalVariableInfo = theInfo != null ?
//    				theInfo.getLocalVariableInfo(aVariableId)
//                    : null;
//                    
//    		theName = theLocalVariableInfo != null ? 
//    				theLocalVariableInfo.getVariableName() 
//    				: "$"+aVariableId;
//        }
//        else
//        {
//            theName = "$" + (-aVariableId-1);
//        }

		LocalVariableInfo theLocalVariableInfo = theInfo != null ?
				theInfo.getLocalVariableInfo(aOperationBytecodeIndex, aVariableId)
                : null;
                
		theName = theLocalVariableInfo != null ? 
				theLocalVariableInfo.getVariableName() 
				: "$("+aOperationBytecodeIndex+", "+aVariableId+")";

        
		if (itsPrintEvents) itsOutput.println(String.format(
				"[LOG] written local variable: %s, value: %s",
				theName,
				aValue));
	}

	public void logOutput(
			long aTimestamp,
			long aThreadId,
			Output aOutput,
			byte[] aData)
	{
		if (itsPrintEvents) itsOutput.println("[LOG] output: "+new String(aData));
	}

	@Override
	public void registerBehavior(BehaviourKind aBehaviourType, int aBehaviourId, int aTypeId, String aBehaviourName, String aSignature)
	{
		if (itsPrintRegistrations) itsOutput.println(String.format(
                "[LOG] register behavior: %s (%d) in type %s (%d)",
                aBehaviourName,
                aBehaviourId,
                getClass(aTypeId).getName(),
                aTypeId));

		super.registerBehavior(aBehaviourType, aBehaviourId, aTypeId, aBehaviourName, aSignature);
	}

	@Override
	public void registerBehaviorAttributes(int aBehaviourId, LineNumberInfo[] aLineNumberTable, LocalVariableInfo[] aLocalVariableTable)
	{
		if (itsPrintRegistrations) itsOutput.println(String.format(
                "[LOG] register behavior attributes for: %s (%d)",
                getBehavior(aBehaviourId).getName(),
                aBehaviourId));
		
		super.registerBehaviorAttributes(aBehaviourId, aLineNumberTable, aLocalVariableTable);
	}

	@Override
	public void registerField(int aFieldId, int aTypeId, String aFieldName)
	{
		if (itsPrintRegistrations) itsOutput.println(String.format(
                "[LOG] register field: %s (%d) in type %s (%d)",
                aFieldName,
                aFieldId,
                getClass(aTypeId).getName(),
                aTypeId));
		
		super.registerField(aFieldId, aTypeId, aFieldName);
	}

	@Override
	public void registerFile(int aFileId, String aFileName)
	{
		if (itsPrintRegistrations) itsOutput.println(String.format(
                "[LOG] register file: %s (%d)",
                aFileName,
                aFileId));
		
		super.registerFile(aFileId, aFileName);
	}

	@Override
	public void registerThread(long aThreadId, String aName)
	{
		if (itsPrintRegistrations) itsOutput.println(String.format(
                "[LOG] register thread: %s (%d)",
                aName,
                aThreadId));
		
		super.registerThread(aThreadId, aName);
	}

	@Override
	public void registerType(int aTypeId, String aTypeName, int aSupertypeId, int[] aInterfaceIds)
	{
		if (itsPrintRegistrations) itsOutput.println(String.format(
                "[LOG] register type: %s (%d)",
                aTypeName,
                aTypeId));
		
		super.registerType(aTypeId, aTypeName, aSupertypeId, aInterfaceIds);
	}
	

}
