/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package java.tod;

import java.tod.io._IO;
import java.tod.io._SocketChannel;

import tod2.access.TODAccessor;



/**
 * Contains a few flags that indicate the state of the native & java agent,
 * as well as the enabling/disabling of trace capture.
 * 
 * @author gpothier
 */
public class AgentReady
{
	/**
	 * Set to true once the {@link EventCollector} is ready to receive events.
	 */
	public static boolean COLLECTOR_READY = false;
	
	/**
	 * This flag is set to true by the native agent, if it is properly loaded.
	 */
	private static boolean NATIVE_AGENT_LOADED = false;
	
	private static boolean STARTED = false;
	
	/**
	 * Called by the native agent.
	 */
	private static void nativeAgentLoaded()
	{
		NATIVE_AGENT_LOADED = true;
	}
	
	/**
	 * Whether the native agent is enabled.
	 */
	public static boolean isNativeAgentLoaded()
	{
		return NATIVE_AGENT_LOADED;
	}
	
	public static boolean isStarted()
	{
		return STARTED;
	}
	
	/**
	 * Called by the native agent when the system is ready to start capturing
	 */
	public static void start()
	{
		// Force loading of native methods.
		_IO.initNatives();
		_SocketChannel.initNatives();
		ObjectIdentity.get("!");
		
		EventCollector.INSTANCE.init();
		TOD.loadInitialCaptureState();
		
		ThreadData.load();
		TODAccessor.setBootstrapFlag(true);
		
		STARTED = true;
	}
	
	
	// The methods below are used by the native agent for native wrapping
	public static void evOOSEnter()
	{
		EventCollector._getThreadData().evOutOfScopeBehaviorEnter(-1);
	}

	public static void evOOSExit_Normal()
	{
		EventCollector._getThreadData().evOutOfScopeBehaviorExit_Normal();
	}
	
	public static void evOOSExit_Exception()
	{
		EventCollector._getThreadData().evOutOfScopeBehaviorExit_Exception();
	}
	
	public static void sendResult_Ref(Object aValue)
	{
		ThreadData theThreadData = EventCollector._getThreadData();
		if (theThreadData.isInScope()) theThreadData.sendValue_Ref(aValue);
	}
	
	public static void sendResult_Int(int aValue)
	{
		ThreadData theThreadData = EventCollector._getThreadData();
		if (theThreadData.isInScope()) theThreadData.sendValue_Int(aValue);
	}
}
