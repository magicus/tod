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
package tod.gui.seed;

import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.gui.IGUIManager;
import tod.gui.view.FilterView;
import tod.gui.view.LogView;

/**
 * A seed that is based on a {@link tod.core.database.browser.IEventBrowser}.
 * Its view is simply a sequential view of filtered events.
 * @author gpothier
 */
public class FilterSeed extends Seed/*<FilterView>*/
{
	private IEventFilter itsFilter;
	
	/**
	 * Timestamp of the first event displayed by this view.
	 */
	private long itsTimestamp;
	
	
	public FilterSeed(IGUIManager aGUIManager, ILogBrowser aLog, IEventFilter aFilter)
	{
		super(aGUIManager, aLog);
		itsFilter = aFilter;
	}
	
	protected LogView requestComponent()
	{
		FilterView theView = new FilterView (getGUIManager(), getEventTrace(), this);
		theView.init();
		return theView;
	}
	
	

	public long getTimestamp()
	{
		return itsTimestamp;
	}
	
	public void setTimestamp(long aTimestamp)
	{
		itsTimestamp = aTimestamp;
	}
	
	public IEventFilter getFilter()
	{
		return itsFilter;
	}
}
