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
package tod.gui.view.highlighter;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.gui.IGUIManager;
import tod.gui.eventsequences.IEventSequenceSeed;
import tod.gui.eventsequences.IEventSequenceView;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

public class HighlighterSequenceSeed implements IEventSequenceSeed
{
	private String itsTitle;
	
	private IRWProperty<IEventBrowser> pBackgroundBrowser =
		new SimpleRWProperty<IEventBrowser>();
	
	private IRWProperty<IEventBrowser> pForegroundBrowser =
		new SimpleRWProperty<IEventBrowser>();

	public HighlighterSequenceSeed(
			String aTitle,
			IEventBrowser aBackgroundBrowser,
			IEventBrowser aForegroundBrowser)
	{
		itsTitle = aTitle;
		pBackgroundBrowser.set(aBackgroundBrowser);
		pForegroundBrowser.set(aForegroundBrowser);
	}

	public IEventSequenceView createView(IGUIManager aGUIManager)
	{
		return new HighlighterSequenceView(aGUIManager, this);
	}

	public IRWProperty<IEventBrowser> pBackgroundBrowser()
	{
		return pBackgroundBrowser;
	}

	public IRWProperty<IEventBrowser> pForegroundBrowser()
	{
		return pForegroundBrowser;
	}
	
	public String getTitle()
	{
		return itsTitle;
	}

	/**
	 * Sets the foreground browser to the intersection of the background
	 * browser and the specified filter.
	 */
	public void setFilter(IEventFilter aFilter)
	{
		IEventBrowser theEventBrowser = aFilter != null ? 
				pBackgroundBrowser().get().createIntersection(aFilter)
				: null;
				
		pForegroundBrowser().set(theEventBrowser);
	}
	
	
}
