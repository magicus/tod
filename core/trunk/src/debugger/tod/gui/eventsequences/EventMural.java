/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui.eventsequences;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.EventComparator;
import tod.core.database.event.ILogEvent;
import tod.gui.BrowserData;
import zz.utils.Cleaner;
import zz.utils.properties.ArrayListProperty;
import zz.utils.properties.IListProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.Orientation;
import zz.utils.ui.UIUtils;

/**
 * A graphic object that represents a mural (see http://reflex.dcc.uchile.cl/?q=node/60) 
 * of sequences of events.
 * @author gpothier
 */
public class EventMural extends JPanel
{
	private static final int MAX_BALOONS = 20;
	
	private static final ImageUpdater itsUpdater = new ImageUpdater();
	private final Orientation itsOrientation;
	
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
			if (itsImage != null) itsImage.setUpToDate(false);
			repaint();
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

	/**
	 * Mural image (one version per display).
	 */
	private ImageData itsImage; 
	
	private boolean itsShowBaloons = true;
	
	private Timer itsTimer;

	
	public EventMural(Orientation aOrientation)
	{
		itsOrientation = aOrientation;
		setPreferredSize(new Dimension(100, 20));
		itsTimer = new Timer(100, new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				repaint();
			}
		});
		itsTimer.setRepeats(false);
	}

	public EventMural(Orientation aOrientation, IEventBrowser aBrowser)
	{
		this(aOrientation);
		pEventBrowsers.add(new BrowserData(aBrowser, Color.BLACK));
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
		itsUpdater.request(this);
	}
	
	
	/**
	 * Whether or not event baloons should be displayed.
	 */
	public void setShowBaloons(boolean aShowBaloons)
	{
		itsShowBaloons = aShowBaloons;
	}

	/**
	 * Updates the set of displayed baloons.
	 */
	protected void updateBaloons()
	{
		removeAll();
		if (! itsShowBaloons) return;
		if (! isReady()) return;
		
		// Create the multibrowser.
		ArrayList<IEventBrowser> theBrowsers = new ArrayList<IEventBrowser>(pEventBrowsers().size());
		for (BrowserData theBrowserData : pEventBrowsers())
		{
			theBrowsers.add (theBrowserData.getBrowser());
		}
		MultiBrowser theBrowser = new MultiBrowser(theBrowsers);
		
		// Get parameters
		int w = getWidth();
		int x = 0;
		
		long t1 = pStart().get();
		long t2 = pEnd().get();
		
		if (t1 == t2) return;
		
		long t = t1;
		
		// Start placing baloons
		theBrowser.setCursor(t);
		
		SpaceManager theManager = new SpaceManager(getHeight());
		
		int i = 0;
		while (theBrowser.hasNext() && i<MAX_BALOONS)
		{
			i++;
			ILogEvent theEvent = theBrowser.next();
			t = theEvent.getTimestamp();
			if (t > t2) break;
			
			x = (int) (w * (t - t1) / (t2 - t1));
			
			Range theRange = theManager.getAvailableRange(x);
			if (theRange == null) continue;
			
			JComponent theBaloon = getBaloon(theEvent);
			
			if (theBaloon != null)
			{
				Rectangle2D theBaloonBounds = theBaloon.getBounds(null);
				
				if (theBaloonBounds.getHeight() > theRange.getSpan()) continue;

				int by = (int) theRange.getStart();
				double bw = theBaloonBounds.getWidth();
				double bh = theBaloonBounds.getHeight();
				
				theBaloon.setLocation(x, by);
				add(theBaloon);
				
				theManager.occupy(x, by, bw, bh);
			}
		}
	}
	
	/**
	 * Returns a baloon (tooltip) for the specified event.
	 * @return A Graphic object to display as a baloon next to the representation
	 * of the specified event, or null if no baloon is available.
	 */
	protected JComponent getBaloon(ILogEvent aEvent)
	{
		return null;
	}

	@Override
	protected void paintComponent(Graphics aGraphics)
	{
		// Paint mural image
		ImageData theImageData = itsImage;
		
		boolean thePaintImage;
		if (theImageData == null || ! theImageData.isUpToDate())
		{
			updateImage();
			thePaintImage = false;
		}
		else thePaintImage = true;

		int w = getWidth();
		int h = getHeight();
		
		if (thePaintImage)
		{
			aGraphics.drawImage(theImageData.getImage(), 0, 0, w, h, null);
		}
		else
		{
			int theSize = 10;
			int theX = w/2;
			int theY = h/2;
			
			long theTime = System.currentTimeMillis();
			aGraphics.setColor(Color.WHITE);
			aGraphics.fillRect(0, 0, getWidth(), getHeight());
			aGraphics.setColor((theTime/200) % 2 == 0 ? Color.BLACK : Color.LIGHT_GRAY);
			aGraphics.fillRect(theX-theSize/2, theY-theSize/2, theSize, theSize);
			
			itsTimer.start();
		}
	}

	public static void paintMural (
			Graphics2D aGraphics, 
			Rectangle aBounds,
			long aT1,
			long aT2,
			BrowserData aBrowserData,
			boolean aSum)
	{
		List<BrowserData> theData = Collections.singletonList(aBrowserData);
		paintMural(aGraphics, aBounds, aT1, aT2, theData, aSum);
	}

	/**
	 * @param aSum If true, values of each series are summed and the resulting
	 * color is a proportional mix of all colors.
	 * If false, the series are "stacked" using the painter's algorithm.
	 */
	public static void paintMural (
			Graphics2D aGraphics, 
			Rectangle aBounds,
			long aT1,
			long aT2,
			Collection<BrowserData> aBrowserData,
			boolean aSum)
	{
		if (aSum)
		{
			paintMural(aGraphics, aBounds, aT1, aT2, aBrowserData);
		}
		else
		{
			for (BrowserData theData : aBrowserData)
			{
				List<BrowserData> theList = Collections.singletonList(theData);
				paintMural(aGraphics, aBounds, aT1, aT2, theList);
			}
		}
	}
	
	public static void paintMural (
			Graphics2D aGraphics, 
			Rectangle aBounds,
			long aT1,
			long aT2,
			Collection<BrowserData> aBrowserData)
	{
		long[][] theValues = new long[aBrowserData.size()][];
		Color[] theColors = new Color[aBrowserData.size()];
		
		int i = 0;
		for (BrowserData theBrowserData : aBrowserData)
		{
			// TODO: check conversion
			theValues[i] = theBrowserData.getBrowser().getEventCounts(aT1, aT2, aBounds.width, false);
			theColors[i] = theBrowserData.getColor();
			i++;
		}
		
		paintMural(aGraphics, aBounds, theValues, theColors);
	}
	
	public static void paintMural (Graphics2D aGraphics, Rectangle aBounds, long[][] aValues, Color[] aColors)
	{
		if (aValues.length == 0) return;
		
		int theHeight = aBounds.height;
		int theY = aBounds.y;
		int bh = 4; // base height

		int theMaxT = 0;
		
		// Determine maximum value
		for (int i = 0; i < aValues[0].length; i++)
		{
			int t = 0; 
			for (int j = 0; j < aValues.length; j++) t += aValues[j][i];
			theMaxT = Math.max(theMaxT, t);
		}
		
		for (int i = 0; i < aValues[0].length; i++)
		{
			int t = 0; // Total for current column
			int r = 0;
			int g = 0;
			int b = 0;
			
			for (int j = 0; j < aValues.length; j++)
			{
				long theValue = aValues[j][i];
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
			aGraphics.fillRect(aBounds.x + i, theY+theHeight-bh, 1, bh);
			
			// Draw proportional bar
			int h = (theHeight-bh) * t / theMaxT;
			aGraphics.setColor(c2);
			aGraphics.fillRect(aBounds.x + i, theY+theHeight-bh-h, 1, h);
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
					itsNextEvents.put (theBrowser.next(), theBrowser);
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
					itsNextEvents.put (theBrowser.next(), theBrowser);
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
			if (theBrowser.hasNext()) itsNextEvents.put (theBrowser.next(), theBrowser);
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
	
	private static class ImageUpdater extends Thread
	{
		private BlockingQueue<EventMural> itsRequestsQueue =
			new LinkedBlockingQueue<EventMural>();
		
		private Set<EventMural> itsCurrentRequests =
			new HashSet<EventMural>();
		
		public ImageUpdater()
		{
			super("EventMural updater");
			start();
		}

		public synchronized void request(EventMural aMural)
		{
			try
			{
				if (! itsCurrentRequests.contains(aMural))
				{
					itsCurrentRequests.add(aMural);
					itsRequestsQueue.put(aMural);
				}
			}
			catch (InterruptedException e)
			{
			}
		}
		
		@Override
		public void run()
		{
			try
			{
				while(true)
				{
					EventMural theRequest = itsRequestsQueue.take();
					long t0 = System.currentTimeMillis();
					System.out.println("[EventMural.Updater] Processing request: "+theRequest);
					try
					{
						doUpdateImage(theRequest);
					}
					catch (Exception e)
					{
						System.err.println("Exception in EventMural.Updater:");
						e.printStackTrace();
					}
					long t1 = System.currentTimeMillis();
					float t = (t1-t0)/1000f;
					System.out.println("[EventMural.Updater] Finished request in "+t+"s.");
					
					itsCurrentRequests.remove(theRequest);
				}
			}
			catch (InterruptedException e)
			{
			}
		}
		
		protected void doUpdateImage(EventMural aMural)
		{
			int width = aMural.getWidth();
			int height = aMural.getHeight();
			if (height == 0 || width == 0) return;
			
			width = aMural.itsOrientation.getU(width, height);
			height = aMural.itsOrientation.getV(width, height);

			ImageData theImageData = aMural.itsImage;
			BufferedImage theImage = theImageData != null ? theImageData.getImage() : null;
			if (theImage == null 
					|| theImage.getWidth() != width 
					|| theImage.getHeight() != height) 
			{
				GraphicsConfiguration theConfiguration = aMural.getGraphicsConfiguration();
				theImage = theConfiguration.createCompatibleImage(width, height);
				theImageData = new ImageData(theImage);
				aMural.itsImage = theImageData;
			}
			
			Graphics2D theGraphics = theImage.createGraphics();
			if (aMural.itsOrientation == Orientation.VERTICAL)
			{
				AffineTransform theTransform = new AffineTransform();
				theTransform.translate(height, 0);
				theTransform.rotate(Math.PI/2);
				
				theGraphics.transform(theTransform);
			}
			theGraphics.setColor(Color.WHITE);
			theGraphics.fillRect(0, 0, width, height);
			Long theStart = aMural.pStart().get();
			Long theEnd = aMural.pEnd().get();
			
			paintMural(
					theGraphics, 
					new Rectangle(0, 0, width, height), 
					theStart, 
					theEnd, 
					aMural.pEventBrowsers(),
					false);

			theImageData.setUpToDate(true);
			aMural.repaint();
		}
	}
	
	private static class ImageData
	{
		private BufferedImage itsImage;
		private boolean itsUpToDate;
		
		public ImageData(BufferedImage aImage)
		{
			itsImage = aImage;
			itsUpToDate = false;
		}

		public BufferedImage getImage()
		{
			return itsImage;
		}

		public void setImage(BufferedImage aImage)
		{
			itsImage = aImage;
		}

		public boolean isUpToDate()
		{
			return itsUpToDate;
		}

		public void setUpToDate(boolean aUpToDate)
		{
			itsUpToDate = aUpToDate;
		}
		
		
	}
}
