/*
 * Created on Aug 30, 2006
 */
package tod.utils;

import java.io.IOException;

public class NativeFile extends NativeStream
{
	public NativeFile(String aFileName, String aMode) throws IOException
	{
		long theFID = fopen(aFileName, aMode);
		if (theFID == 0) throw new IOException("Could not open file");
		setFID(theFID);
	}
}
