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
package tod.gui.view.controlflow;

import java.awt.Color;

import javax.swing.JPanel;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IBehaviorCallEvent;
import tod.gui.Hyperlinks;
import tod.gui.JobProcessor;
import zz.utils.ui.ZLabel;
import zz.utils.ui.text.XFont;

public class CFlowViewUtils
{
	/**
	 * Adds the hyperlinks representing the behavior's arguments to the given container.
	 */
	public static void addArguments(
			ILogBrowser aLogBrowser,
			JobProcessor aJobProcessor,
			JPanel aContainer,
			IBehaviorCallEvent aRefEvent,
			Object[] aArguments, 
			XFont aFont,
			boolean aShowPackageNames)
	{
		aContainer.add(ZLabel.create("(", aFont, Color.BLACK));
		
		if (aArguments != null)
		{
			boolean theFirst = true;
			for (Object theArgument : aArguments)
			{
				if (theFirst) theFirst = false;
				else aContainer.add(ZLabel.create(", ", aFont, Color.BLACK));
				
				aContainer.add(Hyperlinks.object(
						Hyperlinks.SWING, 
						aLogBrowser,
						aJobProcessor,
						theArgument, 
						aRefEvent,
						aShowPackageNames));
			}
		}
		else
		{
			aContainer.add(ZLabel.create("...", aFont, Color.BLACK));
		}
		
		aContainer.add(ZLabel.create(")", aFont, Color.BLACK));
	}
	

}
