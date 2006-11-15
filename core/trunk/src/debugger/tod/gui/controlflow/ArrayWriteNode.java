/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;

import tod.core.database.event.IArrayWriteEvent;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.gui.Hyperlinks;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.figures.SVGFlowText;

public class ArrayWriteNode extends AbstractEventNode
{
	private IArrayWriteEvent itsEvent;

	public ArrayWriteNode(
			CFlowView aView,
			IArrayWriteEvent aEvent)
	{
		super(aView);
		
		itsEvent = aEvent;

		setLayoutManager(new SequenceLayout());
		
		Object theCurrentObject = null;
		IBehaviorCallEvent theContainer = itsEvent.getParent();
		if (theContainer != null)
		{
			theCurrentObject = theContainer.getTarget();
		}
		
		pChildren().add(Hyperlinks.object(getGUIManager(), getEventTrace(), theCurrentObject, itsEvent.getTarget(), CFlowTreeBuilder.FONT));
		pChildren().add(SVGFlowText.create("[", CFlowTreeBuilder.FONT, Color.BLACK));
		pChildren().add(SVGFlowText.create(""+itsEvent.getIndex(), CFlowTreeBuilder.FONT, Color.BLACK));
		pChildren().add(SVGFlowText.create("] = ", CFlowTreeBuilder.FONT, Color.BLACK));
		pChildren().add(Hyperlinks.object(getGUIManager(), getEventTrace(), theCurrentObject, itsEvent.getValue(), CFlowTreeBuilder.FONT));
	}
	
	@Override
	protected ILogEvent getEvent()
	{
		return itsEvent;
	}
}