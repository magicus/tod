/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
package tod.experiments.bench;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DiskTest
{
	public static void main(String[] args) throws IOException
	{
		File theFile = new File("/home/gpothier/tmp/tod-raw.bin");
		if (theFile.exists()) theFile.delete();
		DataOutputStream theStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(theFile), 100000));
		
		
		int n = 1024;
		long t0 = System.currentTimeMillis();
		for (int i=0;i<n;i++) 
		{
			for(int j=0;j<1024*1024/8;j++) theStream.writeLong(j*i);
		}
		theStream.flush();
		theStream.close();

		long t1 = System.currentTimeMillis();
		
		System.out.println(String.format("%,.3fMB/s", n*1000f/(t1-t0)));
	}
}
