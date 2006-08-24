/*
 * Created on Nov 4, 2005
 */
package tod.core.session;

import java.net.URI;

import tod.core.database.browser.ILocationsRepository;
import tod.core.database.browser.ILogBrowser;

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

	public ILogBrowser getLogBrowser()
	{
		return itsDelegate.getLogBrowser();
	}

	public ILocationsRepository getLocations()
	{
		return itsDelegate.getLocations();
	}

	public URI getUri()
	{
		return itsDelegate.getUri();
	}
}
