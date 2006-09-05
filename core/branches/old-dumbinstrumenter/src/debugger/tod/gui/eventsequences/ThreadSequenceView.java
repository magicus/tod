/*
 * Created on Feb 16, 2006
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
