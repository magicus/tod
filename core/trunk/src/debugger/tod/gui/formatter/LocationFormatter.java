/*
 * Created on Nov 26, 2004
 */
package tod.gui.formatter;

import tod.core.BehaviourKind;
import tod.core.model.structure.IBehaviorInfo;
import tod.core.model.structure.IFieldInfo;
import tod.core.model.structure.IThreadInfo;
import tod.core.model.structure.ITypeInfo;
import zz.utils.AbstractFormatter;

/**
 * Formatter for {@link tod.core.model.structure.ILocationInfo}
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
		if (aObject instanceof IFieldInfo)
		{
			IFieldInfo theInfo = (IFieldInfo) aObject;
			return "field "+theInfo.getName();
		}
		else if (aObject instanceof ITypeInfo)
		{
			ITypeInfo theInfo = (ITypeInfo) aObject;
			return "class/interface "+theInfo.getName();
		}
		else if (aObject instanceof IBehaviorInfo)
		{
			IBehaviorInfo theInfo = (IBehaviorInfo) aObject;
			BehaviourKind theBehaviourType = theInfo.getBehaviourKind();
			return theBehaviourType.getName() + " " + theInfo.getName();
		}
		else if (aObject instanceof IThreadInfo)
		{
			IThreadInfo theInfo = (IThreadInfo) aObject;
			String theName = theInfo.getName();
			return theName != null ? 
					"Thread "+theName+" ("+theInfo.getId()+")" 
					: "Thread ("+theInfo.getId()+")";
		}
		else return "Not handled: "+aObject; 
	}

}
