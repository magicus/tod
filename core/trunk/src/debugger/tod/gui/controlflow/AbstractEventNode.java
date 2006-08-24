/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import tod.core.model.browser.ILogBrowser;
import tod.core.model.event.ILogEvent;
import tod.gui.IGUIManager;
import zz.csg.api.GraphicObjectContext;
import zz.csg.impl.SVGGraphicContainer;

public abstract class AbstractEventNode extends SVGGraphicContainer
{
	private CFlowView itsView;

	public AbstractEventNode(CFlowView aView)
	{
		itsView = aView;
	}

	public CFlowTreeBuilder getBuilder()
	{
		return itsView.getBuilder();
	}

	public IGUIManager getGUIManager()
	{
		return itsView.getGUIManager();
	}

	public ILogBrowser getEventTrace()
	{
		return itsView.getTrace();
	}
	
	public CFlowView getView()
	{
		return itsView;
	}
	
	@Override
	public boolean mousePressed(GraphicObjectContext aContext, MouseEvent aEvent, Point2D aPoint)
	{
		ILogEvent theMainEvent = getEvent();
		if (theMainEvent != null)
		{
			getView().selectEvent(theMainEvent);
			return true;			
		}
		else return false;
	}
	
	@Override
	protected void paintBackground(GraphicObjectContext aContext, Graphics2D aGraphics, Area aVisibleArea)
	{
		ILogEvent theMainEvent = getEvent();
		if (theMainEvent != null && getView().isEventSelected(theMainEvent))
		{
			aGraphics.setColor(Color.YELLOW);
			aGraphics.fill(aVisibleArea.getBounds2D());
		}
	}

	/**
	 * Returns the event that corresponds to this node.
	 */
	protected abstract ILogEvent getEvent();

	/**
	 * Searches the node that corresponds to the given event in this node's
	 * hierarchy.
	 */
	public AbstractEventNode getNode(ILogEvent aEvent)
	{
		if (aEvent == getEvent()) return this;
		else return null;
	}
	
	public void expand()
	{
	}

	public void collapse()
	{
	}

}
