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
package tod.gui.seed;

import java.awt.Color;

import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.FontConfig;
import tod.gui.IGUIManager;
import tod.gui.kit.html.HtmlDoc;
import tod.gui.view.FilterView;
import tod.gui.view.LogView;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * A seed that is based on a {@link tod.core.database.browser.IEventBrowser}.
 * Its view is simply a sequential view of filtered events.
 * @author gpothier
 */
public class FilterSeed extends LogViewSeed/*<FilterView>*/
{
	private final IEventFilter itsBaseFilter;
	private final HtmlDoc itsTitle;
	
	
	/**
	 * Timestamp of the first event displayed by this view.
	 */
	private long itsTimestamp;
	
	private IRWProperty<ILogEvent> pSelectedEvent = new SimpleRWProperty<ILogEvent>(this);
	
	private IRWProperty<IEventFilter> pAdditionalFilter = new SimpleRWProperty<IEventFilter>(this);

	
	public FilterSeed(
			IGUIManager aGUIManager, 
			ILogBrowser aLog, 
			String aTitle,
			IEventFilter aBaseFilter)
	{
		this(
				aGUIManager, 
				aLog,
				HtmlDoc.create("<b>"+aTitle+"</b>", FontConfig.BIG, Color.BLACK),
				aBaseFilter);
	}
	
	public FilterSeed(
			IGUIManager aGUIManager, 
			ILogBrowser aLog, 
			HtmlDoc aTitle,
			IEventFilter aBaseFilter)
	{
		super(aGUIManager, aLog);
		itsTitle = aTitle;
		itsBaseFilter = aBaseFilter;
	}
	
	protected LogView requestComponent()
	{
		FilterView theView = new FilterView (getGUIManager(), getLogBrowser(), this);
		theView.init();
		return theView;
	}
	
	public long getTimestamp()
	{
		return itsTimestamp;
	}
	
	public void setTimestamp(long aTimestamp)
	{
		itsTimestamp = aTimestamp;
	}
	
	public IEventFilter getBaseFilter()
	{
		return itsBaseFilter;
	}
	
	public HtmlDoc getTitle()
	{
		return itsTitle;
	}
	
	/**
	 * The currently selected event in the list.
	 */
	public IRWProperty<ILogEvent> pSelectedEvent()
	{
		return pSelectedEvent;
	}

}
