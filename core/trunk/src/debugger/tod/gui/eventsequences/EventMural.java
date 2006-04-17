/*
 * Created on Oct 4, 2005
 */
package tod.gui.eventsequences;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import tod.core.model.event.EventComparator;
import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventBrowser;
import tod.gui.BrowserData;
import zz.csg.api.GraphicObjectContext;
import zz.csg.api.IDisplay;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.impl.SVGGraphicContainer;
import zz.utils.Cleaner;
import zz.utils.properties.ArrayListProperty;
import zz.utils.properties.IListProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.UIUtils;

/**
 * A graphic object that represents a mural (see http://reflex.dcc.uchile.cl/?q=node/60) 
 * of sequences of events.
 * @author gpothier
 */
public class EventMural extends SVGGraphicContainer
{
	private IRWProperty<Long> pStart = new SimpleRWProperty<Long>(this)
	{
		@Override
		protected void changed(Long aOldValue, Long aNewValue)
		{
			markDirty();
		}
	};
	
	private IRWProperty<Long> pEnd = new SimpleRWProperty<Long>(this)
	{
		@Override
		protected void changed(Long aOldValue, Long aNewValue)
		{
			markDirty();
		}
	};
	
	private IListProperty<BrowserData> pEventBrowsers = new ArrayListProperty<BrowserData>(this)
	{
		@Override
		protected void contentChanged()
		{
			markDirty();
		}
	};
	
	private Cleaner itsImageCleaner = new Cleaner()
	{
		@Override
		protected void clean()
		{
			updateImage();
		}
	};
	
	private Cleaner itsBaloonsCleaner = new Cleaner()
	{
		@Override
		protected void clean()
		{
			updateBaloons();
		}
	};
	
	private BufferedImage itsImage;
	private IDisplay itsDisplay;
	
	/**
	 * Constructs a new mural
	 * @param aDisplay The display that will show this mural
	 */
	public EventMural(IDisplay aDisplay)
	{
		itsDisplay = aDisplay;
	}

	/**
	 * Returns the property that contains the starting timestamp of 
	 * the displayed time range.
	 */
	public IRWProperty<Long> pStart ()
	{
		return pStart;
	}
	
	/**
	 * Returns the property that contains the ending timestamp of 
	 * the displayed time range.
	 */
	public IRWProperty<Long> pEnd ()
	{
		return pEnd;
	}
	
	/**
	 * Returns the property that contains the list of event browsers
	 * displayed in this timescale.
	 */
	public IListProperty<BrowserData> pEventBrowsers ()
	{
		return pEventBrowsers;
	}
	
	/**
	 * Returns true only if all required information is available (bounds, range, etc).
	 */
	private boolean isReady()
	{
		return pStart().get() != null 
			&& pEnd().get() != null;
	}
	
	private void markDirty()
	{
		itsImageCleaner.markDirty();
		itsBaloonsCleaner.markDirty();
	}
	
	/**
	 * Updates the timescale image.
	 */
	protected void updateImage()
	{
		if (! isReady()) return;
		
		Rectangle2D theBounds = pBounds().get();
		Rectangle thePixelBounds = itsDisplay.localToPixel(null, this, theBounds);

		if (thePixelBounds.height == 0 || thePixelBounds.width == 0) return;

		if (itsImage == null) 
		{
			GraphicsConfiguration theConfiguration = itsDisplay.getGraphicsConfiguration();
			itsImage = theConfiguration.createCompatibleImage(thePixelBounds.width, thePixelBounds.height);
		}
		
		thePixelBounds.setLocation(0, 0);
		Graphics2D theGraphics = itsImage.createGraphics();
		theGraphics.setColor(Color.WHITE);
		theGraphics.fill(thePixelBounds);
		paintMural(theGraphics, thePixelBounds, pStart().get(), pEnd().get(), pEventBrowsers());
		repaintAllContexts();
	}
	
