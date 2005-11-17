/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;

import tod.core.model.event.IConstructorChainingEvent;
import zz.csg.api.IRectangularGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.text.XFont;

public class ConstructorChainingNode extends AbstractBehaviorNode
{
	public ConstructorChainingNode(
			CFlowView aView,
			IConstructorChainingEvent aEvent)
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
				getEvent().hasThrown() ? Color.RED : Color.BLACK));

		super.fillHeader(aContainer);
	}
}
