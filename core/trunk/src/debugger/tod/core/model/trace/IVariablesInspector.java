/*
 * Created on Nov 4, 2005
 */
package tod.core.model.trace;

import java.util.List;

import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.BehaviorInfo;

/**
 * Permits to determine the value of local variables during a method execution.
 * The inspector maintains a current event; variable values are obtained with respect
 * to the current event, ie. the inspector returns the value a given variable had
 * at the moment the current event was being executed.
 * <br/>
 * There are no uncertainties in evaluating the values of local variables as only
 * one thread accesses them.  
 * 
 * @see tod.core.model.trace.IEventTrace#createVariablesInspector(IBehaviorEnterEvent)
 * @author gpothier
 */
public interface IVariablesInspector
{
	/**
	 * Returns the behavior enter event that represents the method execution 
	 * analysed by this inspector.
	 */
	public IBehaviorCallEvent getBehaviorCall();
	
	/**
	 * Returns the analysed behavior.
	 */
	public BehaviorInfo getBehavior();
	
	/**
	 * Returns a list of all the local variables available 
	 * in the analysed method
	 */
	public List<LocalVariableInfo> getVariables();
	
	/**
	 * Returns a list of all the local variables available at the specified
	 * bytecode index in the analysed method.
	 */
	public List<LocalVariableInfo> getVariables(int aBytecodeIndex);
	
	/**
	 * Returns the current event of this inspector.
	 */
	public ILogEvent getCurrentEvent();
	
	/**
	 * Sets the current event of this inspector. Values of variables 
	 * obtained by {@link #getVariableValue(LocalVariableInfo)} 
	 * are the values they had at the moment
	 * the current event was executed.
	 * @param aEvent An event that occured during the execution of 
	 * the analysed behavior (it must be a direct child).
	 */
	public void setCurrentEvent (ILogEvent aEvent);
	
	/**
	 * Returns the value of the specified variable at the time the 
	 * current event was executed.
	 */
	public Object getVariableValue (LocalVariableInfo aVariable);
}
