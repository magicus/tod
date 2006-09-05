/*
 * Created on Nov 10, 2005
 */
package tod.core.database.event;


/**
 * This interface provide methods that give information
 * about a caller-side event.
 * @author gpothier
 */
public interface ICallerSideEvent extends ILogEvent
{
	/**
	 * Bytecode index of the call within the calling behavior.
	 * <br/>
	 * This information is available only if the caller behavior
	 * was instrumented.
	 * @return Index of the call, or -1 if not available
	 * @see #getCallingBehavior()
	 */
	public int getOperationBytecodeIndex();

}
