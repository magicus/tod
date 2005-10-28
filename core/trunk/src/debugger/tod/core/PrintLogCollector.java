/*
 * Created on Oct 9, 2004
 */
package tod.core;

import java.io.PrintStream;
import java.util.Arrays;

import tod.core.ILogCollector;
import tod.core.Output;
import tod.core.ILocationRegistrer.LineNumberInfo;
import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.model.structure.BehaviorInfo;

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
	
	private Stack<BehaviorInfo> itsBehaviorsStack = new ArrayStack<BehaviorInfo>();

	public PrintLogCollector()
	{
		this (System.out);
	}
	
	public PrintLogCollector(PrintStream aOutput)
	{
		itsOutput = aOutput;
	}

	public void logBeforeMethodCall(
			long aTimestamp, 
			long aThreadId,
			int aOperationBytecodeIndex,
			int aMethodLocationId,
			Object aTarget,
			Object[] aArguments)
	{
		if (itsPrintEvents) itsOutput.println(String.format(
                "[LOG] calling method: %s (%d) on %s with (%s)",
                getBehavior(aMethodLocationId).getName(),
                aMethodLocationId,
                aTarget,
                Arrays.asList(aArguments)));
	}
	
	public void logAfterMethodCall(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aMethodLocationId,
			Object aTarget, 
			Object aResult)
	{
		if (itsPrintEvents) itsOutput.println(String.format(
                "[LOG] method called: %s (%d) on %s result: %s",
                getBehavior(aMethodLocationId).getName(),
                aMethodLocationId,
                aTarget,
                aResult));
	}


	public void logBehaviorEnter(
			long aTimestamp,
			long aThreadId, 
			int aBehaviorLocationId)
	{
		BehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);
		if (itsPrintEvents) itsOutput.println(String.format(
				"[LOG] entering: %s (%d)",
				theBehavior.getName(),
				aBehaviorLocationId));
		itsBehaviorsStack.push(theBehavior);
	}

	public void logBehaviorExit(
			long aTimestamp,
			long aThreadId,
			int aBehaviorLocationId)
	{
		BehaviorInfo theBehavior = getBehavior(aBehaviorLocationId);
		if (itsPrintEvents) itsOutput.println(String.format(
				"[LOG] exiting: %s (%d)",
				theBehavior.getName(),
				aBehaviorLocationId));
		if (itsBehaviorsStack.pop() != theBehavior) throw new RuntimeException();
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

	public void logInstantiation(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex,
			int aTypeLocationId,
			Object aInstance)
	{
		if (itsPrintEvents) itsOutput.println(String.format(
				"[LOG] class instantiated: %s (%d), instance: %s",
				getType(aTypeLocationId).getName(),
				aTypeLocationId,
				aInstance));
	}

	public void logLocalVariableWrite(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aVariableId, 
			Object aTarget, 
			Object aValue)
	{
		BehaviorInfo theInfo = itsBehaviorsStack.peek();
        
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
	public void registerBehavior(BehaviourType aBehaviourType, int aBehaviourId, int aTypeId, String aBehaviourName, String aSignature)
	{
		if (itsPrintRegistrations) itsOutput.println(String.format(
                "[LOG] register behavior: %s (%d) in type %s (%d)",
                aBehaviourName,
                aBehaviourId,
                getType(aTypeId).getName(),
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
                getType(aTypeId).getName(),
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
