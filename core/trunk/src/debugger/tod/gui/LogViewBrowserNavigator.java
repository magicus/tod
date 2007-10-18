/*
TOD - Trace Oriented Debugger.
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
package tod.gui;

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
	private JPanel itsViewContainer;
	
	public LogViewBrowserNavigator()
	{
		itsViewContainer = new JPanel(new StackLayout());
	}

	public JPanel getViewContainer()
	{
		return itsViewContainer;
	}

	protected void setSeed (LogViewSeed aSeed)
	{
		if (getCurrentSeed() != null) 
		{
			try
			{
				LogView theComponent = getCurrentSeed().getComponent();
				if (theComponent != null) itsViewContainer.remove(theComponent);
				getCurrentSeed().deactivate();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		super.setSeed(aSeed);
		
		if (getCurrentSeed() != null) 
		{
			try
			{
				getCurrentSeed().activate();
				LogView theComponent = getCurrentSeed().getComponent();
				assert itsViewContainer.getComponentCount() == 0;
				itsViewContainer.add(theComponent);
				viewChanged(theComponent);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else viewChanged(null);
		
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
