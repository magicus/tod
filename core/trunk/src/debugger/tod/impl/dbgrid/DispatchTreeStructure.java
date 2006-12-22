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
package tod.impl.dbgrid;

import static tod.impl.dbgrid.DebuggerGridConfig.*;

/**
 * Utility class that permits to determine the number of
 * leaf and internal dispatchers given a number of database nodes.
 * @author gpothier
 */
public class DispatchTreeStructure
{
	public final int leafNodes;
	public final int internalNodes;
	public final int databaseNodes;
	
	public DispatchTreeStructure(final int aLeafNodes, final int aInternalNodes, final int aDatabaseNodes)
	{
		leafNodes = aLeafNodes;
		internalNodes = aInternalNodes;
		databaseNodes = aDatabaseNodes;
	}
	
	public static DispatchTreeStructure compute(int aDatabaseNodes)
	{
		int theLeafNodes = (aDatabaseNodes+DISPATCH_BRANCHING_FACTOR-1)/DISPATCH_BRANCHING_FACTOR;
		if (theLeafNodes == 1) return new DispatchTreeStructure(0, 0, aDatabaseNodes);
		
		int theTotalInternalNodes = 0;
		int theInternalNodes = (theLeafNodes+DISPATCH_BRANCHING_FACTOR-1)/DISPATCH_BRANCHING_FACTOR;
		while (true)
		{
			if (theInternalNodes == 1) return new DispatchTreeStructure(theLeafNodes, theTotalInternalNodes, aDatabaseNodes);
			theTotalInternalNodes += theInternalNodes;
			theInternalNodes = (theInternalNodes+DISPATCH_BRANCHING_FACTOR-1)/DISPATCH_BRANCHING_FACTOR;
		}
	}
	
	@Override
	public String toString()
	{
		return String.format(
				"Tree structure: %d db nodes, %d leaf dispatch, %d internal dispatch",
				databaseNodes,
				leafNodes,
				internalNodes);
	}
	
	public static void main(String[] args)
	{
		for(int i=1;i<1000;i++)
		{
			System.out.println(compute(i));
		}
	}
}
