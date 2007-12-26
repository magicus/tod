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
import tod.impl.dbgrid.dispatch.DatabaseNode;
import tod.impl.dbgrid.dispatch.RIDatabaseNode.StringSearchHit;

public class TestStringSearch
{
	private static final String[] STRINGS = {
		"Hello", "World", "Hello World", "Hello123", "123", "Hello123World", "HelloWorld"
	};
	
	@Test public void testSearch() throws RemoteException
	{
		TODConfig theConfig = new TODConfig();
		theConfig.set(TODConfig.INDEX_STRINGS, true);
		DatabaseNode theNode = DatabaseNode.createLocalNode();

		for (int i=0;i<STRINGS.length;i++)
		{
			theNode.register(i, STRINGS[i], i);
		}
		
		search(theNode, "Hello");
		search(theNode, "123");
		search(theNode, "Hello World");
		search(theNode, "H*lo");
	}
	
	private void search(DatabaseNode aNode, String aText) throws RemoteException
	{
		System.out.println("Search: "+aText);
		printIterator(aNode.searchStrings(aText));
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
