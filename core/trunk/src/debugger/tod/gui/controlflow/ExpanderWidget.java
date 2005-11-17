/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import zz.csg.api.GraphicObjectContext;
import zz.csg.impl.AbstractRectangularGraphicObject;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * A widget that permits to expand/collapse a section of a tree
 * @author gpothier
 */
public class ExpanderWidget extends AbstractRectangularGraphicObject
{
	public static final double WIDTH = 10;
	
	private final IRWProperty<Boolean> pExpanded = new SimpleRWProperty<Boolean>(this, false)
	{
		@Override
		protected void changed(Boolean aOldValue, Boolean aNewValue)
		{
			repaintAllContexts();
		}
	};
	
	private Color itsColor;
	private double itsThickness;
	
	public ExpanderWidget(Color aColor, double aThickness)
	{
		itsColor = aColor;
		itsThickness = aThickness;
	}

	/**
	 * This property reflects the expanded/collapsed state of this widget.
	 */
	public IRWProperty<Boolean> pExpanded()
	{
		return pExpanded;
	}
	
	public void paint(GraphicObjectContext aContext, Graphics2D aGraphics, Area aVisibleArea)
	{
		Rectangle2D theBounds = pBounds().get();
		aGraphics.setColor(itsColor);
		aGraphics.fill(new Rectangle2D.Double(
				theBounds.getX() + theBounds.getWidth()/2 - itsThickness/2, 
				theBounds.getY() + 1,
				itsThickness,
				theBounds.getHeight() - 2));
	}
	
	@Override
	public boolean mousePressed(GraphicObjectContext aContext, MouseEvent aEvent, Point2D aPoint)
	{
		pExpanded().set(! pExpanded().get());
		return true;
	}
	
}
