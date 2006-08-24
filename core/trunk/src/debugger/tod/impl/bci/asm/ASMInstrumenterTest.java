/*
 * Created on Oct 27, 2005
 */
package tod.impl.bci.asm;

import java.net.URI;
import java.net.URISyntaxException;

import tod.core.session.ASMLocalSession;


public class ASMInstrumenterTest
{
	public static void main(String[] args) throws URISyntaxException
	{
		new ASMLocalSession(new URI("file:/home/gpothier/tmp/ASM"), null, null, null);
//		new ASMLocalSession(null, null, null, null);
	}
}
