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
package tod.impl.dbgrid.bench;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.junit.Test;

import tod.impl.dbgrid.EventGenerator;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.bench.BenchBase.BenchResults;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridMessage;
import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;

public class BenchGridEventSerialization
{
	@Test public void bench()
	{
		bench(10000);
		bench(10000);
		bench(100000);
		bench(1*1000*1000);
	}
	
	private void bench(final int n)
	{
		System.out.println("Bench with n="+n);
		
		final EventGenerator theGenerator = new EventGenerator(0);
		
		BenchResults theBlankResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				for(int i=0;i<n;i++) theGenerator.next();
			}
		});
		
		System.out.println(theBlankResults);
		
		BenchResults theSerialResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				try
				{
					int i=0;
					while (i<n)
					{
						ObjectOutputStream theStream = new ObjectOutputStream(new DummyOutputStream());
						for(int j=0;j<100;j++) 
						{
							GridEvent theEvent = theGenerator.next();
							theStream.writeObject(theEvent);
							i++;
						}
					}
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		});
		
		BenchResults theBitResults = BenchBase.benchmark(new Runnable()
		{
			public void run()
			{
				BitStruct theBitStruct = new IntBitStruct(1000);
				for(int i=0;i<n;i++)
				{
					theBitStruct.reset();
					GridEvent theEvent = theGenerator.next();
					theEvent.writeTo(theBitStruct);
					
					theBitStruct.reset();
					GridMessage.read(theBitStruct);
				}
			}
		});
		
		float dt1 = (theSerialResults.totalTime - theBlankResults.totalTime)/1000f;
		float theEpS1 = n/dt1;
		
		float dt2 = (theBitResults.totalTime - theBlankResults.totalTime)/1000f;
		float theEpS2 = n/dt2;
		
		System.out.println(theSerialResults);
		System.out.println(theBitResults);
		System.out.println("Events/s (serial): "+theEpS1);
		System.out.println("Events/s (bit): "+theEpS2);
	}
	
	private static class DummyOutputStream extends OutputStream
	{
		@Override
		public void write(int aB) throws IOException
		{
		}
	}
}
