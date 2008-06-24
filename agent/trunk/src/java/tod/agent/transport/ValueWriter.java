package tod.agent.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Helper class to serialize a Java object into a byte array.
 * @author minostro
 * @deprecated Dead-born... we must have a cross-platform representation of
 * objects.
 */
public class ValueWriter
{
	public static byte[] serialize(Object aObject) throws IOException
	{
		ByteArrayOutputStream theOut = new ByteArrayOutputStream();
		ObjectOutputStream theStream = new ObjectOutputStream(theOut);
		theStream.writeObject(ObjectValue.ensurePortable(aObject));
		theStream.flush();
		return theOut.toByteArray();
	}
}
