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

import java.util.List;

import javax.swing.JComponent;

import tod.core.database.event.ILogEvent;
import tod.core.database.event.IWriteEvent;
import tod.core.database.structure.ObjectId;
import tod.gui.JobProcessor;

/**
 * Provider of watch data.
 * @author gpothier
 */
public abstract class AbstractWatchProvider
{
	private String itsTitle;
	
	public AbstractWatchProvider(String aTitle)
	{
		itsTitle = aTitle;
	}

	/**
	 * Builds the title of the watch window.
	 * @param aJobProcessor A job processor that can be used if elements
	 * of the title are to be created asynchronously.
	 */
	public abstract JComponent buildTitleComponent(JobProcessor aJobProcessor);
	
	/**
	 * Returns a title for this watch provider.
	 */
	public String getTitle()
	{
		return itsTitle;
	}
	
	/**
	 * Returns a current object. Currently this is only for
	 * stack frame reconstitution, represents the "this" variable.
	 */
	public abstract ObjectId getCurrentObject();
	
	/**
	 * Returns the event that serves as a temporal reference for the watched objects.
	 */
	public abstract ILogEvent getRefEvent();
	
	/**
	 * Returns the list of available entries.
	 * This might be a time-consuming operation.
	 */
	public abstract List<Entry> getEntries();

	public static abstract class Entry
	{
		/**
		 * Returns the name of this entry.
		 * This method should execute quickly.
		 */
		public abstract String getName();
		
		/**
		 * Returns the possible values for this entry.
		 * This might be a time-consuming operation.
		 */
		public abstract Object[] getValue();

		/**
		 * Returns the possible setter events for this entry.
		 * This might be a time-consuming operation.
		 */
		public abstract IWriteEvent[] getSetter();
	}
}
