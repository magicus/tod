/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import tod.core.model.browser.IObjectInspector;
import tod.core.model.structure.IBehaviorInfo;
import tod.gui.view.LogView;
import zz.csg.api.IDisplay;

/**
 * Sequence seed for a particular field.
 * @author gpothier
 */
public class MethodSequenceSeed implements IEventSequenceSeed
{
	private IBehaviorInfo itsMethod;
	private final IObjectInspector itsInspector;

	public MethodSequenceSeed(IObjectInspector aInspector, IBehaviorInfo aMethod)
	{
		itsInspector = aInspector;
		itsMethod = aMethod;
	}

	public IEventSequenceView createView(IDisplay aDisplay, LogView aLogView)
	{
		return new MethodSequenceView(aDisplay, aLogView, itsInspector, itsMethod);
	}

}
