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
package tod.gui.eventsequences;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import tod.core.DebugFlags;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.gui.IGUIManager;
import tod.gui.seed.CFlowSeed;
import tod.gui.seed.FilterSeed;
import zz.utils.ItemAction;
import zz.utils.Utils;

public class ThreadSequenceView extends AbstractSingleBrowserSequenceView
{
	public static final Color EVENT_COLOR = Color.GRAY;

	private final ThreadSequenceSeed itsSeed;
	
	public ThreadSequenceView(IGUIManager aGUIManager, ThreadSequenceSeed aSeed)
	{
		super(aGUIManager, EVENT_COLOR);
		itsSeed = aSeed;
		
		if (DebugFlags.SHOW_DEBUG_GUI)
		{
			addBaseAction(new ShowCFlowAction());
			addBaseAction(new ShowEventsAction());
		}
		
		setMuralCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	public ILogBrowser getLogBrowser()
	{
		return itsSeed.getLogBrowser();
	}
	
	public IThreadInfo getThread()
	{
		return itsSeed.getThread();
	}
	
	@Override
	protected void muralClicked()
	{
		showCFlow();
	}
	
	@Override
	protected IEventBrowser getBrowser()
	{
		IEventFilter theFilter = getLogBrowser().createThreadFilter(getThread());
		IEventBrowser theBrowser = getLogBrowser().createBrowser(theFilter);
		return theBrowser;
	}

	public String getTitle()
	{
		List<IHostInfo> theHosts = new ArrayList<IHostInfo>();
		Utils.fillCollection(theHosts, getLogBrowser().getHosts());

		if (theHosts.size() > 1)
		{
			return String.format(
					"Thread \"%s\" [id %d] on host \"%s\" [id %d]",
					getThread().getName(),
					getThread().getId(),
					getThread().getHost().getName(),
					getThread().getHost().getId());
		}
		else
		{
			return String.format(
					"Thread \"%s\" [id %d]",
					getThread().getName(),
					getThread().getId());
		}
	}
	
	protected void showCFlow()
	{
		CFlowSeed theSeed = new CFlowSeed(getGUIManager(), getLogBrowser(), getThread());
		getGUIManager().openSeed(theSeed, false);
	}
	
	private class ShowCFlowAction extends ItemAction
	{
		public ShowCFlowAction()
		{
			setTitle("view control flow");
			setDescription(
					"<html>" +
					"<b>Show control flow.</b> Shows the control flow of <br>" +
					"this thread.");
		}
		
		@Override
		public void actionPerformed(ActionEvent aE)
		{
			showCFlow();
		}
	}
	
	private class ShowEventsAction extends ItemAction
	{
		public ShowEventsAction()
		{
			setTitle("(all events)");
			setDescription(
					"<html>" +
					"<b>Show all events.</b> Show all the events of this <br>" +
					"thread in a list. This is used to debug TOD itself.");
		}

		@Override
		public void actionPerformed(ActionEvent aE)
		{
			IEventFilter theFilter = getLogBrowser().createThreadFilter(getThread());
			FilterSeed theSeed = new FilterSeed(
					getGUIManager(), 
					getLogBrowser(), 
					"All events of thread "+getThread().getName(),
					theFilter);
			getGUIManager().openSeed(theSeed, false);
		}
	}
}
