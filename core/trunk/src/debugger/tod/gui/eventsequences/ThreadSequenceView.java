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
package tod.gui.eventsequences;

import java.awt.Color;
import java.awt.event.ActionEvent;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IThreadInfo;
import tod.gui.seed.CFlowSeed;
import tod.gui.seed.FilterSeed;
import tod.gui.view.LogView;
import zz.utils.ItemAction;

public class ThreadSequenceView extends AbstractSingleBrowserSequenceView
{
	public static final Color EVENT_COLOR = Color.GRAY;

	private final ThreadSequenceSeed itsSeed;
	
	public ThreadSequenceView(LogView aLogView, ThreadSequenceSeed aSeed)
	{
		super(aLogView, EVENT_COLOR);
		itsSeed = aSeed;
		
		addBaseAction(new ShowCFlowAction());
		addBaseAction(new ShowEventsAction());
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
	protected IEventBrowser getBrowser()
	{
		IEventFilter theFilter = getLogBrowser().createThreadFilter(getThread());
		IEventBrowser theBrowser = getLogBrowser().createBrowser(theFilter);
		return theBrowser;
	}

	public String getTitle()
	{
		return String.format(
				"Thread view - %s [%d]/%s [%d]",
				getThread().getName(),
				getThread().getId(),
				getThread().getHost().getName(),
				getThread().getHost().getId());
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
			CFlowSeed theSeed = new CFlowSeed(getGUIManager(), getLogBrowser(), getThread());
			getGUIManager().openSeed(theSeed, false);
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
