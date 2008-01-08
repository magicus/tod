/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.agent;

/**
 * This class keeps a registry of traced methods.
 * This is used by the instrumentation of method calls:
 * the events generated for a call to a traced method are
 * not the same as those of a non-traced method.
 * @author gpothier
 */
public class TracedMethods
{
	private static boolean[] traced = new boolean[10000];
	
	public static final void setTraced(int aId)
	{
		if (aId >= traced.length)
		{
			boolean[] room = new boolean[aId*2];
			System.arraycopy(traced, 0, room, 0, traced.length);
			System.out.println("Reallocated TracedMethods: "+room.length);
			traced = room;
		}
		
		//System.out.println("Marking traced: "+aId);
		traced[aId] = true;
	}
	
	public static final boolean isTraced(int aId)
	{
		//System.out.println("isTraced: "+aId);
		return aId >= traced.length ? false : traced[aId];
	}
}
