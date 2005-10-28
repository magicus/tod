/*
 * Created on Aug 16, 2005
 */
package tod.plugin;

import tod.session.DefaultSessionFactory;
import tod.session.ISession;
import zz.utils.notification.IEvent;
import zz.utils.notification.SimpleEvent;
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
	
	private IRWProperty<ISession> pCurrentSession = new SimpleRWProperty<ISession>(this)
	{
		@Override
		protected void changed(ISession aOldValue, ISession aNewValue)
		{
			if (aOldValue != null) aOldValue.disconnect();
		}
	};
	
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
	public IProperty<ISession> pCurrentSession()
	{
		return pCurrentSession;
	}

	/**
	 * Obtains a free, clean collector session.
	 */
	public ISession createSession()
	{
		ISession theSession = DefaultSessionFactory.getInstance().createSession(null, null, null, null);
		pCurrentSession.set(theSession);
		return theSession;
	}
	
}
