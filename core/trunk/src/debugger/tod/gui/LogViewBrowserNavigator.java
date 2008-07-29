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

import tod.gui.seed.LogViewSeed;
import tod.gui.view.LogView;
import zz.utils.ui.StackLayout;

/**
 * Browser navigator for log view seeds.
 * @author gpothier
 */
public class LogViewBrowserNavigator extends BrowserNavigator<LogViewSeed>
{
	private final IGUIManager itsGUIManager;
	private JPanel itsViewContainer;
	private LogView itsCurrentView;
	
	public LogViewBrowserNavigator(IGUIManager aGUIManager)
	{
		itsGUIManager = aGUIManager;
		itsViewContainer = new JPanel(new StackLayout());
	}

	public JPanel getViewContainer()
	{
		return itsViewContainer;
	}

	protected void setSeed (LogViewSeed aSeed)
	{
		LogView thePreviousView = itsCurrentView;
		
		try
		{
			if (itsCurrentView != null && (aSeed == null || ! itsCurrentView.getClass().equals(aSeed.getComponentClass())))
			{
				// Keep current view
				itsViewContainer.remove(itsCurrentView);
				itsCurrentView = null;
			}
			
			if (itsCurrentView == null && aSeed != null)
			{
				Class<? extends LogView> theClass = aSeed.getComponentClass();
				Constructor<? extends LogView> theConstructor = theClass.getConstructor(IGUIManager.class);
				itsCurrentView = theConstructor.newInstance(itsGUIManager);
				itsCurrentView.init();
				itsViewContainer.add(itsCurrentView);
			}
			
			if (itsCurrentView != null) itsCurrentView.setSeed(aSeed);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		if (itsCurrentView != thePreviousView) viewChanged(itsCurrentView);
		
		super.setSeed(aSeed);
		
		itsViewContainer.revalidate();
		itsViewContainer.repaint();
		itsViewContainer.validate();
	}
	
	/**
	 * Called when a new view is displayed. Does
	 * nothing by default.
	 */
	protected void viewChanged(LogView theView)
	{
	}

}
