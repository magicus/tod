/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;

import reflex.lib.logging.miner.gui.IGUIManager;
import tod.core.model.event.IParentEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventTrace;
import tod.gui.Hyperlinks;
import zz.csg.api.IGraphicContainer;
import zz.csg.api.IRectangularGraphicContainer;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.AbstractSimpleLayout;
import zz.csg.api.layout.StackLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.PropertyUtils;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.text.XFont;

public abstract class AbstractBehaviorNode extends AbstractEventNode
{
	private IParentEvent itsContainerEvent;
	private ILogEvent itsMainEvent;
	
	private IRectangularGraphicContainer itsChildrenContainer;
	private IRectangularGraphicContainer itsHeader;
	private IRectangularGraphicContainer itsFooter;
	private ExpanderWidget itsExpanderWidget;
	
	private IRWProperty<Boolean> pExpanded = new SimpleRWProperty<Boolean>(this, false)
	{
		@Override
		protected void changed(Boolean aOldValue, Boolean aNewValue)
		{
			if (aNewValue) expand();
			else collapse();
		}
	};
	
	public AbstractBehaviorNode(
			CFlowView aView,
			IParentEvent aContainerEvent,
			ILogEvent aMainEvent)
	{
		super (aView);
		
		itsContainerEvent = aContainerEvent;
		itsMainEvent = aMainEvent;
		
		itsExpanderWidget = new ExpanderWidget();
		pChildren().add(itsExpanderWidget);
		
		PropertyUtils.connect(pExpanded(), itsExpanderWidget.pExpanded(), true);
		
		itsChildrenContainer = new SVGGraphicContainer();
		itsChildrenContainer.setLayoutManager(new StackLayout());
		pChildren().add(itsChildrenContainer);
	}
	
	@Override
	protected final ILogEvent getMainEvent()
	{
		return itsMainEvent;
	}
	
	@Override
	protected boolean validate()
	{
		super.validate();
		
		itsHeader = buildHeader();
		pChildren().add(itsHeader);
		
		itsFooter = buildFooter();
		pChildren().add(itsFooter);
		
		setLayoutManager(new MyLayout());
		
		return true;
	}

	/**
	 * Whether this node is expanded or collapsed.
	 */
	public IRWProperty<Boolean> pExpanded()
	{
		return pExpanded;
	}
	
	private void expand()
	{
		List<IRectangularGraphicObject> theNodes = getBuilder().buildNodes(itsContainerEvent);
		
		for (IRectangularGraphicObject theNode : theNodes)
		{
			itsChildrenContainer.pChildren().add(theNode);
		}
		
		repaintAllContexts();
	}
	
	private void collapse()
	{
		itsChildrenContainer.pChildren().clear();		
		repaintAllContexts();
	}
	
	protected abstract IRectangularGraphicContainer buildHeader();
	
	/**
	 * Returns the font size that should be used for headers.
	 * The font is bigger when the node is expanded.
	 */
	protected XFont getHeaderFont()
	{
		return pExpanded().get() ? CFlowTreeBuilder.HEADER_FONT : CFlowTreeBuilder.FONT;
	}
	
	/**
	 * Adds the hyperlinks representing the behavior's arguments to the given container.
	 * Can be used for the implementation of {@link #buildHeader()}
	 */
	protected void addArguments(IGraphicContainer aContainer, Object[] aArguments, XFont aFont)
	{
		aContainer.pChildren().add(SVGFlowText.create("(", aFont, Color.BLACK));
		
		if (aArguments != null)
		{
			boolean theFirst = true;
			for (Object theArgument : aArguments)
			{
				if (theFirst) theFirst = false;
				else aContainer.pChildren().add(SVGFlowText.create(", ", aFont, Color.BLACK));
				
				aContainer.pChildren().add(Hyperlinks.object(
						getGUIManager(),
						getEventTrace(),
						theArgument, 
						aFont));
			}
		}
		else
		{
			aContainer.pChildren().add(SVGFlowText.create("...", aFont, Color.BLACK));
		}
		
		aContainer.pChildren().add(SVGFlowText.create(")", aFont, Color.BLACK));
	}
	
	protected abstract IRectangularGraphicContainer buildFooter();
	
	private class MyLayout extends AbstractSimpleLayout
	{

		@Override
		protected void layout(IGraphicContainer aContainer)
		{
			assert aContainer == AbstractBehaviorNode.this;
			
			double theY = 0;

			if (itsHeader != null)
			{
				itsHeader.pBounds().setPosition(ExpanderWidget.WIDTH, theY);
				theY += itsHeader.pBounds().get().getHeight();
			}				
			
			itsChildrenContainer.pBounds().setPosition(ExpanderWidget.WIDTH + 10, theY);
			theY += itsChildrenContainer.pBounds().get().getHeight();
			
			if (itsFooter != null)
			{
				itsFooter.pBounds().setPosition(ExpanderWidget.WIDTH, theY);
				theY += itsFooter.pBounds().get().getHeight();
			}
			
			itsExpanderWidget.pBounds().set(0, 0, ExpanderWidget.WIDTH, theY);
			
			Rectangle2D theBounds = computeBounds(null);
			resize(theBounds.getWidth(), theBounds.getHeight());
		}
		
	}
	
}
