/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;

import tod.core.model.event.IInstantiationEvent;
import tod.gui.Hyperlinks;
import zz.csg.api.IRectangularGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.text.XFont;

public class InstantiationNode extends AbstractBehaviorNode
{
	public InstantiationNode(
			CFlowView aView,
			IInstantiationEvent aInstantiationEvent)
	{
		super (aView, aInstantiationEvent);
	}
	
	
	@Override
	protected IInstantiationEvent getEvent()
	{
		return (IInstantiationEvent) super.getEvent();
	}
	
	@Override
	protected void fillHeader(IRectangularGraphicContainer aContainer)
	{
		XFont theFont = getHeaderFont();
		
		aContainer.pChildren().add(SVGFlowText.create("new ", theFont, Color.BLACK));
		aContainer.pChildren().add(Hyperlinks.type(getGUIManager(), getEvent().getType(), theFont));

		addArguments(aContainer, getEvent().getArguments(), theFont);
	}
	
	@Override
	protected void fillFooter(IRectangularGraphicContainer aContainer)
	{
		XFont theFont = getHeaderFont();

		if (getEvent().hasThrown())
		{
			aContainer.pChildren().add(SVGFlowText.create("Thrown ", theFont, Color.RED));
			
			aContainer.pChildren().add(Hyperlinks.object(
					getGUIManager(), 
					getEventTrace(), 
					getEvent().getResult(),
					theFont));
		}
		else
		{
			aContainer.pChildren().add(SVGFlowText.create("Instanciated ", theFont, Color.BLACK));

			aContainer.pChildren().add(Hyperlinks.object(
					getGUIManager(), 
					getEventTrace(), 
					getEvent().getInstance(),
					theFont));
		}
		
	}
	
}
