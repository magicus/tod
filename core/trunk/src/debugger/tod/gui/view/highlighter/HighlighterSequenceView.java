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
package tod.gui.view.highlighter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.BrowserData;
import tod.gui.IGUIManager;
import tod.gui.eventsequences.AbstractSequenceView;
import zz.utils.list.IList;
import zz.utils.list.IListListener;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;

/**
 * A sequence view that displays a browser as background, which permits to
 * give a context to a foreground browser.
 * @author gpothier
 */
public class HighlighterSequenceView extends AbstractSequenceView
implements IPropertyListener, IListListener
{
	private HighlighterSequenceSeed itsSeed;
	
	public HighlighterSequenceView(IGUIManager aGUIManager, HighlighterSequenceSeed aSeed)
	{
		super(aGUIManager);
		itsSeed = aSeed;
	}

	
	public HighlighterSequenceSeed getSeed()
	{
		return itsSeed;
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		getSeed().pBackgroundBrowser.addHardListener(this);
		getSeed().pForegroundBrowsers.addHardListener(this);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		getSeed().pBackgroundBrowser.removeListener(this);
		getSeed().pForegroundBrowsers.removeListener(this);
	}

	@Override
	protected final List<BrowserData> getBrowsers()
	{
		List<BrowserData> theBrowsers = new ArrayList<BrowserData>();
		
		IEventBrowser theBackgroundBrowser = getSeed().pBackgroundBrowser.get();
		if(theBackgroundBrowser != null)
		{
			theBrowsers.add (new BrowserData(theBackgroundBrowser, Color.BLACK));
		}
		
		for (BrowserData theBrowserData : getSeed().pForegroundBrowsers)
		{
			theBrowsers.add (new BrowserData(
					theBackgroundBrowser.createIntersection(theBrowserData.browser.getFilter()), 
					theBrowserData.color,
					theBrowserData.markSize));
		}
		
		return theBrowsers;
	}


	public String getTitle()
	{
		return getSeed().getTitle();
	}

	@Override
	protected ILogEvent getEventAt(long aTimestamp, long aTolerance)
	{
		return getEventAt(itsSeed.pBackgroundBrowser.get().clone(), aTimestamp, aTolerance);
	}

	public void propertyChanged(IProperty aProperty, Object aOldValue, Object aNewValue)
	{
		update();
	}


	public void propertyValueChanged(IProperty aProperty)
	{
	}


	public void elementAdded(IList aList, int aIndex, Object aElement)
	{
		update();
	}


	public void elementRemoved(IList aList, int aIndex, Object aElement)
	{
		update();
	}

}