	/**
	 * Updates the set of displayed baloons.
	 */
	protected void updateBaloons()
	{
		pChildren().clear();
		if (! isReady()) return;
		
		// Create the multibrowser.
		ArrayList<IEventBrowser> theBrowsers = new ArrayList<IEventBrowser>(pEventBrowsers().size());
		for (BrowserData theBrowserData : pEventBrowsers())
		{
			theBrowsers.add (theBrowserData.getBrowser());
		}
		MultiBrowser theBrowser = new MultiBrowser(theBrowsers);
		
		// Get parameters
		Rectangle2D theBounds = pBounds().get();
		
		double w = theBounds.getWidth();
		double x = 0;
		
		long t1 = pStart().get();
		long t2 = pEnd().get();
		long t = t1;
		
		// Start placing baloons
		theBrowser.setCursor(t);
		
		SpaceManager theManager = new SpaceManager(theBounds.getHeight());
		
		while (theBrowser.hasNext())
		{
			ILogEvent theEvent = theBrowser.next();
			t = theEvent.getTimestamp();
			if (t > t2) break;
			
			x = w * (t - t1) / (t2 - t1);
			
			Range theRange = theManager.getAvailableRange(x);
			if (theRange == null) continue;
			
			IRectangularGraphicObject theBaloon = getBaloon(theEvent);
			
			if (theBaloon != null)
			{
				Rectangle2D theBaloonBounds = theBaloon.getBounds(null);
				
				if (theBaloonBounds.getHeight() > theRange.getSpan()) continue;

				double by = theRange.getStart();
				double bw = theBaloonBounds.getWidth();
				double bh = theBaloonBounds.getHeight();
				
				theBaloon.pBounds().set(new Rectangle2D.Double(x, by, bw, bh));
				pChildren().add(theBaloon);
				
				theManager.occupy(x, by, bw, bh);
			}
		}
	}
	
	/**
	 * Returns a baloon (tooltip) for the specified event.
	 * @return A Graphic object to display as a baloon next to the representation
	 * of the specified event, or null if no baloon is available.
	 */
	protected IRectangularGraphicObject getBaloon(ILogEvent aEvent)
	{
		return null;
	}

	@Override
	protected void changed(IRWProperty aProperty)
	{
		super.changed(aProperty);
		if (aProperty == pBounds())
		{
			itsImage = null;
			markDirty();
		}
	}
	
	@Override
	protected void paintBackground(GraphicObjectContext aContext, Graphics2D aGraphics, Area aVisibleArea)
	{
		// Paint mural image
		if (itsImage != null)
		{
			Rectangle2D theBounds = pBounds().get();
			int w = (int) theBounds.getWidth();
			int h = (int) theBounds.getHeight();
			aGraphics.drawImage(itsImage, 0, 0, w, h, null);
		}
	}

	public static void paintMural (
			Graphics2D aGraphics, 
			Rectangle aBounds,
			long aT1,
			long aT2,
			BrowserData aBrowserData)
	{
		paintMural(aGraphics, aBounds, aT1, aT2, Collections.singletonList(aBrowserData));
	}

	public static void paintMural (
			Graphics2D aGraphics, 
			Rectangle aBounds,
			long aT1,
			long aT2,
			Collection<BrowserData> aBrowserData)
	{
		int[][] theValues = new int[aBrowserData.size()][];
		Color[] theColors = new Color[aBrowserData.size()];
		
		int i = 0;
		for (BrowserData theBrowserData : aBrowserData)
		{
			theValues[i] = theBrowserData.getBrowser().getEventCounts(aT1, aT2, aBounds.width);
			theColors[i] = theBrowserData.getColor();
			i++;
		}
		
		paintMural(aGraphics, aBounds, theValues, theColors);
	}
	
	public static void paintMural (Graphics2D aGraphics, Rectangle aBounds, int[][] aValues, Color[] aColors)
	{
		int theH = aBounds.height;
		int theY = aBounds.y;
		int k = 4;
		
		for (int i = 0; i < aValues[0].length; i++)
		{
			int t = 0; // Total for current column
			int r = 0;
			int g = 0;
			int b = 0;
			
			for (int j = 0; j < aValues.length; j++)
			{
				int theValue = aValues[j][i];
				Color theColor = aColors[j];
				
				t += theValue;
				r += theValue * theColor.getRed();
				g += theValue * theColor.getGreen();
				b += theValue * theColor.getBlue();
			}
			
			if (t == 0) continue;
			
			Color c1 = new Color(r/t, g/t, b/t);
			Color c2 = UIUtils.getLighterColor(c1);

			// Draw main bar
			aGraphics.setColor(c1);
			aGraphics.fillRect(aBounds.x + i, theY, 1, theH);
			
			// Draw proportional bar
			int h = theH - ((theH * k)/(t+k));
			aGraphics.setColor(c2);
			aGraphics.fillRect(aBounds.x + i, theY + (theH - h)/2, 1, h);
		}
	}
	
	/**
	 * Helper class that permits to obtain a sequence of events from 
	 * multiple browsers
	 * @author gpothier
	 */
	private static class MultiBrowser
	{
		private List<IEventBrowser> itsBrowsers;
		
		private Set<IEventBrowser> itsNonEmptyBrowsers = new HashSet<IEventBrowser>();
		
		private SortedMap<ILogEvent, IEventBrowser> itsNextEvents = 
			new TreeMap<ILogEvent, IEventBrowser>(EventComparator.getInstance());
		
		public MultiBrowser(List<IEventBrowser> aBrowsers)
		{
			itsBrowsers = aBrowsers;
		}

