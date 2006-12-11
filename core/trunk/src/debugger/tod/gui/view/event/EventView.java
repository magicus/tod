/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
package tod.gui.view.event;

import javax.swing.JLabel;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IThreadInfo;
import tod.gui.IGUIManager;
import tod.gui.formatter.EventFormatter;
import tod.gui.kit.LinkLabel;
import tod.gui.kit.SeedLinkLabel;
import tod.gui.seed.CFlowSeed;
import tod.gui.seed.FilterSeed;
import tod.gui.view.LogView;
import zz.utils.ui.GridStackLayout;

/**
 * Base class for event viewers. It sets a framework for the UI:
 * subclasses should override the {@link #init()} method
 * and create their UI here after calling super.
 * They should add the components to the panel with no
 * layout constraints; they will be stacked vertically.
 * @author gpothier
 */
public abstract class EventView extends LogView
{
	public EventView(IGUIManager aManager, ILogBrowser aLog)
	{
		super (aManager, aLog);
	}
	
	public void init()
	{
		setLayout(new GridStackLayout(1, 0, 5, false, false));
		add (createTitleLabel(EventFormatter.getInstance().getHtmlText(getEvent())));
		
		ILogEvent theEvent = getEvent();
		IThreadInfo theThreadInfo = theEvent.getThread();
		
		// Thread & timestamp
		add (createTitledLink(
				"Thread: ", 
				"\""+theThreadInfo.getName()+"\" ["+theThreadInfo.getId()+"]", 
				new FilterSeed (getGUIManager(), getLogBrowser(), getLogBrowser().createThreadFilter(theThreadInfo))));

		add (createTitledPanel(
				"Timestamp: ", 
				new JLabel (""+theEvent.getTimestamp())));

		
		// CFLow
		LinkLabel theCFlowLabel = new SeedLinkLabel(
				getGUIManager(), 
				"View control flow", 
				new CFlowSeed(getGUIManager(), getLogBrowser(), theEvent));
		add (theCFlowLabel);
		
	}

	/**
	 * Returns the event represented by this view.
	 */
	protected abstract ILogEvent getEvent ();
	
}
