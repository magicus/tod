/*
TOD plugin - Eclipse pluging for TOD
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.plugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import tod.core.config.TODConfig;
import tod.core.session.ISession;
import tod.core.session.SessionUtils;
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
	 * Returns a session suitable for a new launch.
	 */
	public DebuggingSession getSession(
			ILaunch aLaunch,
			IJavaProject aJavaProject,
			TODConfig aConfig)
	{
		DebuggingSession theCurrentSession = pCurrentSession.get();
		Class theSessionClass = SessionUtils.getSessionClass(aConfig);
		
		final String theHostName = aConfig.get(TODConfig.CLIENT_HOST_NAME);
		if (theCurrentSession != null)
		{
			try
			{
				Class theCurrentSessionClass = theCurrentSession.getDelegate().getClass();
				if (! theCurrentSessionClass.equals(theSessionClass))
				{
					final boolean[] theResult = new boolean[1];
					Display.getDefault().syncExec(new Runnable()
					{
						public void run()
						{
							Shell theShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
							theResult[0] = MessageDialog.openQuestion(
									theShell, 
									"Cannot reuse current session", 
									"The current debugging session cannot be reused " +
									"because the newly requested session is of another " +
									"type. " +
									"Launch anyway, resetting the current session?");
						}
					});
					
					if (! theResult[0]) return null;
					theCurrentSession.getLogBrowser().clear();
				}
				else if (theCurrentSession.getLogBrowser().getHost(theHostName) != null)
				{
					final boolean[] theResult = new boolean[1];
					Display.getDefault().syncExec(new Runnable()
					{
						public void run()
						{
							Shell theShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
							theResult[0] = MessageDialog.openQuestion(
									theShell, 
									"Cannot reuse current session", 
									"The current debugging session cannot be reused " +
									"because it already contains a trace of host " +
									"'"+theHostName+"'. " +
									"Launch anyway, resetting the current session?");
						}
					});

					if (! theResult[0]) return null;
					theCurrentSession.getLogBrowser().clear();
				}
				else
				{
					// We can use the same session.
					theCurrentSession.setConfig(aConfig);
					return theCurrentSession;
				}
			}
			catch (final Exception e)
			{
				final int[] theResult = new int[1];
				Display.getDefault().syncExec(new Runnable()
				{
					public void run()
					{
						Shell theShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
						theResult[0] = ErrorDialog.openError(
								theShell, 
								"Cannot reuse current session", 
								"An error occurred while trying to reuse the " +
								"current session. Drop the current session and " +
								"create a new one?",
								new Status(
										IStatus.ERROR,
										"tod.plugin",
										0,
										"Exception",
										e));
					}
				});

				if (theResult[0] == Dialog.CANCEL) return null;
			}
		}
		
		return createSession(aLaunch, aJavaProject, aConfig);
	}

	/**
	 * Obtains a free, clean collector session.
	 */
	private DebuggingSession createSession(
			ILaunch aLaunch,
			IJavaProject aJavaProject, 
			TODConfig aConfig)
	{
		try
		{
			DebuggingSession thePreviousSession = pCurrentSession.get();
			if (thePreviousSession != null) thePreviousSession.disconnect();

			DebuggingSession theDebuggingSession = new DebuggingSession(
					SessionUtils.createSession(aConfig),
					aLaunch,
					aJavaProject);
			
			pCurrentSession.set(theDebuggingSession);
			return theDebuggingSession;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
	}
	
}
