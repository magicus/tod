/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import tod.core.database.browser.IObjectInspector;
import tod.core.database.structure.IFieldInfo;
import tod.gui.view.LogView;
import zz.csg.api.IDisplay;

/**
 * Sequence seed for a particular field.
 * @author gpothier
 */
public class FieldSequenceSeed implements IEventSequenceSeed
{
	private IFieldInfo itsField;
	private final IObjectInspector itsInspector;

	public FieldSequenceSeed(IObjectInspector aInspector, IFieldInfo aField)
	{
		itsInspector = aInspector;
		itsField = aField;
	}

	public IEventSequenceView createView(IDisplay aDisplay, LogView aLogView)
	{
		return new FieldSequenceView(aDisplay, aLogView, itsInspector, itsField);
	}

}
