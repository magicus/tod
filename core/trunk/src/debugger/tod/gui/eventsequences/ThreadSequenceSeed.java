/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import tod.core.model.browser.ILogBrowser;
import tod.core.model.structure.IThreadInfo;
import tod.gui.view.LogView;
import zz.csg.api.IDisplay;

/**
 * Sequence seed for a particular field.
 * @author gpothier
 */
public class ThreadSequenceSeed implements IEventSequenceSeed
{
	private ILogBrowser itsTrace;
	private IThreadInfo itsThread;

	public ThreadSequenceSeed(ILogBrowser aTrace, IThreadInfo aThread)
	{
		itsTrace = aTrace;
		itsThread = aThread;
	}

	public IThreadInfo getThread()
	{
		return itsThread;
	}

	public ILogBrowser getTrace()
	{
		return itsTrace;
	}

	public ThreadSequenceView createView(IDisplay aDisplay, LogView aLogView)
	{
		return new ThreadSequenceView(aDisplay, aLogView, this);
	}

}
