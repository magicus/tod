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
package tod.core.database.browser;

import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IThreadInfo;

/**
 * Provides forward and backward stepping operations.
 * @author gpothier
 */
public class Stepper
{
	private ILogBrowser itsBrowser;
	private ILogEvent itsCurrentEvent;

	public Stepper(ILogBrowser aBrowser)
	{
		itsBrowser = aBrowser;
	}

	public IThreadInfo getThread()
	{
		return getCurrentEvent().getThread();
	}

	public ILogEvent getCurrentEvent()
	{
		return itsCurrentEvent;
	}
	
	public void setCurrentEvent(ILogEvent aCurrentEvent)
	{
		itsCurrentEvent = aCurrentEvent;
	}
	
	private void forward(IEventBrowser aBrowser)
	{
		aBrowser.setPreviousEvent(itsCurrentEvent);
		do
		{
			itsCurrentEvent = aBrowser.hasNext() ? aBrowser.next() : null;		
		} while (itsCurrentEvent instanceof IBehaviorExitEvent);
	}
	
	private void backward(IEventBrowser aBrowser)
	{
		aBrowser.setNextEvent(itsCurrentEvent);
		do
		{
			itsCurrentEvent = aBrowser.hasPrevious() ? aBrowser.previous() : null;
		} while (itsCurrentEvent instanceof IBehaviorExitEvent);
	}

	public void forwardStepInto()
	{
		forward(itsBrowser.createBrowser(itsBrowser.createThreadFilter(getThread())));
	}
	
	public void backwardStepInto()
	{
		backward(itsBrowser.createBrowser(itsBrowser.createThreadFilter(getThread())));
	}
	
	public void forwardStepOver()
	{
		forward(itsBrowser.createBrowser(itsBrowser.createIntersectionFilter(
				itsBrowser.createThreadFilter(getThread()),
				itsBrowser.createDepthFilter(itsCurrentEvent.getDepth()))));
	}
	
	public void backwardStepOver()
	{
		backward(itsBrowser.createBrowser(itsBrowser.createIntersectionFilter(
				itsBrowser.createThreadFilter(getThread()),
				itsBrowser.createDepthFilter(itsCurrentEvent.getDepth()))));
	}
	
	public void stepOut()
	{
		itsCurrentEvent = itsCurrentEvent.getParent();
	}
}
