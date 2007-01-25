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

import java.util.List;

import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILogEvent;

import zz.csg.api.IRectangularGraphicObject;

/**
 * Provider of watch data.
 * @author gpothier
 */
public interface IWatchProvider
{
	/**
	 * Builds the title of the watch window.
	 */
	public IRectangularGraphicObject buildTitle();
	
	/**
	 * Returns a current object. Currently this is only for
	 * stack frame reconstitution, represents the "this" variable.
	 */
	public WatchEntry getCurrentObject();
	
	/**
	 * Returns the set of watch entries.
	 */
	public List<WatchEntry> getEntries();
	
	public static class WatchEntry
	{
		public final String name;
		
		/**
		 * The array of possible values
		 */
		public final Object[] values;
		
		/**
		 * The array of possible setters corresponding to the values
		 */
		public final ILogEvent[] setters;
		
		public WatchEntry(String aName, Object aValue)
		{
			this(aName, aValue, null);
		}
		
		public WatchEntry(String aName, Object aValue, ILogEvent aSetter)
		{
			this(aName, new Object[] {aValue}, new ILogEvent[] {aSetter});
		}
		
		public WatchEntry(String aName, Object[] aValues, ILogEvent[] aSetters)
		{
			name = aName;
			values = aValues;
			setters = aSetters;
		}

	}
}
