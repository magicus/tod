/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package btree;

import java.io.File;
import java.util.Random;

import javax.swing.JPanel;


public class BTreeTest
{
	public static int t = 100;
	public static int n = 2000;
	public static int keysRange = Integer.MAX_VALUE;
	
	public static void main(String[] args)
	{
		testNode();
		testPut();
		testRemove();
	}

	public static void testNode()
	{
		// Test merging
//		testMergeLeft(1);
//		testMergeLeft(5);
		testMergeRight(0);
		testMergeRight(4);
	}
	
	public static void testMergeLeft(int aIndex)
	{
		assert aIndex > 0 && aIndex <= 5;
		
		Config.setT(3);
		Node theParent = new Node(
				100, 
				5,
				new long[] {10, 20, 30, 40, 50},
				new long[] {2, 4, 6, 8, 10},
				new int[] {0, 1, 2, 3, 4, 5});
		
		Node theChild = new Node(
				aIndex,
				2,
				new long[] {100, 101, -1, -1, -1},
				new long[] {1, 2, -1, -1, -1},
				new int[] {10, 11, 12, -1, -1, -1});
		
		Node theNeighbour = new Node(
				aIndex-1,
				2,
				new long[] {200, 201, -1, -1, -1},
				new long[] {3, 4, -1, -1, -1},
				new int[] {20, 21, 22, -1, -1, -1});
		
		theParent.mergeLeft(aIndex, theChild, theNeighbour);
	}
	
	public static void testMergeRight(int aIndex)
	{
		assert aIndex >= 0 && aIndex < 5;
		
		Config.setT(3);
		Node theParent = new Node(
				100, 
				5,
				new long[] {10, 20, 30, 40, 50},
				new long[] {2, 4, 6, 8, 10},
				new int[] {0, 1, 2, 3, 4, 5});
		
		Node theChild = new Node(
				aIndex,
				2,
				new long[] {100, 101, -1, -1, -1},
				new long[] {1, 2, -1, -1, -1},
				new int[] {10, 11, 12, -1, -1, -1});
		
		Node theNeighbour = new Node(
				aIndex-1,
				2,
				new long[] {200, 201, -1, -1, -1},
				new long[] {3, 4, -1, -1, -1},
				new int[] {20, 21, 22, -1, -1, -1});
		
		theParent.mergeRight(aIndex, theChild, theNeighbour);
	}
	
	public static void testPut()
	{
		File theFile = new File("btree");
		theFile.delete();
		Config.setT(t);
		BTree theTree = new BTree(theFile, true);

		Random theRandom = new Random(0);
		for (int i=0;i<n;i++)
		{
			long theLong = theRandom.nextInt(keysRange);
			System.out.println("Put: "+theLong);
			theTree.put(theLong, theLong);
		}
		
		int theRead0 = theTree.getManager().getReadCount();
		int theWrite0 = theTree.getManager().getWriteCount();

		
		theRandom = new Random(0);
		for (int i=0;i<n;i++)
		{
			long theLong = theRandom.nextInt(keysRange);
			Long theValue = theTree.get(theLong);
			System.out.println("Get1: "+theLong+" -> "+theValue);
			assert theValue != null && theValue.longValue() == theLong;
		}
		
		int theRead1 = theTree.getManager().getReadCount();
		int theWrite1 = theTree.getManager().getWriteCount();
		
		theTree = new BTree(theFile, true);

		theRandom = new Random(0);
		for (int i=0;i<n;i++)
		{
			long theLong = theRandom.nextInt(keysRange);
			Long theValue = theTree.get(theLong);
			System.out.println("Get2: "+theLong+" -> "+theValue);
			assert theValue != null && theValue.longValue() == theLong;
		}
		
		int theRead2 = theTree.getManager().getReadCount();
		int theWrite2 = theTree.getManager().getWriteCount();
		
		
		System.out.println("t: "+t+", n: "+n+", page size: "+Config.pageSize());
		System.out.println("Disk reads 0: "+theRead0);
		System.out.println("Disk writes 0: "+theWrite0);
		System.out.println("Disk reads 1: "+(theRead1-theRead0));
		System.out.println("Disk writes 1: "+theWrite1);
		System.out.println("Disk reads 2: "+theRead2);
		System.out.println("Disk writes 2: "+theWrite2);
	}
	
	public static void testRemove()
	{
		int theActualN = (n/3)*3;
		File theFile = new File("btree");
		theFile.delete();
		Config.setT(t);
		BTree theTree = new BTree(theFile, true);
		
		long theLong;
		Random theRandom = new Random(0);
		for (int i=0;i<theActualN;i++)
		{
			theLong = theRandom.nextInt(keysRange)*2;
			System.out.println("Put: "+theLong);
			theTree.put(theLong, theLong);
//			System.out.println(theTree);
		}
		
		int theRead0 = theTree.getManager().getReadCount();
		int theWrite0 = theTree.getManager().getWriteCount();
		
		
		theRandom = new Random(0);
		for (int i=0;i<theActualN/3;i++)
		{
			theRandom.nextInt(keysRange);
			theRandom.nextInt(keysRange);
			theLong = theRandom.nextInt(keysRange)*2;
			System.out.println("Delete: "+theLong);
			theTree.remove(theLong);
//			System.out.println(theTree);
			
			theLong++;
			System.out.println("Delete: "+theLong);
			assert ! theTree.remove(theLong);
//			System.out.println(theTree);
		}
		
		int theRead1 = theTree.getManager().getReadCount();
		int theWrite1 = theTree.getManager().getWriteCount();
		
		theTree = new BTree(theFile, true);
		
		theRandom = new Random(0);
		for (int i=0;i<theActualN/3;i++)
		{
			theLong = theRandom.nextInt(keysRange)*2;
			Long theValue = theTree.get(theLong);
			System.out.println("Get: "+theLong+" -> "+theValue);
			assert theValue != null && theValue.longValue() == theLong;
			
			theLong = theRandom.nextInt(keysRange)*2;
			theValue = theTree.get(theLong);
			System.out.println("Get: "+theLong+" -> "+theValue);
			assert theValue != null && theValue.longValue() == theLong;
			
			theLong = theRandom.nextInt(keysRange)*2;
			theValue = theTree.get(theLong);
			System.out.println("Get: "+theLong+" -> "+theValue);
			assert theValue == null;
		}
		
		int theRead2 = theTree.getManager().getReadCount();
		int theWrite2 = theTree.getManager().getWriteCount();
		
		
		System.out.println("t: "+t+", n: "+n+", page size: "+Config.pageSize());
		System.out.println("Disk reads 0: "+theRead0);
		System.out.println("Disk writes 0: "+theWrite0);
		System.out.println("Disk reads 1: "+(theRead1-theRead0));
		System.out.println("Disk writes 1: "+theWrite1);
		System.out.println("Disk reads 2: "+theRead2);
		System.out.println("Disk writes 2: "+theWrite2);
	}
}
