/*
 * Created on Nov 4, 2005
 */
package tod.plugin;

import org.eclipse.jdt.core.IJavaProject;

import tod.core.session.DelegatedSession;
import tod.core.session.ISession;

public class DebuggingSession extends DelegatedSession
{
	private IJavaProject itsJavaProject;

	public DebuggingSession(ISession aDelegate, IJavaProject aJavaProject)
	{
		super(aDelegate);
		itsJavaProject = aJavaProject;
	}

	public IJavaProject getJavaProject()
	{
		return itsJavaProject;
	}
}
