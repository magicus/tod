/*
 * Created on May 20, 2005
 */
package tod.impl.local;

import java.security.InvalidParameterException;
import java.util.NoSuchElementException;

import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventBrowser;

/**
 * base implementation of {@link tod.core.model.trace.IEventBrowser}
 * relying only on {@link tod.core.model.trace.IEventBrowser#getEventCount()}
 * and {@link tod.core.model.trace.IEventBrowser#getEvent(int)},
 * and timestamp related methods.
 * @author gpothier
 */
public abstract class AbstractEventBrowser implements IEventBrowser
{
	private int itsCursor = 0;

	public int getCursor()
	{
		return itsCursor;
	}

	public ILogEvent getNext()
	{
		if (! hasNext()) throw new NoSuchElementException();
		return getEvent(itsCursor++);
	}

	public ILogEvent getPrevious()
	{
		if (! hasPrevious()) throw new NoSuchElementException();
		return getEvent(--itsCursor);
	}

	public boolean hasNext()
	{
		return itsCursor < getEventCount();
	}

	public boolean hasPrevious()
	{
		return itsCursor > 0;
	}

	public void setCursor(int aPosition)
	{
		if (aPosition < 0 || aPosition > getEventCount()) throw new InvalidParameterException("Received "+aPosition);
		itsCursor = aPosition;
	}

}
