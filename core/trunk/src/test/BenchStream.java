

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class BenchStream
{
	public static void main(String[] args) throws IOException
	{
		byte[] buffer = new byte[10*1000*1000];
		
		long t0 = System.currentTimeMillis();
		
		for(int i=0;i<100;i++)
		{
			DataInputStream stream = new DataInputStream(new ByteArrayInputStream(buffer));
			while (stream.available() > 100)
			{
				stream.readByte();
				stream.readBoolean();
				stream.readLong();
				stream.readInt();
				stream.readShort();
			}
		}
		
		long t1 = System.currentTimeMillis();
		
		float dt = (t1-t0)/1000f;
		System.out.println("executed in "+dt+"s");
	}
}
