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
package tod.plugin.views;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventFilter;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ILocationInfo;
import tod.gui.MinerUI;
import tod.gui.seed.FilterSeed;
import tod.gui.seed.LogViewSeed;
import tod.gui.seed.LogViewSeedFactory;
import tod.plugin.DebuggingSession;
import tod.plugin.TODPluginUtils;
import tod.plugin.TODSessionManager;
import zz.utils.properties.IProperty;
import zz.utils.properties.PropertyListener;

public class EventViewer extends MinerUI
{
	private final TraceNavigatorView itsTraceNavigatorView;

	public EventViewer(TraceNavigatorView aTraceNavigatorView)
	{
		itsTraceNavigatorView = aTraceNavigatorView;
		TODSessionManager.getInstance().pCurrentSession().addHardListener(new PropertyListener<DebuggingSession>()
				{
					public void propertyChanged(IProperty<DebuggingSession> aProperty, DebuggingSession aOldValue, DebuggingSession aNewValue)
					{
						reset();
					}
				});
	}

	protected DebuggingSession getSession()
	{
		return TODSessionManager.getInstance().pCurrentSession().get();
	}
	
	public void showElement (IJavaElement aElement)
	{
		try
		{
			ILocationInfo theLocationInfo = TODPluginUtils.getLocationInfo(getSession(), aElement);
			LogViewSeed theSeed = LogViewSeedFactory.getDefaultSeed(this, getBrowser(), theLocationInfo);
			openSeed(theSeed, false);
		}
		catch (JavaModelException e)
		{
			throw new RuntimeException("Could not show element", e);
		}
	}
	
	public void gotoEvent(ILogEvent aEvent)
	{
	    itsTraceNavigatorView.gotoEvent(getSession(), aEvent);
	}
}
