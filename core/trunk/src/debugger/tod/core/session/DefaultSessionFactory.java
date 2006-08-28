/*
 * Created on Oct 26, 2005
 */
package tod.core.session;

import java.net.URI;

import tod.impl.bci.asm.ASMLocalSession;

public class DefaultSessionFactory implements ISessionFactory
{
	private static DefaultSessionFactory INSTANCE = new DefaultSessionFactory();

	public static DefaultSessionFactory getInstance()
	{
		return INSTANCE;
	}

	private DefaultSessionFactory()
	{
	}

	public ISession createSession(
			URI aLocation, 
			String aGlobalWorkingSet, 
			String aIdentificationWorkingSet,
			String aTraceWorkingSet)
	{
		return new ASMLocalSession(aLocation, aGlobalWorkingSet, aIdentificationWorkingSet, aTraceWorkingSet);
	}

	public ISession loadSession(URI aLocation)
	{
		throw new UnsupportedOperationException();
	}

}
