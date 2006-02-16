/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import java.awt.Color;

import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.IFieldInfo;
import tod.core.model.structure.IMemberInfo;
import tod.core.model.trace.IObjectInspector;
import tod.gui.view.LogView;
import zz.csg.api.IDisplay;
import zz.csg.api.IRectangularGraphicObject;

public class FieldSequenceView extends AbstractMemberSequenceView
{
	public static final Color FIELD_COLOR = Color.BLUE;
	
	private IFieldInfo itsField;

	
	public FieldSequenceView(IDisplay aDisplay, LogView aLogView, IObjectInspector aInspector, IFieldInfo aField)
	{
		super(aDisplay, aLogView, FIELD_COLOR, aInspector);
		itsField = aField;
	}

	public String getTitle()
	{
		return "field " + itsField.getName();
	}

	@Override
	protected IRectangularGraphicObject getBaloon(ILogEvent aEvent)
	{
		IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;
		Object theValue = theEvent.getValue();
		return createBaloon(theValue);
	}

	@Override
	public IMemberInfo getMember()
	{
		return itsField;
	}
}
