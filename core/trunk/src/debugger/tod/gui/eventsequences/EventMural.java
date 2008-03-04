/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JPanel;
import javax.swing.Timer;

import tod.core.database.browser.IEventBrowser;
import tod.gui.BrowserData;
import tod.utils.TODUtils;
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
	
	/**
	 * Mural image (one version per display).
	 */
	private ImageData itsImage; 
	
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
		
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent aE)
			{
				markDirty();
			}
		});
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
	protected boolean isReady()
	{
		return pStart().get() != null 
			&& pEnd().get() != null;
	}
	
	protected void markDirty()
	{
		itsImageCleaner.markDirty();
		System.out.println("[EventMural] markDirty");
	}
	
	/**
	 * Updates the timescale image.
	 */
	protected void updateImage()
	{
		System.out.println("[EventMural] updateImage()");
		if (! isReady()) return;
		System.out.println("[EventMural] updateImage - requesting");
		itsUpdater.request(this);
	}
	
	
	@Override
	protected void paintComponent(Graphics aGraphics)
	{
		if (pEventBrowsers().isEmpty()) 
		{
			super.paintComponent(aGraphics);
			return;
		}
		
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
			BufferedImage theImage = theImageData.getImage();
			aGraphics.drawImage(theImage, 0, 0, w, h, null);
			if (theImage.getWidth() != w || theImage.getHeight() != h) markDirty();
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
		if (aT1 == aT2) return;
		long[][] theValues = new long[aBrowserData.size()][];
		Color[] theColors = new Color[aBrowserData.size()];
		
		int i = 0;
		for (BrowserData theBrowserData : aBrowserData)
		{
			// TODO: check conversion
			System.out.println("[EventMural] Requesting counts: "+aT1+"-"+aT2);
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
					System.out.println("[ImageUpdater] adding request");
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
					TODUtils.log(1,"[EventMural.Updater] Processing request: "+theRequest);
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
					TODUtils.log(1,"[EventMural.Updater] Finished request in "+t+"s.");
					
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
			
			int u = aMural.itsOrientation.getU(width, height);
			int v = aMural.itsOrientation.getV(width, height);
			
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
				theTransform.translate(v, 0);
				theTransform.rotate(Math.PI/2);
				
				theGraphics.transform(theTransform);
			}
			theGraphics.setColor(Color.WHITE);
			theGraphics.fillRect(0, 0, u, v);
			Long theStart = aMural.pStart().get();
			Long theEnd = aMural.pEnd().get();
			
			System.out.println("[ImageUpdater] doUpdateImage ["+theStart+"-"+theEnd+"]");
			
			paintMural(
					theGraphics, 
					new Rectangle(0, 0, u, v), 
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
