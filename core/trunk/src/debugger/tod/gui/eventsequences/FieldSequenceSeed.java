/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import reflex.lib.logging.miner.gui.view.LogView;
import tod.core.model.structure.FieldInfo;
import tod.core.model.trace.IObjectInspector;
import zz.csg.api.IDisplay;

/**
 * Sequence seed for a particular field.
 * @author gpothier
 */
public class FieldSequenceSeed implements IEventSequenceSeed
{
	private FieldInfo itsField;
	private final IObjectInspector itsInspector;

	public FieldSequenceSeed(IObjectInspector aInspector, FieldInfo aField)
	{
		itsInspector = aInspector;
		itsField = aField;
	}

	public IEventSequenceView createView(IDisplay aDisplay, LogView aLogView)
	{
		return new FieldSequenceView(aDisplay, aLogView, itsInspector, itsField);
	}

}
