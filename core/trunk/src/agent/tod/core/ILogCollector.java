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
	 * @param aOperationBytecodeIndex Index of the operation in the behavior's bytecode
	 * @param aVariableId If positive, index of the variable's symbolic information
	 * in the LocalVariablesTable attribute. 
	 * If negative, index = -id-1, index of the variable's storage in the frame's local variables array.
	 * @param aValue The new value of the variable.
	 */
	public void logLocalVariableWrite(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aVariableId, 
			Object aValue);
	
	/**
	 * Indicates that the next "before method call" event will be a
	 * call to a first-level constructor for an instantiation.
	 */
	public void logInstantiation(long aThreadId);
	
	/**
	 * Indicates that the next "before method call" event will be a
	 * call to super(...) or this(...) from within a constructor.
	 */
	public void logConstructorChaining(long aThreadId);
	
	/**
	 * Called before a behavior is called,
	 * when the target behavior is known not to be instrumented for tracing.
	 * @param aBehaviorLocationId Id of the called method.
	 * @param aTarget The object on which the method is called.
	 * @param aArguments The arguments passed to the called method.
	 * @param aOperationBytecodeIndex Index of the operation in the behavior's bytecode
	 */
	public void logBeforeBehaviorCall(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aBehaviorLocationId,
			Object aTarget,
			Object[] aArguments);
	
	/**
	 * Called before a method is called,
	 * when the target behavior is known to be instrumented for tracing. 
	 * The next event on the thread will be a "behavior enter"
	 * @param aBehaviorLocationId Id of the called method.
	 * @param aOperationBytecodeIndex Index of the operation in the behavior's bytecode
	 */
	public void logBeforeBehaviorCall(
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aBehaviorLocationId);
	
	/**
	 * Called after a behavior call has returned normally, 
	 * when the target behavior is known not to be instrumented for tracing.
	 * @param aBehaviorLocationId Id of the called method.
	 * @param aTarget The object on which the method is called.
	 * @param aResult The result of the call.
	 * @param aOperationBytecodeIndex Index of the operation in the behavior's bytecode
	 */
	public void logAfterBehaviorCall(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aBehaviorLocationId,
			Object aTarget,
			Object aResult);
	
	/**
	 * Called after a behavior call has returned normally or with an exception, 
	 * when the target behavior is known to be instrumented for tracing. 
	 * The return value of the behavior, or the thrown exception,
	 * if any, is provided by the "behavior exit" event that precedes this event.
	 * @param aMethodLocationId Id of the called method.
	 */
	public void logAfterBehaviorCall(long aThreadId);
	
	/**
	 * Called after a method call has returned with an exception,
	 * when the target behavior is known not to be instrumented for tracing.
	 * @param aBehaviorLocationId Id of the called method.
	 * @param aTarget The object on which the method is called.
	 * @param aException The exception thrown by the method.
	 * @param aOperationBytecodeIndex Index of the operation in the behavior's bytecode
	 */
	public void logAfterBehaviorCallWithException(
			long aTimestamp, 
			long aThreadId, 
			int aOperationBytecodeIndex, 
			int aBehaviorLocationId,
			Object aTarget,
			Object aException);
	
	/**
	 * Called when execution of a behaviour (method, constructor, static block) starts.
	 * @param aBehaviorLocationId Id of the behaviour
	 * @param aTarget The object on which the behaviour is executed, or null if static.
	 * @param aArguments The arguments passed to the behaviour
	 * @param aObject The current object on which the behavior executes.
	 */
	public void logBehaviorEnter(
			long aTimestamp, 
			long aThreadId, 
			int aBehaviorLocationId,
			Object aObject,
			Object[] aArguments);
	
	/**
	 * Called when execution of a behaviour (method, constructor, static block) ends
	 * normally (not because of an exception).
	 * @param aBehaviorLocationId Id of the behaviour
	 * @param aTarget The object on which the behaviour is executed, or null if static.
	 * @param aValue Returns value of the behaviour, if applicable
	 */
	public void logBehaviorExit(
			long aTimestamp, 
			long aThreadId, 
			int aBehaviorLocationId,
			Object aResult);

	/**
	 * Called when the execution of a behavior ends abruptly.
	 * @param aBehaviorLocationId The Id of the behavior
	 * @param aException The exception thrown
	 */
	public void logBehaviorExitWithException(
			long aTimestamp, 
			long aThreadId, 
			int aBehaviorLocationId,
			Object aException);
	
	/**
	 * Called when an exception is generated.
	 * @param aBehaviorLocationId Id of the behavior in which the exception
	 * was generated
	 * @param aOperationBytecodeIndex Precise location of the exception generation
	 * @param aException Generated exception
	 */
	public void logExceptionGenerated(
			long aTimestamp, 
			long aThreadId, 
			int aBehaviorLocationId,
			int aOperationBytecodeIndex,
			Object aException);
	
	/**
	 * This is another version of {@link #logExceptionGenerated(long, long, int, int, Object)},
	 * used when the agent canot resolve class and method names.
	 */
	public void logExceptionGenerated(
			long aTimestamp,
			long aThreadId,
			String aMethodName,
			String aMethodSignature,
			String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex,
			Object aException);
	
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
