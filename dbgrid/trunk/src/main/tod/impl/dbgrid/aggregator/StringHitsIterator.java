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
package tod.impl.dbgrid.aggregator;

import java.rmi.RemoteException;

import tod.impl.database.BufferedBidiIterator;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.db.RIBufferIterator;
import tod.impl.dbgrid.dispatch.RINodeConnector.StringSearchHit;

/**
 * An iterator that returns string search results provided by a 
 * buffer iterator.
 * @author gpothier
 */
public class StringHitsIterator extends BufferedBidiIterator<StringSearchHit[], Long> 
{
	private RIBufferIterator<StringSearchHit[]> itsSourceIterator;

	public StringHitsIterator(RIBufferIterator<StringSearchHit[]> aSourceIterator)
	{
		itsSourceIterator = aSourceIterator;
	}

	@Override
	protected StringSearchHit[] fetchNextBuffer()
	{
		try
		{
			return itsSourceIterator.next(DebuggerGridConfig.QUERY_ITERATOR_BUFFER_SIZE);
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected StringSearchHit[] fetchPreviousBuffer()
	{
		try
		{
			return itsSourceIterator.previous(DebuggerGridConfig.QUERY_ITERATOR_BUFFER_SIZE);
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Long get(StringSearchHit[] aBuffer, int aIndex)
	{
		return aBuffer[aIndex].getObjectId();
	}

	@Override
	protected int getSize(StringSearchHit[] aBuffer)
	{
		return aBuffer.length;
	}
	
	
	
}
