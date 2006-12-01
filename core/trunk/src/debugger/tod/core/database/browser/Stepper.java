/*
 * Created on Oct 15, 2006
 */
package tod.core.database.browser;

import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IThreadInfo;

/**
 * Provides forward and backward stepping operations.
 * @author gpothier
 */
public class Stepper
{
	private ILogBrowser itsBrowser;
	private IThreadInfo itsThread;
	private ILogEvent itsCurrentEvent;

	public Stepper(ILogBrowser aBrowser, IThreadInfo aThread)
	{
		itsBrowser = aBrowser;
		itsThread = aThread;
	}

	public IThreadInfo getThread()
	{
		return itsThread;
	}

	public ILogEvent getCurrentEvent()
	{
		return itsCurrentEvent;
	}
	
	public void setCurrentEvent(ILogEvent aCurrentEvent)
	{
		itsCurrentEvent = aCurrentEvent;
	}
	
	private void forward(IEventBrowser aBrowser)
	{
		aBrowser.setPreviousEvent(itsCurrentEvent);
		do
		{
			itsCurrentEvent = aBrowser.next();		
		} while (itsCurrentEvent instanceof IBehaviorExitEvent);
	}
	
	private void backward(IEventBrowser aBrowser)
	{
		aBrowser.setNextEvent(itsCurrentEvent);
		do
		{
			itsCurrentEvent = aBrowser.previous();
		} while (itsCurrentEvent instanceof IBehaviorExitEvent);
	}

	public void forwardStepInto()
	{
		forward(itsBrowser.createBrowser(itsBrowser.createThreadFilter(itsThread)));
	}
	
	public void backwardStepInto()
	{
		backward(itsBrowser.createBrowser(itsBrowser.createThreadFilter(itsThread)));
	}
	
	public void forwardStepOver()
	{
		forward(itsBrowser.createBrowser(itsBrowser.createIntersectionFilter(
				itsBrowser.createThreadFilter(itsThread),
				itsBrowser.createDepthFilter(itsCurrentEvent.getDepth()))));
	}
	
	public void backwardStepOver()
	{
		backward(itsBrowser.createBrowser(itsBrowser.createIntersectionFilter(
				itsBrowser.createThreadFilter(itsThread),
				itsBrowser.createDepthFilter(itsCurrentEvent.getDepth()))));
	}
	
	public void stepOut()
	{
		itsCurrentEvent = itsCurrentEvent.getParent();
	}
}
