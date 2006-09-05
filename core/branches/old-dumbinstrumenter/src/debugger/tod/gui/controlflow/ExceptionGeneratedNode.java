/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;

import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.event.ILogEvent;
import tod.gui.Hyperlinks;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.figures.SVGFlowText;

public class ExceptionGeneratedNode extends AbstractEventNode
{
	private IExceptionGeneratedEvent itsEvent;

	public ExceptionGeneratedNode(
			CFlowView aView,
			IExceptionGeneratedEvent aEvent)
	{
		super(aView);
		
		itsEvent = aEvent;

		setLayoutManager(new SequenceLayout());
		
		pChildren().add(SVGFlowText.create("Exception: ", CFlowTreeBuilder.FONT, Color.RED));
		pChildren().add(Hyperlinks.object(getGUIManager(), getEventTrace(), itsEvent.getException(), CFlowTreeBuilder.FONT));
	}
	
	@Override
	protected ILogEvent getEvent()
	{
		return itsEvent;
	}

}
