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
package tod.impl.dbgrid.dispatch;

import tod.core.database.structure.IExceptionResolver;
import tod.impl.dbgrid.GridMaster;

/**
 * This is a "fake" exception resolver that simply delegates
 * to the grid master's resolver.
 * @author gpothier
 */
public class LocalNodeExceptionResolver implements IExceptionResolver
{
	private final DatabaseNode itsNode;
	private GridMaster itsMaster;
	
	public LocalNodeExceptionResolver(DatabaseNode aNode)
	{
		itsNode = aNode;
	}

	public int getBehaviorId(String aClassName, String aMethodName, String aMethodSignature)
	{
		if (itsMaster == null) itsMaster = (GridMaster) itsNode.getMaster();
		return itsMaster.getBehaviorId(aClassName, aMethodName, aMethodSignature);
	}

}
