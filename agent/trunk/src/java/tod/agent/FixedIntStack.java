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
package tod.agent;

/**
 * Copied from zz.utils.
 * Implements a fixed-size stack of primitive integers.
 * @author gpothier
 */
public class FixedIntStack
{
	private int[] itsData;
	private int itsHeight;
	
	public FixedIntStack(int aInitialSize)
	{
		itsData = new int[aInitialSize];
		itsHeight = 0;
	}
	
	public void push(int aValue)
	{
		itsData[itsHeight++] = aValue;
	}
	
	public int pop()
	{
		return itsData[--itsHeight];
	}
	
	public boolean isEmpty()
	{
		return itsHeight == 0;
	}
	
	public void clear()
	{
		itsHeight = 0;
	}
}
