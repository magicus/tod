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
package tod.gui.controlflow.watch;

import static tod.gui.FontConfig.STD_FONT;

import javax.swing.JPanel;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IWriteEvent;
import tod.gui.GUIUtils;
import tod.gui.Hyperlinks;
import tod.gui.JobProcessor;
import tod.gui.kit.AsyncPanel;

/**
 * Represents a watch entry (field or variable).
 * @author gpothier
 */
public class WatchEntryNode<E> extends JPanel
{
	private final JobProcessor itsJobProcessor;
	private final ILogBrowser itsLogBrowser;
	private final IWatchProvider<E> itsProvider;
	private final E itsEntry;
	
	private Object[] itsValue;
	private IWriteEvent[] itsSetter;
	
	public WatchEntryNode(
			ILogBrowser aLogBrowser,
			JobProcessor aJobProcessor,
			IWatchProvider<E> aProvider, 
			E aEntry)
	{
		super(GUIUtils.createSequenceLayout());
		setOpaque(false);
		itsJobProcessor = aJobProcessor;
		itsLogBrowser = aLogBrowser;
		itsProvider = aProvider;
		itsEntry = aEntry;
		createUI();
	}
	
	private void createUI()
	{
		String theName = itsProvider.getEntryName(itsEntry);
		add(GUIUtils.createLabel(theName + " = "));
		add(new AsyncPanel(itsJobProcessor)
		{
			@Override
			protected void runJob()
			{
				itsSetter = itsProvider.getEntrySetter(itsEntry);
				if (itsSetter == null)
				{
					itsValue = itsProvider.getEntryValue(itsEntry);
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
								itsLogBrowser, 
								itsJobProcessor,
								itsProvider.getCurrentObject(),
								theValue,
								itsProvider.getRefEvent(),
								STD_FONT));
						
						if (theSetter != null)
						{
							add(GUIUtils.createLabel(" ("));
							add(Hyperlinks.event("why?", theSetter, STD_FONT));
							add(GUIUtils.createLabel(")"));
						}
					}
				}
			}
			
		});
	}
	
	
}
