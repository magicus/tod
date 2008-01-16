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
package tod.agent;



/**
 * This class performs some calibration of the timestamp parameters
 * @author gpothier
 */
public class TimestampCalibration
{
	/**
	 * Maximum number of events per second we want to be able to handle.
	 */
	private static final long evps = 1000000000;
	
	static
	{
		calibrate();
	}
	
	public static int shift;
	public static int inaccuracy;
	
	/**
	 * Calculates the average delay, in ns, between two distinct timestamp values.
	 */
	private static void calibrate()
	{
		long total = 0;
		long c = 0;
		for (int i=0;i<10;i++)
		{
			long d = getTimestampDelta();
			System.out.println("[TOD] Delay: "+d+"ns.");
			total += d;
			c++;
		}
		
		long avg = total/c;
		
		if (avg > Integer.MAX_VALUE) avg = Integer.MAX_VALUE;
		
		inaccuracy = BitUtilsLite.log2ceil((int) avg)-1;
		
		int extraRange = (int) (evps/1000000000L);
		shift = Math.max(BitUtilsLite.log2ceil(extraRange), 0);
		
		System.out.println("[TOD] Timer calibration done (d: "+avg+", s: "+shift+", i: "+inaccuracy+").");
	}
	
	/**
	 * Returns the time elapsed between two different timestamp values.
	 */
	private static long getTimestampDelta()
	{
		long s = System.nanoTime();
		long s2;
		while ((s2 = System.nanoTime()) == s);
		
		return s2-s;
	}
}
