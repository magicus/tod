/*
 * Created on Oct 26, 2005
 */
package tod.core.session;

import java.net.URI;

public abstract class AbstractSession implements ISession
{
	private final URI itsUri;

	public AbstractSession(URI aUri)
	{
		itsUri = aUri;
	}

	public URI getUri()
	{
		return itsUri;
	} 
}
