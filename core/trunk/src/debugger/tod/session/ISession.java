/*
 * Created on Oct 26, 2005
 */
package tod.session;

import java.net.URI;

import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.ILocationTrace;

public interface ISession
{
	/**
	 * Returns a resource identifier for this session, that can be used to retrieve
	 * previous sessions.
	 * @see ISessionFactory#loadSession(URI);
	 */
	public URI getUri();
	public IEventTrace getEventTrace();
	public ILocationTrace getLocationTrace();
	
	/**
	 * Returns the path where the agent caches instrumented classes
	 */
	public String getCachedClassesPath();
	
	/**
	 * Disconnects this session from the target VM, if it is connected 
	 */
	public void disconnect();
}
