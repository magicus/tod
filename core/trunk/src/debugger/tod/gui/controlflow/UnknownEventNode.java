/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;

import reflex.lib.logging.miner.gui.IGUIManager;
import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventTrace;
import tod.gui.Hyperlinks;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.figures.SVGFlowText;

public class UnknownEventNode extends AbstractEventNode
{
	private ILogEvent itsEvent;

	public UnknownEventNode(
			CFlowView aView,
			ILogEvent aEvent)
	{
		super(aView);
		
		itsEvent = aEvent;

		setLayoutManager(new SequenceLayout());
		
		pChildren().add(SVGFlowText.create("Unknown", CFlowTreeBuilder.FONT, Color.GRAY));
	}
	
	@Override
	protected ILogEvent getEvent()
	{
		return itsEvent;
	}
}
