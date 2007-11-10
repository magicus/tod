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
package tod.impl.dbgrid.db;

import tod.agent.DebugFlags;
import tod.impl.dbgrid.db.IndexSet.IndexManager;
import tod.impl.dbgrid.db.file.HardPagedFile;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Probe;
import tod.utils.ObjectCodec;

/**
 * An index set specialized for objects.
 * @author gpothier
 */
public class ObjectIndexSet extends RoleIndexSet
{
	
	public ObjectIndexSet(
			String aName, 
			IndexManager aIndexManager,
			HardPagedFile aFile, 
			int aIndexCount)
	{
		super(aName, aIndexManager, aFile, aIndexCount);
		
	}

	@Override
	public void addTuple(int aIndex, RoleTuple aTuple)
	{
		if (DebugFlags.ALIAS_OBJECTS > 0)
		{
			aIndex = ((aIndex-1) % DebugFlags.ALIAS_OBJECTS) + 1;
		}
		
		super.addTuple(aIndex, aTuple);
	}
	

}
