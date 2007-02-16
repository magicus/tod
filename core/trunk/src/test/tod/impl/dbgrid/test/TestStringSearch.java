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
package tod.impl.dbgrid.test;

import java.rmi.RemoteException;

import org.junit.Test;

import tod.core.config.TODConfig;
import tod.impl.dbgrid.db.RIBufferIterator;
import tod.impl.dbgrid.dispatch.LeafEventDispatcher;
import tod.impl.dbgrid.dispatch.RILeafDispatcher.StringSearchHit;
import tod.impl.dbgrid.gridimpl.uniform.UniformEventDispatcher;

public class TestStringSearch
{
	private static final String[] STRINGS = {
		"Hello", "World", "Hello World", "Hello123", "123", "Hello123World", "HelloWorld"
	};
	
	@Test public void testSearch() throws RemoteException
	{
		TODConfig theConfig = new TODConfig();
		theConfig.set(TODConfig.INDEX_STRINGS, true);
		LeafEventDispatcher theDispatcher = new UniformEventDispatcher(null);

		for (int i=0;i<STRINGS.length;i++)
		{
			theDispatcher.register(i, STRINGS[i]);
		}
		
		search(theDispatcher, "Hello");
		search(theDispatcher, "123");
		search(theDispatcher, "Hello World");
		search(theDispatcher, "H*lo");
	}
	
	private void search(LeafEventDispatcher aDispatcher, String aText) throws RemoteException
	{
		System.out.println("Search: "+aText);
		printIterator(aDispatcher.searchStrings(aText));
		System.out.println("Done");
	}
	
	private void printIterator(RIBufferIterator<StringSearchHit[]> aIterator) throws RemoteException
	{
		while(true)
		{
			StringSearchHit[] theHits = aIterator.next(1);
			if (theHits == null) break;
			
			StringSearchHit theHit = theHits[0];
			
			System.out.println(theHit.getObjectId()
					+": "+STRINGS[(int) theHit.getObjectId()]
					+" ("+theHit.getScore()+")");
		}
	}
}
