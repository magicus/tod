/*
 * Created on Oct 25, 2004
 */
package reflex.lib.logging;

import java.util.List;

/**
 * @author gpothier
 */
public class Util
{

	public static void ensureSize (List<?> aList, int aSize)
	{
		while (aList.size() <= aSize) aList.add (null);
	}

}
