/*
 * Created on Oct 9, 2004
 */
package tod.core;


/**
 * This interface is for objects that collect log events.
 * Methods of this interface are called synchronously as
 * events occur in each thread.
 * @author gpothier
 */
public interface ILogCollector extends ILocationRegistrer
{
	/**
	 * Called when a field write operation is performed.
	 * @param aFieldLocationId The location id of the written field
	 * @param aTarget The object into which the field was written
	 * @param aValue The new value of the field.
	 * @param aOperationBytecodeIndex Index of the operation in the behavior's bytecode
	 */
	public void logFieldWrite(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aFieldLocationId, 
			Object aTarget, 
			Object aValue);
	
	/**
	 * Called when a local variable write operation is performed.
	 * @param aVariableId If positive, index of the variable's symbolic information
	 * in the LocalVariablesTable attribute. 
	 * If negative, index = -id-1, index of the variable's storage in the frame's local variables array.
	 * @param aTarget The object executing the method that performs the write.
	 * @param aValue The new value of the variable.
	 * @param aOperationBytecodeIndex Index of the operation in the behavior's bytecode
	 */
	public void logLocalVariableWrite(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aVariableId, 
			Object aTarget, 
			Object aValue);
	
	/**
	 * Called after an instantiation occurs
	 * @param aTypeLocationId Id of the type that was instantiated.
	 * @param aInstance The new instance
	 * @param aOperationBytecodeIndex Index of the operation in the behavior's bytecode
	 */
	public void logInstantiation(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aTypeLocationId,
			Object aInstance);
	
	/**
	 * Called before a method is called.
	 * @param aMethodLocationId Id of the called method.
	 * @param aTarget The object on which the method is called.
	 * @param aArguments The arguments passed to the called method.
	 * @param aOperationBytecodeIndex Index of the operation in the behavior's bytecode
	 */
	public void logBeforeMethodCall(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aMethodLocationId,
			Object aTarget,
			Object[] aArguments);
	
	/**
	 * Called before a method is called.
	 * @param aMethodLocationId Id of the called method.
	 * @param aTarget The object on which the method is called.
	 * @param aResult The result of the call.
	 * @param aOperationBytecodeIndex Index of the operation in the behavior's bytecode
	 */
	public void logAfterMethodCall(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aMethodLocationId,
			Object aTarget,
			Object aResult);
	
	/**
	 * Called when execution of a behaviour (method, constructor, static block) starts.
	 * @param aBehaviorLocationId Id of the behaviour
	 * @param aTarget The object on which the behaviour is executed, or null if static.
	 * @param aArguments The arguments passed to the behaviour
	 */
	public void logBehaviorEnter(
			long aTimestamp, 
			long aThreadId, 
			int aBehaviorLocationId);
	
	/**
	 * Called when execution of a behaviour (method, constructor, static block) ends.
	 * @param aBehaviorLocationId Id of the behaviour
	 * @param aTarget The object on which the behaviour is executed, or null if static.
	 * @param aValue Returns value of the behaviour, if applicable
	 */
	public void logBehaviorExit(
			long aTimestamp, 
			long aThreadId, 
			int aBehaviorLocationId);
	
	/**
	 * Called when data is output by a behaviour on the standard output streams
	 * @param aOutput Identifies the output stream
	 * @param aData The output data
	 */
	public void logOutput (
			long aTimestamp,
			long aThreadId, 
			Output aOutput,
			byte[] aData);
	
}
