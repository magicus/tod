/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;

import tod.core.model.event.IMethodCallEvent;
import zz.csg.api.IRectangularGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.text.XFont;

public class MethodCallNode extends AbstractBehaviorNode
{
	public MethodCallNode(
			CFlowView aView,
			IMethodCallEvent aEvent)
	{
		super (aView, aEvent);
	}

	@Override
	protected void fillHeader(IRectangularGraphicContainer aContainer)
	{
		XFont theFont = getHeaderFont();
		
		aContainer.pChildren().add(SVGFlowText.create(
				"call to ", 
				theFont, 
				getEvent().getExitEvent().hasThrown() ? Color.RED : Color.BLACK));

		super.fillHeader(aContainer);
	}
}
