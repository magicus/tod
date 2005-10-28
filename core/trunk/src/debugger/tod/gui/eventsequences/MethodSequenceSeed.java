/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import reflex.lib.logging.miner.gui.view.LogView;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.trace.IObjectInspector;
import zz.csg.api.IDisplay;

/**
 * Sequence seed for a particular field.
 * @author gpothier
 */
public class MethodSequenceSeed implements IEventSequenceSeed
{
	private BehaviorInfo itsMethod;
	private final IObjectInspector itsInspector;

	public MethodSequenceSeed(IObjectInspector aInspector, BehaviorInfo aMethod)
	{
		itsInspector = aInspector;
		itsMethod = aMethod;
	}

	public IEventSequenceView createView(IDisplay aDisplay, LogView aLogView)
	{
		return new MethodSequenceView(aDisplay, aLogView, itsInspector, itsMethod);
	}

}
