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
package tod.gui;

import tod.core.database.event.ILogEvent;
import tod.gui.kit.Bus;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * A context that cannot be modified.
 * @author gpothier
 */
public class FrozenContext implements IContext
{
	private Bus itsBus;
	private IGUIManager itsGUIManager;
	private IRWProperty<ILogEvent> pSelectedEvent;
	
	private FrozenContext(Bus aBus, IGUIManager aManager, IRWProperty<ILogEvent> aSelectedEvent)
	{
		itsBus = aBus;
		itsGUIManager = aManager;
		pSelectedEvent = aSelectedEvent;
	}

	public Bus getBus()
	{
		return itsBus;
	}

	public IGUIManager getGUIManager()
	{
		return itsGUIManager;
	}

	public IRWProperty<ILogEvent> pSelectedEvent()
	{
		return pSelectedEvent;
	}

	/**
	 * Creates a frozen context that is in the same state as the given context.
	 */
	public static FrozenContext create(IContext aSource)
	{
		IRWProperty<ILogEvent> theProperty= new SimpleRWProperty<ILogEvent>(null, aSource.pSelectedEvent().get())
		{
			@Override
			protected Object canChange(ILogEvent aOldValue, ILogEvent aNewValue)
			{
				// Note: that might change, but for now:
				// don't just reject, throw an exception so we know who tries to change the value.
				throw new RuntimeException("read-only");
			}
		};
			
		return new FrozenContext(aSource.getBus(), aSource.getGUIManager(), theProperty);
	}
}
