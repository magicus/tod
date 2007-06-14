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

import java.util.Set;

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
import zz.utils.Utils;
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
	
	private IRWProperty<DebuggingSession> pCurrentSession = new SimpleRWProperty<DebuggingSession>(this)
	{
		@Override
		protected void changed(DebuggingSession aOldValue, DebuggingSession aNewValue)
		{
			if (aOldValue != null) 
			{
				try
				{
					aOldValue.disconnect();
				}
				catch (RuntimeException e)
				{
					System.err.println("[TODSessionManager] Error while disconnecting:");
					e.printStackTrace();
				}
			}
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
	public IProperty<DebuggingSession> pCurrentSession()
	{
		return pCurrentSession;
	}
	
	/**
	 * Drops the current session.
	 */
	public void killSession()
	{
		pCurrentSession.set(null);
	}
	
	/**
	 * Returns a session suitable for a new launch, or null
	 * if launch should be cancelled.
	 */
	public DebuggingSession getSession(
			ILaunch aLaunch,
			SourceRevealer aSourceRevealer,
			TODConfig aConfig)
	{
		DebuggingSession theCurrentSession = pCurrentSession.get();
		if (theCurrentSession != null && ! theCurrentSession.isAlive()) theCurrentSession = null;
		Class theSessionClass = SessionUtils.getSessionClass(aConfig);
		
		String theHostName = aConfig.get(TODConfig.CLIENT_HOST_NAME);
		if (theCurrentSession != null)
		{
			assert theCurrentSession.getLogBrowser() != null;
			try
			{
				Class theCurrentSessionClass = theCurrentSession.getDelegate().getClass();
				TODConfig theCurrentConfig = theCurrentSession.getConfig();
				if (! compatible(theCurrentConfig, aConfig))
				{
					if (! msgIncompatibleType()) return null;
					theCurrentSession.getLogBrowser().clear();
				}
				else if (! theCurrentSessionClass.equals(theSessionClass))
				{
					if (! msgIncompatibleType()) return null;
					theCurrentSession.getLogBrowser().clear();
				}
				else if (theCurrentSession.getLogBrowser().getHost(theHostName) != null)
				{
					if (! msgHasHost(theHostName)) return null;
					theCurrentSession.getLogBrowser().clear();
				}
				else
				{
					// We can use the same session.
					return theCurrentSession;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				if (! msgError(e)) return null;
			}
		}
		
		return createSession(aLaunch, aSourceRevealer, aConfig);
	}

	
	private boolean msgIncompatibleType()
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
		
		return theResult[0];
	}
	
	private boolean msgHasHost(final String aHostName)
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
						"'"+aHostName+"'. " +
						"Launch anyway, resetting the current session?");
			}
		});
		return theResult[0];
	}
	
	private boolean msgError(final Throwable aThrowable)
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
						"current session. The current session will be " +
						"dropped",
						new Status(
								IStatus.ERROR,
								"tod.plugin",
								0,
								"Exception",
								aThrowable));
			}
		});
		
		return theResult[0] != Dialog.CANCEL;
	}
	
	/**
	 * Obtains a free, clean collector session.
	 */
	private DebuggingSession createSession(
			ILaunch aLaunch,
			SourceRevealer aSourceRevealer, 
			TODConfig aConfig)
	{
		try
		{
			DebuggingSession theDebuggingSession = new DebuggingSession(
					SessionUtils.createSession(aConfig),
					aLaunch,
					aSourceRevealer);
			
			pCurrentSession.set(theDebuggingSession);
			return theDebuggingSession;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
	}
	
	
	private static final Set<TODConfig.Item> MODIFIABLE_ITEMS = (Set) Utils.createSet(
			TODConfig.AGENT_VERBOSE,
			TODConfig.INDEX_STRINGS,
			TODConfig.CLIENT_HOST_NAME
	);
	
	/**
	 * Tests if two configurations are compatible.
	 * @param aCurrentConfig The current config of a session.
	 * @param aNewConfig The new config that should be used
	 * @return True iff the current session can be reused.
	 */
	private boolean compatible(TODConfig aCurrentConfig, TODConfig aNewConfig)
	{
		for (TODConfig.Item theItem : TODConfig.ITEMS)
		{
			if (MODIFIABLE_ITEMS.contains(theItem)) continue;
			if (!Utils.equalOrBothNull(
					aCurrentConfig.get(theItem), 
					aNewConfig.get(theItem))) return false;
		}
		return true;
	}
	
}
