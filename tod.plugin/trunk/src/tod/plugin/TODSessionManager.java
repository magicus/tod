/*
 * Created on Aug 16, 2005
 */
package tod.plugin;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jdt.core.IJavaProject;

import tod.core.config.TODConfig;
import tod.core.session.ISession;
import tod.impl.local.LocalSession;
import zz.utils.properties.IProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;


/**
 * Manages a pool of debugging sessions.
 * @author gpothier
 */
public class TODSessionManager
{
	private static TODSessionManager INSTANCE = new TODSessionManager();
	
	private IRWProperty<DebuggingSession> pCurrentSession = new SimpleRWProperty<DebuggingSession>(this);
	
	public static TODSessionManager getInstance()
	{
		return INSTANCE;
	}
	
	private TODSessionManager()
	{
	}
	
	/**
	 * This propety contains the curent TOD session.
	 */
	public IProperty<DebuggingSession> pCurrentSession()
	{
		return pCurrentSession;
	}

	/**
	 * Obtains a free, clean collector session.
	 */
	public DebuggingSession createSession(IJavaProject aJavaProject)
	{
		ISession theSession;
		try
		{
			DebuggingSession thePreviousSession = pCurrentSession.get();
			if (thePreviousSession != null) thePreviousSession.disconnect();
			
			TODConfig theConfig = new TODConfig();
			theSession = new LocalSession(theConfig, new URI("file:/home/gpothier/tmp/ASM"));
			DebuggingSession theDebuggingSession = new DebuggingSession(theSession, aJavaProject);
			
			pCurrentSession.set(theDebuggingSession);
			return theDebuggingSession;
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
		
	}
	
}
