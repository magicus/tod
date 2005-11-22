/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;

import tod.core.model.event.ILocalVariableWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.gui.Hyperlinks;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.figures.SVGFlowText;

public class LocalVariableWriteNode extends AbstractEventNode
{
	private ILocalVariableWriteEvent itsEvent;

	public LocalVariableWriteNode(
			CFlowView aView,
			ILocalVariableWriteEvent aEvent)
	{
		super(aView);
		
		itsEvent = aEvent;

		setLayoutManager(new SequenceLayout());
		
		pChildren().add(SVGFlowText.create(itsEvent.getVariable().getVariableName(), CFlowTreeBuilder.FONT, Color.BLACK));
		pChildren().add(SVGFlowText.create(" = ", CFlowTreeBuilder.FONT, Color.BLACK));
		pChildren().add(Hyperlinks.object(getGUIManager(), getEventTrace(), itsEvent.getValue(), CFlowTreeBuilder.FONT));
	}
	
	@Override
	protected ILogEvent getEvent()
	{
		return itsEvent;
	}
}