		public void setCursor(long aT)
		{
			if (itsNextEvents.isEmpty())
			{
				init(aT);
				return;
			}
			
			ILogEvent theFirstEvent = itsNextEvents.firstKey();
			ILogEvent theLastEvent = itsNextEvents.lastKey();
			
			if (aT < theFirstEvent.getTimestamp())
			{
				init(aT);
			}
			else if (aT > theLastEvent.getTimestamp())
			{
				skip(aT);
			}
			else
			{
				// already in map
				ILogEvent theEvent;
				do
				{
					theEvent = peekNext();
					if (theEvent.getTimestamp() < aT) consume(theEvent);
					else break;
				} while (true);
			}
		}
		
		private void init(long aT)
		{
			itsNextEvents.clear();
			for (IEventBrowser theBrowser : itsBrowsers)
			{
				theBrowser.setNextTimestamp(aT);
				if (theBrowser.hasNext())
				{
					itsNonEmptyBrowsers.add(theBrowser);
					itsNextEvents.put (theBrowser.getNext(), theBrowser);
				}
			}
		}
		
		private void skip(long aT)
		{
			itsNextEvents.clear();
			for (Iterator<IEventBrowser> theIterator = itsNonEmptyBrowsers.iterator();theIterator.hasNext();)
			{
				IEventBrowser theBrowser = theIterator.next();
				
				theBrowser.setNextTimestamp(aT);
				if (theBrowser.hasNext())
				{
					itsNextEvents.put (theBrowser.getNext(), theBrowser);
				}
				else theIterator.remove();
			}
		}
		
		/**
		 * Determine if there are more events in this browser
		 */
		public boolean hasNext()
		{
			return ! itsNextEvents.isEmpty();
		}

		/**
		 * Returns the next event and advances the cursor.
		 */
		public ILogEvent next()
		{
			ILogEvent theEvent = peekNext();
			consume(theEvent);
			
			return theEvent;
		}
		
		private ILogEvent peekNext()
		{
			return itsNextEvents.firstKey();
		}
		
		private void consume(ILogEvent aEvent)
		{
			IEventBrowser theBrowser = itsNextEvents.remove(aEvent);
			if (theBrowser.hasNext()) itsNextEvents.put (theBrowser.getNext(), theBrowser);
			else itsNonEmptyBrowsers.remove(theBrowser);
		}
	}

	/**
	 * This class permits to place baloons according to available space.
	 * TODO: Implement a better algorithm, for now we use discrete scanlines.
	 * @author gpothier
	 */
	private static class SpaceManager
	{
		private static double K = 4.0;
		private double itsHeight;
		private double[] itsLines;
		
		public SpaceManager(double aHeight)
		{
			itsHeight = aHeight;
			itsLines = new double[(int) (itsHeight/K)];
			
			for (int i = 0; i < itsLines.length; i++) itsLines[i] = -1;
		}

		/**
		 * Returns the biggest available range at the specified position.
		 * @return A {@link Range}, or null if there is no space.
		 */
		public Range getAvailableRange (double aX)
		{
			Range theBiggestRange = null;
			double theStart = -1;
			double theEnd = -1;
			
			for (int i = 0; i < itsLines.length; i++)
			{
				double x = itsLines[i];
				
				if (theStart < 0)
				{
					if (x < aX) theStart = i*K;
				}
				else
				{
					if (x < aX) theEnd = i*K;
					else
					{
						Range theRange = new Range(theStart, theEnd);
						if (theBiggestRange == null || theRange.getSpan() > theBiggestRange.getSpan())
							theBiggestRange = theRange;
						
						theStart = theEnd = -1;
					}
				}
			}
			
			if (theBiggestRange == null && theStart >= 0)
			{
				theBiggestRange = new Range(theStart, theEnd);
			}
			
			return theBiggestRange;
		}
		
		/**
		 * Marks the given bounds as occupied 
		 */
		public void occupy(double aX, double aY, double aW, double aH)
		{
			double theY1 = aY;
			double theY2 = aY+aH;
			
			int theI1 = (int) (theY1 / K);
			int theI2 = (int) (theY2 / K);
			
			for (int i=theI1;i<=theI2;i++) itsLines[i] = aX+aW;
		}
		
	}
	
	private static class Range
	{
		private double itsStart;
		private double itsEnd;
		
		public Range(double aStart, double aEnd)
		{
			itsStart = aStart;
			itsEnd = aEnd;
		}
		
		public double getEnd()
		{
			return itsEnd;
		}

		public double getStart()
		{
			return itsStart;
		}

		public boolean intersects (Range aRange)
		{
			return aRange.getStart() <= getEnd() || getStart() <= aRange.getEnd();
		}
		
		public boolean contains (Range aRange)
		{
			return aRange.getStart() >= getStart() && aRange.getEnd() <= getEnd();			
		}
		
		public double getSpan()
		{
			return itsEnd - itsStart;
		}
	}
}
