/*
 * Created on Oct 26, 2005
 */
package tod.core.session;

import java.net.URI;

import javax.swing.JComponent;

import tod.core.database.browser.ILocationsRepository;
import tod.core.database.browser.ILogBrowser;

public interface ISession
{
	/**
	 * Returns a resource identifier for this session, that can be used to retrieve
	 * previous sessions.
	 * @see ISessionFactory#loadSession(URI);
	 */
	public URI getUri();
	public ILogBrowser getLogBrowser();
	
	/**
	 * Returns the path where the agent caches instrumented classes
	 */
	public String getCachedClassesPath();
	
	/**
	 * Disconnects this session from the target VM, if it is connected 
	 */
	public void disconnect();
	
	/**
	 * Creates a console that can be used to control this session.
	 */
	public JComponent createConsole();
}
