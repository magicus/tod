/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import tod.core.model.structure.IThreadInfo;
import tod.core.model.trace.IEventTrace;
import tod.gui.view.LogView;
import zz.csg.api.IDisplay;

/**
 * Sequence seed for a particular field.
 * @author gpothier
 */
public class ThreadSequenceSeed implements IEventSequenceSeed
{
	private IEventTrace itsTrace;
	private IThreadInfo itsThread;

	public ThreadSequenceSeed(IEventTrace aTrace, IThreadInfo aThread)
	{
		itsTrace = aTrace;
		itsThread = aThread;
	}

	public IThreadInfo getThread()
	{
		return itsThread;
	}

	public IEventTrace getTrace()
	{
		return itsTrace;
	}

	public ThreadSequenceView createView(IDisplay aDisplay, LogView aLogView)
	{
		return new ThreadSequenceView(aDisplay, aLogView, this);
	}

}
