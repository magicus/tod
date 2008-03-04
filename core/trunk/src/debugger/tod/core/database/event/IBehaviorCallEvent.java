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
package tod.core.database.event;

import tod.core.database.structure.IBehaviorInfo;

/**
 * This event corresponds to the call and execution of 
 * any behavior (method, constructor, ...).
 * <br/> 
 * Available information will vary depending on the instrumentation 
 * at the caller and callee sites.
 */
public interface IBehaviorCallEvent extends IParentEvent, ICallerSideEvent
{
	/**
	 * The arguments passed to the behavior.
	 * <br/>
	 * This information is always available.
	 */
	public Object[] getArguments();
	
	/**
	 * The behavior that is actually executed. 
	 * It might be different than {@link #getCalledBehavior() },
	 * for instance if the caller calls an interface or overridden method.
	 * <br/>
	 * This information is always available.
	 */
	public IBehaviorInfo getExecutedBehavior();
	
	/**
	 * The called behavior.
	 * <br/>
	 * This information is available only if the caller behavior
	 * was instrumented.
	 */
	public IBehaviorInfo getCalledBehavior();
	
	/**
	 * The object on which the behavior was called, or
	 * null if static.
	 * <br/>
	 * This information is always available.
	 */
	public Object getTarget();
	
	/**
	 * The behavior that requested the call.
	 * <br/>
	 * This information is available only if the caller behavior
	 * was instrumented.
	 * @return Calling behavior, or null if not available
	 */
	public IBehaviorInfo getCallingBehavior();
	
	/**
	 * Returns the event that corresponds to the end of this behavior.
	 * This method can return null, for instance if the program
	 * terminates with {@link System#exit(int)}.
	 */
	public IBehaviorExitEvent getExitEvent();
	

	
}
