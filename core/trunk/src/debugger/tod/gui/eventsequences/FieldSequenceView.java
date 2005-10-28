/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import java.awt.Color;

import reflex.lib.logging.miner.gui.view.LogView;
import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.MemberInfo;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IObjectInspector;
import tod.gui.ObjectInspectorView;
import zz.csg.api.IDisplay;
import zz.csg.api.IRectangularGraphicObject;

public class FieldSequenceView extends AbstractMemberSequenceView
{
	public static final Color FIELD_COLOR = Color.BLUE;
	
	private FieldInfo itsField;

	
	public FieldSequenceView(IDisplay aDisplay, LogView aLogView, IObjectInspector aInspector, FieldInfo aField)
	{
		super(aDisplay, aLogView, aInspector, FIELD_COLOR);
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
	public MemberInfo getMember()
	{
		return itsField;
	}
}
