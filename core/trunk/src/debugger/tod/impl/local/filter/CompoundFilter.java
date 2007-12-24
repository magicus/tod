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
package tod.impl.local.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventFilter;
import tod.impl.local.LocalBrowser;

/**
 * Base class for filters that are compound of other filters.
 * @author gpothier
 */
public abstract class CompoundFilter extends AbstractStatelessFilter implements ICompoundFilter
{
	private List<IEventFilter> itsFilters;
	
	public CompoundFilter(LocalBrowser aBrowser)
	{
		this (aBrowser, new ArrayList<IEventFilter>());
	}
	
	public CompoundFilter(LocalBrowser aBrowser, List<IEventFilter> aFilters)
	{
		super (aBrowser);
		itsFilters = aFilters;
	}
	
	public CompoundFilter(LocalBrowser aBrowser, IEventFilter... aFilters)
	{
		super (aBrowser);
		itsFilters = new ArrayList<IEventFilter>(Arrays.asList(aFilters));
	}
	
	public List<IEventFilter> getFilters()
	{
		return itsFilters;
	}
	
	public void add (IEventFilter aFilter)
	{
		itsFilters.add(aFilter);
	}
	
	public void remove (IEventFilter aFilter)
	{
		itsFilters.remove(aFilter);
	}
	
}
