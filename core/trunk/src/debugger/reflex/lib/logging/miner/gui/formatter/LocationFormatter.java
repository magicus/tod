/*
 * Created on Nov 26, 2004
 */
package reflex.lib.logging.miner.gui.formatter;

import tod.core.BehaviourType;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.structure.TypeInfo;
import zz.utils.AbstractFormatter;

/**
 * Formatter for {@link tod.core.model.structure.LocationInfo}
 * @author gpothier
 */
public class LocationFormatter extends AbstractFormatter
{
	private static LocationFormatter INSTANCE = new LocationFormatter();

	public static LocationFormatter getInstance()
	{
		return INSTANCE;
	}

	private LocationFormatter()
	{
	}

	protected String getText(Object aObject, boolean aHtml)
	{
		if (aObject instanceof FieldInfo)
		{
			FieldInfo theInfo = (FieldInfo) aObject;
			return "field "+theInfo.getName();
		}
		else if (aObject instanceof TypeInfo)
		{
			TypeInfo theInfo = (TypeInfo) aObject;
			return "class/interface "+theInfo.getName();
		}
		else if (aObject instanceof BehaviorInfo)
		{
			BehaviorInfo theInfo = (BehaviorInfo) aObject;
			BehaviourType theBehaviourType = theInfo.getBehaviourType();
			return theBehaviourType.getName() + " " + theInfo.getName();
		}
		else if (aObject instanceof ThreadInfo)
		{
			ThreadInfo theInfo = (ThreadInfo) aObject;
			String theName = theInfo.getName();
			return theName != null ? 
					"Thread "+theName+" ("+theInfo.getId()+")" 
					: "Thread ("+theInfo.getId()+")";
		}
		else return "Not handled: "+aObject; 
	}

}
