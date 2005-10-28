/*
 * Created on Oct 11, 2004
 */
package reflex.lib.logging.core.impl.mop.behavior;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * This stream permits to collect output emitted within logged
 * methods.
 * @author gpothier
 */
public class CollectorStream extends ByteArrayOutputStream
{
	private OutputHandler itsHandler;
	
	public CollectorStream(OutputHandler aHandler)
	{
		itsHandler = aHandler;
	}
	
	public void flush() throws IOException
	{
		super.flush();
		if (size() > 0)
		{
			itsHandler.handleFlush(this);
			reset();
		}
	}
}