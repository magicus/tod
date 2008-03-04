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
package tod.core.database.browser;

import java.util.List;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;

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
public interface IVariablesInspector extends ICompoundInspector<LocalVariableInfo>
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
}
