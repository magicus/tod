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

import tod.impl.dbgrid.db.DatabaseNode;

/**
 * Wraps an exception that occurred in a {@link DatabaseNode}
 * @author gpothier
 */
public class NodeException extends RuntimeException
{
	private static final long serialVersionUID = -2467881614217337652L;
	private int itsNodeId;

	public NodeException(int aNodeId, Throwable aCause)
	{
		super("Exception occurred in node "+aNodeId, aCause);
		itsNodeId = aNodeId;
	}

	public int getNodeId()
	{
		return itsNodeId;
	}
}
