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
package tod.bench.overhead;


import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Random;

public class Dummy
{
	public static final String RESULT_PREFIX = "EXEC: ";
	
	public static void main(String[] args)
	{
		System.out.println("Dummy");
		Object theObject = new Object();
		
		int j = 0;
		
		long t0 = System.currentTimeMillis();
		
		Object[] theObjects = new Object[100];
		for(int i=0;i<theObjects.length;i++) theObjects[i] = new Object();
		
		Random theRandom = new Random(0);
		for(int i=0;i<10000000;i++)
		{
			j += (i*2)%10;
			foo(theObjects[theRandom.nextInt(theObjects.length)], j);
			if (i % 1000000 == 0) System.out.println(i);
		}
		
		long t1 = System.currentTimeMillis();
		System.out.println("j: "+j);
		System.out.println(RESULT_PREFIX+(t1-t0));
	}
	
	public static int foo(Object o, long b)
	{
		long c = o.hashCode()+b;
		return (int)(c/2);
	}
}
