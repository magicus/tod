/*
 * Created on Nov 16, 2004
 */
package reflex.lib.logging.miner.gui.formatter;

import tod.core.model.structure.ObjectId;
import zz.utils.AbstractFormatter;

/**
 * @author gpothier
 */
public class ObjectFormatter extends AbstractFormatter
{
	private static ObjectFormatter INSTANCE = new ObjectFormatter();

	public static ObjectFormatter getInstance()
	{
		return INSTANCE;
	}

	private ObjectFormatter()
	{
	}

	protected String getText(Object aObject, boolean aHtml)
	{
		if (aObject == null) return "null";
		else if (aObject instanceof ObjectId.ObjectHash)
		{
			ObjectId.ObjectHash theHash = (ObjectId.ObjectHash) aObject;
			return "Object (hash: "+theHash.getHascode()+")";
		}
		else if (aObject instanceof ObjectId.ObjectUID)
		{
			ObjectId.ObjectUID theObjectUID = (ObjectId.ObjectUID) aObject;
			return "Object (uid: "+theObjectUID.getId()+")";
		}
		else return ""+aObject;
	}

}
