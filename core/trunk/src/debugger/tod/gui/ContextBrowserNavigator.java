/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui;

import java.lang.reflect.Constructor;

import javax.swing.JPanel;

import tod.core.database.event.ILogEvent;
import tod.gui.activities.ActivityPanel;
import tod.gui.activities.ActivitySeed;
import tod.gui.activities.IEventSeed;
import zz.utils.properties.PropertyUtils;
import zz.utils.properties.PropertyUtils.Connector;
import zz.utils.ui.StackLayout;

/**
 * Browser navigator for a given context.
 * @author gpothier
 */
public class ContextBrowserNavigator extends BrowserNavigator<ActivitySeed>
{
	private final IContext itsContext;
	private JPanel itsContainer;
	private ActivityPanel itsCurrentActivityPanel;
	private Connector<ILogEvent> itsSelectedEventConnector;
	
	public ContextBrowserNavigator(IContext aContext)
	{
		super(aContext.getGUIManager().getJobScheduler());
		itsContext = aContext;
		itsContainer = new JPanel(new StackLayout());
	}

	public JPanel getActivityContainer()
	{
		return itsContainer;
	}

	@Override
	protected void setSeed (ActivitySeed aSeed)
	{
		ActivityPanel thePreviousPanel = itsCurrentActivityPanel;
		
		try
		{
			if (itsCurrentActivityPanel != null 
					&& (aSeed == null 
							|| ! itsCurrentActivityPanel.getClass().equals(aSeed.getComponentClass())))
			{
				// Drop current view
				itsContainer.remove(itsCurrentActivityPanel);
				itsCurrentActivityPanel = null;
			}
			
			if (itsCurrentActivityPanel == null && aSeed != null)
			{
				Class<? extends ActivityPanel> theClass = aSeed.getComponentClass();
				Constructor<? extends ActivityPanel> theConstructor = theClass.getConstructor(IContext.class);
				itsCurrentActivityPanel = theConstructor.newInstance(itsContext);
				itsCurrentActivityPanel.init();
				itsContainer.add(itsCurrentActivityPanel);
			}

			if (itsSelectedEventConnector != null)
			{
				itsSelectedEventConnector.disconnect();
				itsSelectedEventConnector = null;
			}

			if (aSeed instanceof IEventSeed)
			{
				IEventSeed theEventSeed = (IEventSeed) aSeed;
				itsSelectedEventConnector = PropertyUtils.connect(
						theEventSeed.pEvent(), 
						itsContext.pSelectedEvent(), 
						true);
			}
			else
			{
				itsContext.pSelectedEvent().set(null);
			}

			
			if (itsCurrentActivityPanel != null) itsCurrentActivityPanel.setSeed(aSeed);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		if (itsCurrentActivityPanel != thePreviousPanel) viewChanged(itsCurrentActivityPanel);
		
		super.setSeed(aSeed);
		
		itsContainer.revalidate();
		itsContainer.repaint();
//		itsContainer.validate();
	}
	
	/**
	 * Called when a new view is displayed. Does
	 * nothing by default.
	 */
	protected void viewChanged(ActivityPanel theView)
	{
	}

}
