/*
 * Created on Apr 16, 2006
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
