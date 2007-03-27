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

import javax.swing.JComponent;

import tod.core.database.event.ILogEvent;
import tod.core.database.event.IWriteEvent;
import tod.core.database.structure.ObjectId;
import tod.gui.JobProcessor;

/**
 * Provider of watch data.
 * @author gpothier
 */
public interface IWatchProvider<E>
{
	/**
	 * Builds the title of the watch window.
	 * @param aJobProcessor A job processor that can be used if elements
	 * of the title are to be created asynchronously.
	 */
	public JComponent buildTitle(JobProcessor aJobProcessor);
	
	/**
	 * Returns a current object. Currently this is only for
	 * stack frame reconstitution, represents the "this" variable.
	 */
	public ObjectId getCurrentObject();
	
	/**
	 * Returns the event that serves as a temporal reference for the watched objects.
	 */
	public ILogEvent getRefEvent();
	
	/**
	 * Returns the list of available entries.
	 * This might be a time-consuming operation.
	 */
	public List<E> getEntries();

	/**
	 * Returns the name of the given entry.
	 * This method should execute quickly.
	 */
	public String getEntryName(E aEntry);

	/**
	 * Returns the possible values for the given entry.
	 * This might be a time-consuming operation.
	 */
	public Object[] getEntryValue(E aEntry);

	/**
	 * Returns the possible setter events for the given entry.
	 * This might be a time-consuming operation.
	 */
	public IWriteEvent[] getEntrySetter(E aEntry);
}
