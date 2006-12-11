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
package tod.core.database.browser;

import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IThreadInfo;
import zz.utils.tree.ITree;

/**
 * Permits to determine control flow information of a given thread, 
 * providing a tree view of the events.
 * @author gpothier
 */
public interface ICFlowBrowser extends ITree<ILogEvent, ILogEvent>
{
	/**
	 * Returns the thread considered by this browser.
	 */
	public IThreadInfo getThread();
}
