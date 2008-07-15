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
package dummy;

/**
 * A fully instrumented, CPU-intensive program.
 * @author gpothier
 */
public class BurnTest
{
	private static int rndSeed = 1234598;
	private static final int N = 50000;
	
	public static void main(String[] args)
	{
		System.out.println("Burn test");
//		StringBuilder b = new StringBuilder("ho");
//		while(true)
//		{
//			b.append(b.toString());
//		}
		
		// Warm up
		Node root = createTree(null, 10000);
		for (int i=0;i<10;i++) root.visit();
		
		// Real thing
		long t0 = System.currentTimeMillis();
		root = createTree(null, N);
		long t1 = System.currentTimeMillis();
		for (int i=0;i<100;i++) root.visit();
		long t2 = System.currentTimeMillis();
		
		float dt1 = 1f*(t1-t0)/1000;
		float dt2 = 1f*(t2-t1)/1000;
		
		System.out.println(String.format("Create: %.2fs, visit: %.2fds", dt1, dt2));
	}
	
	public static int random(int max)
	{
		rndSeed = rndSeed*57 + 9;
		if (rndSeed<0) rndSeed = -rndSeed;
		return rndSeed % max;
	}
	
	/**
	 * Generates a tree with n nodes.
	 */
	public static Node createTree(Node parent, int n)
	{
		Node[] children = new Node[10];
		if (n == 1) return new Node(parent, null, "leaf");
		
		Node node = new Node(parent, children, "Subtree size: "+n);
		
		int remaining = n;
		int i=0;
		while (remaining > 0)
		{
			int subN = i < 9 ? random(1 + n/2) : remaining;
			children[i] = subN > 0 ? createTree(node, subN) : null;
			remaining -= subN;
			i++;
		}
		
		return node;
	}
	
	private static class Node
	{
		private Node itsParent;
		private Node[] itsChildren;
		private Object itsValue;

		public Node(Node aParent, Node[] aChildren, Object aValue)
		{
			itsParent = aParent;
			itsChildren = aChildren;
			itsValue = aValue;
		}

		public Node getParent()
		{
			return itsParent;
		}
		
		public Node[] getChildren()
		{
			return itsChildren;
		}
		
		public Object getValue()
		{
			return itsValue;
		}
		
		public void visit()
		{
			if (itsChildren != null)
			{
				for (Node child : itsChildren) if (child != null) child.visit();
			}
		}
	}
}
