/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import tod.core.transport.LogReceiver;
import tod.impl.common.ObjectInspector;
import tod.utils.ConfigUtils;
import zz.utils.bit.BitUtils;


/**
 * This class groups several flags that are used to
 * disable certain features for testing purposes.
 * 
 * @author gpothier
 */
public class DebugFlags
{
	private static final String HOME = System.getProperty("user.home");	
	
	/**
	 * Causes database nodes to skip incoming events
	 */
	public static final boolean SKIP_EVENTS = ConfigUtils.readBoolean("skip-events", false); 
	
	/**
	 * Causes database nodes to skip incoming objects
	 */
	public static final boolean SKIP_OBJECTS = ConfigUtils.readBoolean("skip-objects", false); 
	
	/**
	 * Maximum number of events to process, or 0 for no limit.
	 */
	public static long MAX_EVENTS = ConfigUtils.readLong("max-events", 0);
	
	/**
	 * If true, then
	 */
	public static boolean REPLAY_MODE = ConfigUtils.readBoolean("replay-mode", false);
	
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
	 * Causes the event collector to print the events it receives.
	 */
	public static final boolean COLLECTOR_LOG = false;
	
	/**
	 * Stream to which the event collector sends debug info.
	 * Default is System.out
	 */
	public static final PrintStream COLLECTOR_PRINT_STREAM =
//		System.out;
		createStream(HOME+"/tmp/tod/collector.log");

	/**
	 * Whether a message should be printed whenever a new behavior is registered
	 */
	public static final boolean LOG_REGISTERED_BEHAVIORS = false;
	
	/**
	 * Whether timestamps should be pretty printed if {@link #COLLECTOR_LOG}
	 * is true.
	 */
	public static final boolean COLLECTOR_FORMAT_TIMESTAMPS = false;
	
	/**
	 * If set to true, the local collector will actually store events.
	 */
	public static final boolean LOCAL_COLLECTOR_STORE = true;
	
	/**
	 * If not 0, {@link LogReceiver} prints received message counts
	 * every {@link #RECEIVER_PRINT_COUNTS} messages.
	 */
	public static final int RECEIVER_PRINT_COUNTS = (int) BitUtils.pow2(20);
	
	/**
	 * If true, hierarchical dispatching parameters are
	 * tweaked so that there is one internal dispatcher, one
	 * leaf dispatcher and one db node.
	 */
	public static final boolean DISPATCH_FAKE_1 = false;
	
	/**
	 * Ignore host when filtering on threads.
	 * Should not be used if more than one host is being debugged.
	 */
	public static final boolean IGNORE_HOST = ConfigUtils.readBoolean("ignore-host", false); 
	
	/**
	 * Whether {@link ObjectInspector} should try to guess the type of unknown objects
	 */
	public static final boolean TRY_GUESS_TYPE = ConfigUtils.readBoolean("try-guess-type", false);
	
	/**
	 * Whether to show GUI items that are used for debugging.
	 */
	public static final boolean SHOW_DEBUG_GUI = ConfigUtils.readBoolean("show-debug-gui", false);
	
	/**
	 * Whether murals should be computed for MuralScroller
	 */
	public static final boolean COMPUTE_SCROLLER_MURALS = false;
	
	private static PrintStream createStream(String aName)
	{
		try
		{
			File theFile = new File(aName);
			theFile.delete();
			File theParentFile = theFile.getParentFile();
			if (theParentFile != null) theParentFile.mkdirs();
//			System.out.println(theFile.getAbsolutePath());
			return new PrintStream(new FileOutputStream(theFile));
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	static
	{
		if (SKIP_EVENTS == true) System.err.println("******* Warning: SKIP_EVENTS (DebugFlags)");
		if (DISABLE_REORDER == true) System.err.println("******* Warning: DISABLE_REORDER (DebugFlags)");
		if (DISABLE_INDEXES == true) System.err.println("******* Warning: DISABLE_INDEXES (DebugFlags)");
		if (DISABLE_STORE == true) System.err.println("******* Warning: DISABLE_STORE (DebugFlags)");
		if (DISABLE_LOCATION_INDEX == true) System.err.println("******* Warning: DISABLE_LOCATION_INDEX (DebugFlags)");
		if (DISABLE_USE_PAGES == true) System.err.println("******* Warning: DISABLE_USE_PAGES (DebugFlags)");
		if (LOCAL_COLLECTOR_STORE == false) System.err.println("******* Warning: LOCAL_COLLECTOR_STORE (DebugFlags)");
		if (DISPATCH_FAKE_1 == true) System.err.println("******* Warning: DISPATCH_FAKE_1 (DebugFlags)");
		if (IGNORE_HOST == true) System.err.println("******* Warning: IGNORE_HOST (DebugFlags)");
	}
}