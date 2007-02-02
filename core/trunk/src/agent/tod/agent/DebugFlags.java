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
package tod.agent;

import tod.core.EventInterpreter;

/**
 * This class groups severla flags that are used to
 * disable certain features for testing purposes.
 * 
 * @author gpothier
 */
public class DebugFlags
{
	/**
	 * Causes database nodes to skip incoming events
	 */
	public static final boolean SKIP_EVENTS = false; 
	
	/**
	 * Causes database nodes to not reorder incoming events
	 */
	public static final boolean DISABLE_REORDER = false; 
	
	/**
	 * Causes database nodes to not index incoming events.
	 */
	public static final boolean DISABLE_INDEXES = false; 
	
	/**
	 * Causes {@link HardPagedFile} to not store pages.
	 */
	public static final boolean DISABLE_STORE = false;
	
	/**
	 * Artificially limits the number of of object indexes.
	 * Set to 0 to disable the limit
	 */
	public static final int ALIAS_OBJECTS = 150000;
	
	/**
	 * Disables the bytecode location index.
	 */
	public static final boolean DISABLE_LOCATION_INDEX = false;
	
	/**
	 * Disables asynchronous file writes in {@link HardPagedFile}
	 */
	public static final boolean DISABLE_ASYNC_WRITES = true;
	
	/**
	 * if true, pages are not explicitly marked as used when accessed.
	 */
	public static final boolean DISABLE_USE_PAGES = false;

	/**
	 * Causes the {@link EventInterpreter} to ignore all events
	 */
	public static final boolean DISABLE_INTERPRETER = false;

	/**
	 * If true, the {@link EventInterpreter} prints all the events it receives
	 */
	public static final boolean EVENT_INTERPRETER_LOG = false;

	/**
	 * Causes the high level collectors to ignore all events
	 */
	public static final boolean COLLECTOR_IGNORE_ALL = false;

	/**
	 * Causes the socket collector to not send events
	 */
	public static final boolean DISABLE_EVENT_SEND = false;

	/**
	 * Causes the event collector to print the events it receives.
	 */
	public static final boolean COLLECTOR_LOG = false;
	
	/**
	 * If set to true, the local collector will actually store events.
	 */
	public static final boolean LOCAL_COLLECTOR_STORE = true;
	
	/**
	 * If not 0, {@link LogReceiver} prints received message counts
	 * every {@link #RECEIVER_PRINT_COUNTS} messages.
	 */
	public static final int RECEIVER_PRINT_COUNTS = 100000;
	
	/**
	 * If true, hierarchical dispatching parameters are
	 * tweaked so that there is one internal dispatcher, one
	 * leaf dispatcher and one db node.
	 */
	public static final boolean DISPATCH_FAKE_1 = false;
	
	static
	{
		if (SKIP_EVENTS == true) System.err.println("Warning: SKIP_EVENTS (DebugFlags)");
		if (DISABLE_REORDER == true) System.err.println("Warning: DISABLE_REORDER (DebugFlags)");
		if (DISABLE_INDEXES == true) System.err.println("Warning: DISABLE_INDEXES (DebugFlags)");
		if (DISABLE_STORE == true) System.err.println("Warning: DISABLE_STORE (DebugFlags)");
		if (ALIAS_OBJECTS != 0) System.err.println("Warning: ALIAS_OBJECTS (DebugFlags)");
		if (DISABLE_LOCATION_INDEX == true) System.err.println("Warning: DISABLE_LOCATION_INDEX (DebugFlags)");
		if (DISABLE_USE_PAGES == true) System.err.println("Warning: DISABLE_USE_PAGES (DebugFlags)");
		if (DISABLE_INTERPRETER == true) System.err.println("Warning: DISABLE_INTERPRETER (DebugFlags)");
		if (COLLECTOR_IGNORE_ALL == true) System.err.println("Warning: COLLECTOR_IGNORE_ALL (DebugFlags)");
		if (DISABLE_EVENT_SEND == true) System.err.println("Warning: DISABLE_EVENT_SEND (DebugFlags)");
		if (LOCAL_COLLECTOR_STORE == false) System.err.println("Warning: LOCAL_COLLECTOR_STORE (DebugFlags)");
		if (DISPATCH_FAKE_1 == true) System.err.println("Warning: DISPATCH_FAKE_1 (DebugFlags)");
	}
}
