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

import java.awt.Color;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IWriteEvent;
import tod.gui.Hyperlinks;
import tod.gui.JobProcessor;
import tod.gui.Hyperlinks.ISeedFactory;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;

/**
 * Represents a watch entry (field or variable).
 * @author gpothier
 */
public class WatchEntryNode<E> extends SVGGraphicContainer
{
	private final JobProcessor itsJobProcessor;
	private final ISeedFactory itsSeedFactory;
	private final ILogBrowser itsLogBrowser;
	private final IWatchProvider<E> itsProvider;
	private final E itsEntry;
	
	private Object[] itsValue;
	private IWriteEvent[] itsSetter;

	public WatchEntryNode(
			ISeedFactory aSeedFactory,
			ILogBrowser aLogBrowser,
			JobProcessor aJobProcessor,
			IWatchProvider<E> aProvider, 
			E aEntry)
	{
		itsJobProcessor = aJobProcessor;
		itsSeedFactory = aSeedFactory;
		itsLogBrowser = aLogBrowser;
		itsProvider = aProvider;
		itsEntry = aEntry;
		createUI();
		
		aJobProcessor.submit(
				new JobProcessor.Job()
				{
					@Override
					public Object run()
					{
						itsSetter = itsProvider.getEntrySetter(itsEntry);
						itsValue = itsProvider.getEntryValue(itsEntry);
						updateUI();
						return null;
					}
				});
	}
	
	private void createUI()
	{
		disableUpdate();
		
		String theName = itsProvider.getEntryName(itsEntry);
		pChildren().add(SVGFlowText.create(theName + " = ", STD_FONT, Color.BLACK));
		
		setLayoutManager(new SequenceLayout());

		enableUpdate();
	}
	
	private void updateUI()
	{
		if (itsValue != null)
		{
			disableUpdate();
			
			boolean theFirst = true;
			for (int i=0;i<itsValue.length;i++)
			{
				Object theValue = itsValue[i];
				IWriteEvent theSetter = itsSetter != null ? itsSetter[i] : null;
	
				if (theFirst) theFirst = false;
				else pChildren().add(SVGFlowText.create(" / ", STD_FONT, Color.BLACK));
				
				pChildren().add(Hyperlinks.object(
						itsSeedFactory,
						itsLogBrowser, 
						itsJobProcessor,
						itsProvider.getCurrentObject(),
						theValue,
						STD_FONT));
				
				if (theSetter != null)
				{
					pChildren().add(SVGFlowText.create(" (", STD_FONT, Color.BLACK));
					
					pChildren().add(Hyperlinks.event(
							itsSeedFactory,
							"why?", 
							theSetter, 
							STD_FONT));
					
					pChildren().add(SVGFlowText.create(")", STD_FONT, Color.BLACK));
				}
			}
			
			enableUpdate();
		}		
	}
	
	
}
