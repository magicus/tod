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
package tod.agent.transport;

/**
 * Commands that can be sent by the agent to the database and vice versa.
 * @author gpothier
 */
public enum Command
{
	/**
	 * This command flushes all buffered events and indexes.
	 * args: none
	 * return:
	 *  number of flushed events: int
	 */
	CMD_FLUSH,
	
	/**
	 * This command clears the database.
	 * args: none
	 * return: none
	 */
	CMD_CLEAR,
	
	/**
	 * This command notifies the database that this VM is ending.
	 */
	CMD_END,
	
	/**
	 * Tells the agent to enable/disable trace capture.
	 * args: boolean(byte) aEnable
	 * return: none 
	 */
	CMD_ENABLECAPTURE;
	
	/**
	 * Base value for sending serialized commands
	 */
	public static final int BASE = 100;
	
	/**
	 * Cached values; call to values() is costly. 
	 */
	public static final Command[] VALUES = values();

}
