/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
import tod.gui.view.LogView;
import zz.csg.api.IDisplay;
import zz.utils.ItemAction;

public class ThreadSequenceView extends AbstractSingleBrowserSequenceView
{
	public static final Color EVENT_COLOR = Color.GRAY;

	private final ThreadSequenceSeed itsSeed;
	
	public ThreadSequenceView(IDisplay aDisplay, LogView aLogView, ThreadSequenceSeed aSeed)
	{
		super(aDisplay, aLogView, EVENT_COLOR);
		itsSeed = aSeed;
		
		addBaseAction(new ShowThreadAction());
	}

	public ILogBrowser getTrace()
	{
		return itsSeed.getTrace();
	}
	
	public IThreadInfo getThread()
	{
		return itsSeed.getThread();
	}
	
	@Override
	protected IEventBrowser getBrowser()
	{
		IEventFilter theFilter = getTrace().createThreadFilter(getThread());
		IEventBrowser theBrowser = getTrace().createBrowser(theFilter);
		return theBrowser;
	}

	public String getTitle()
	{
		return "Thread view - \""+getThread().getName() + "\"";
	}
	
	private class ShowThreadAction extends ItemAction
	{
		public ShowThreadAction()
		{
			setTitle("view");
		}

		@Override
		public void actionPerformed(ActionEvent aE)
		{
			getGUIManager().openSeed(new CFlowSeed(getGUIManager(), getTrace(), getThread()), false);
		}
	}

}
