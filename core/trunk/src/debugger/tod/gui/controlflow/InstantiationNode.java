/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;

import tod.core.model.event.IBehaviorEnterEvent;
import tod.core.model.event.IInstantiationEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.TypeInfo;
import tod.gui.Hyperlinks;
import zz.csg.api.IRectangularGraphicContainer;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.text.XFont;

public class InstantiationNode extends AbstractBehaviorNode
{
	private TypeInfo itsType;
	private BehaviorInfo itsConstructor;
	private Object[] itsArguments;
	private Object itsInstance;
	
	public InstantiationNode(
			CFlowView aView,
			IInstantiationEvent aInstantiationEvent,
			IBehaviorEnterEvent aBehaviorEnterEvent)
	{
		super (aView, aBehaviorEnterEvent, aInstantiationEvent);
		itsType = aInstantiationEvent.getType();
		itsInstance = aInstantiationEvent.getInstance();
		itsConstructor = aBehaviorEnterEvent.getBehavior();
	}
	
	public InstantiationNode(
			CFlowView aView,
			IInstantiationEvent aInstantiationEvent)
	{
		super (aView, null, aInstantiationEvent);
		itsType = aInstantiationEvent.getType();
		itsInstance = aInstantiationEvent.getInstance();
	}
	
	protected IRectangularGraphicContainer buildHeader()
	{
		XFont theFont = getHeaderFont();
		
		IRectangularGraphicContainer theHeader = new SVGGraphicContainer();
		theHeader.setLayoutManager(new SequenceLayout());
		
		theHeader.pChildren().add(SVGFlowText.create("new ", theFont, Color.BLACK));
		theHeader.pChildren().add(Hyperlinks.type(getGUIManager(), itsType, theFont));

		addArguments(theHeader, itsArguments, theFont);
		
		
		return theHeader;
	}
	
	protected IRectangularGraphicContainer buildFooter()
	{
		XFont theFont = getHeaderFont();

		IRectangularGraphicContainer theFooter = new SVGGraphicContainer();
		theFooter.setLayoutManager(new SequenceLayout());

		theFooter.pChildren().add(SVGFlowText.create("Instanciated: ", theFont, Color.BLACK));
		theFooter.pChildren().add(Hyperlinks.object(getGUIManager(), getEventTrace(), itsInstance, theFont));
		
		return theFooter;
	}
	
}
