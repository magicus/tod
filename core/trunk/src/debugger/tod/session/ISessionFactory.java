/*
 * Created on Oct 26, 2005
 */
package tod.session;

import java.net.URI;

public interface ISessionFactory
{
	/**
	 * Loads an existing session.
	 */
	public ISession loadSession(URI aLocation);
	
	/**
	 * Creates a new session at the given location.
	 */
	public ISession createSession(
			URI aLocation,
			String aGlobalWorkingSet,
			String aIdentificationWorkingSet,
			String aTraceWorkingSet);
}
