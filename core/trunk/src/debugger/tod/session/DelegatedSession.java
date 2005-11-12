/*
 * Created on Nov 4, 2005
 */
package tod.session;

import java.net.URI;

import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.ILocationTrace;

/**
 * A session that delegates to another session.
 * @author gpothier
 */
public abstract class DelegatedSession implements ISession
{
	private ISession itsDelegate;

	public DelegatedSession(ISession aDelegate)
	{
		itsDelegate = aDelegate;
	}

	public void disconnect()
	{
		itsDelegate.disconnect();
	}

	public String getCachedClassesPath()
	{
		return itsDelegate.getCachedClassesPath();
	}

	public IEventTrace getEventTrace()
	{
		return itsDelegate.getEventTrace();
	}

	public ILocationTrace getLocationTrace()
	{
		return itsDelegate.getLocationTrace();
	}

	public URI getUri()
	{
		return itsDelegate.getUri();
	}
}
