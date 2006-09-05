/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import java.awt.Color;

import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMemberInfo;
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
