/*
TOD - Trace Oriented Debugger.
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

import tod.core.ILogCollector;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;

/**
 * Root of the interface graph of logging events.
 * @author gpothier
 */
public interface ILogEvent 
{
	/**
	 * Returns a pointer to this event.
	 * @see ILogBrowser#getEvent(ExternalPointer)
	 */
	public ExternalPointer getPointer();
	
	/**
	 * Identifies the host in which the event occurred.
	 */
	public IHostInfo getHost();
	
	/**
	 * Identifies the thread in which the event occured.
	 */
	public IThreadInfo getThread();
	
	/**
	 * Depth of this event in its control flow stack.
	 */
	public int getDepth();
	
	/**
	 * Timestamp of the event. Its absolute value has no
	 * meaning, but the difference between two timestamps
	 * is a duration in nanoseconds.
	 */
	public long getTimestamp();

	/**
	 * Returns a pointer to the parent event.
	 * Note that this method is more efficient than {@link #getParent()}.
	 */
	public ExternalPointer getParentPointer();
	
	/**
	 * Returns behavior call event corresponding to the behavior execution
	 * during which this event occurred.
	 * Note that calling this method might cause a database access, at
	 * least the first time it is called (implementations of this method 
	 * should cache the result).
	 * If only the identity of the parent event is needed, use 
	 * {@link #getParentPointer()} instead.
	 */
	public IBehaviorCallEvent getParent();
	
	/**
	 * Returns the stack of advices this event is in the cflow of.
	 * @return An array of advice source ids, or null if not in the cflow of any advice.
	 */
	public int[] getAdviceCFlow();
}
