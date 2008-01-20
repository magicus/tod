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
package tod.gui.view.controlflow.watch;

import javax.swing.JPanel;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IWriteEvent;
import tod.gui.GUIUtils;
import tod.gui.Hyperlinks;
import tod.gui.JobProcessor;
import tod.gui.kit.AsyncPanel;
import tod.gui.view.controlflow.watch.AbstractWatchProvider.Entry;

/**
 * Represents a watch entry (field or variable).
 * @author gpothier
 */
public class WatchEntryNode extends JPanel
{
	private final JobProcessor itsJobProcessor;
	private final ILogBrowser itsLogBrowser;
	private final WatchPanel itsWatchPanel;
	private final AbstractWatchProvider itsProvider;
	private final Entry itsEntry;
	
	private Object[] itsValue;
	private IWriteEvent[] itsSetter;
	
	public WatchEntryNode(
			ILogBrowser aLogBrowser,
			JobProcessor aJobProcessor,
			WatchPanel aWatchPanel,
			AbstractWatchProvider aProvider, 
			Entry aEntry)
	{
		super(GUIUtils.createSequenceLayout());
		itsWatchPanel = aWatchPanel;
		setOpaque(false);
		itsJobProcessor = aJobProcessor;
		itsLogBrowser = aLogBrowser;
		itsProvider = aProvider;
		itsEntry = aEntry;
		createUI();
	}
	
	protected boolean showPackageNames()
	{
		return itsWatchPanel.showPackageNames();
	}

	private void createUI()
	{
		String theName = itsEntry.getName();
		add(GUIUtils.createLabel(theName + " = "));
		add(new AsyncPanel(itsJobProcessor)
		{
			@Override
			protected void runJob()
			{
				itsSetter = itsEntry.getSetter();
				if (itsSetter == null)
				{
					itsValue = itsEntry.getValue();
				}
				else
				{
					itsValue = new Object[itsSetter.length];
					for (int i=0;i<itsSetter.length;i++)
					{
						itsValue[i] = itsSetter[i].getValue();
					}
				}
			}

			@Override
			protected void update()
			{
				if (itsValue != null)
				{
					boolean theFirst = true;
					for (int i=0;i<itsValue.length;i++)
					{
						Object theValue = itsValue[i];
						IWriteEvent theSetter = itsSetter != null ? itsSetter[i] : null;
			
						if (theFirst) theFirst = false;
						else add(GUIUtils.createLabel(" / "));
						
						add(Hyperlinks.object(
								Hyperlinks.SWING, 
								itsLogBrowser, 
								itsJobProcessor,
								itsProvider.getCurrentObject(),
								theValue,
								itsProvider.getRefEvent(),
								showPackageNames()));
						
						if (theSetter != null)
						{
							add(GUIUtils.createLabel(" ("));
							add(Hyperlinks.event(Hyperlinks.SWING, "why?", theSetter));
							add(GUIUtils.createLabel(")"));
						}
					}
				}
			}
		});
	}
	
	
}
