/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;

import reflex.lib.logging.miner.gui.IGUIManager;
import tod.core.model.event.IBehaviorEnterEvent;
import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventTrace;
import tod.gui.Hyperlinks;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.figures.SVGFlowText;

public class FieldWriteNode extends AbstractEventNode
{
	private IFieldWriteEvent itsEvent;

	public FieldWriteNode(
			CFlowView aView,
			IFieldWriteEvent aEvent)
	{
		super(aView);
		
		itsEvent = aEvent;

		setLayoutManager(new SequenceLayout());
		
		Object theCurrentObject = null;
		IBehaviorEnterEvent theContainer = itsEvent.getParent();
		if (theContainer != null)
		{
			theCurrentObject = theContainer.getTarget();
		}
		
		pChildren().add(Hyperlinks.object(getGUIManager(), getEventTrace(), theCurrentObject, itsEvent.getTarget(), CFlowTreeBuilder.FONT));
		pChildren().add(SVGFlowText.create(".", CFlowTreeBuilder.FONT, Color.BLACK));
		pChildren().add(SVGFlowText.create(itsEvent.getField().getName(), CFlowTreeBuilder.FONT, Color.BLACK));
		pChildren().add(SVGFlowText.create(" = ", CFlowTreeBuilder.FONT, Color.BLACK));
		pChildren().add(Hyperlinks.object(getGUIManager(), getEventTrace(), theCurrentObject, itsEvent.getValue(), CFlowTreeBuilder.FONT));
	}
	
	@Override
	protected ILogEvent getMainEvent()
	{
		return itsEvent;
	}
}
