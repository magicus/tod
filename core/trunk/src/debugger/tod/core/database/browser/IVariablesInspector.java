/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.core.database.browser;

import java.util.List;

import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;

/**
 * Permits to determine the value of local variables during a method execution.
 * The inspector maintains a current event; variable values are obtained with respect
 * to the current event, ie. the inspector returns the value a given variable had
 * at the moment the current event was being executed.
 * <br/>
 * There are no uncertainties in evaluating the values of local variables as only
 * one thread accesses them.  
 * 
 * @see tod.core.database.browser.ILogBrowser#createVariablesInspector(IBehaviorEnterEvent)
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
	public IBehaviorInfo getBehavior();
	
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
	
	/**
	 * Returns the event that set the variable to the value it had at
	 * the time the current event was executed.
	 */
	public ILocalVariableWriteEvent getVariableSetter(LocalVariableInfo aVariable);
}
