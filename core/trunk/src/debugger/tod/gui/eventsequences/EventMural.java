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
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.StyledEditorKit.FontSizeAction;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.LocationUtils;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.gui.BrowserData;
import tod.gui.FontConfig;
import tod.gui.GUIUtils;
import tod.gui.IGUIManager;
import tod.gui.Resources;
import tod.gui.formatter.EventFormatter;
import tod.utils.TODUtils;
import zz.utils.Cleaner;
import zz.utils.notification.IEvent;
import zz.utils.notification.IFireableEvent;
import zz.utils.notification.SimpleEvent;
import zz.utils.properties.ArrayListProperty;
import zz.utils.properties.IListProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.GridStackLayout;
import zz.utils.ui.MouseModifiers;
import zz.utils.ui.MouseWheelPanel;
import zz.utils.ui.NullLayout;
import zz.utils.ui.Orientation;
import zz.utils.ui.UIUtils;
import zz.utils.ui.ResourceUtils.ImageResource;

/**
 * A graphic object that represents a mural (see http://reflex.dcc.uchile.cl/?q=node/60) 
 * of sequences of events.
 * @author gpothier
 */
public class EventMural extends MouseWheelPanel
{
	private static final ImageUpdater itsUpdater = new ImageUpdater();
	private final Orientation itsOrientation;
	
	/**
	 * The first timestamp of the displayed time range.
	 */
	public final IRWProperty<Long> pStart = new SimpleRWProperty<Long>(this)
	{
		@Override
		protected void changed(Long aOldValue, Long aNewValue)
		{
			markDirty();
		}
	};
	
	/**
	 * The last timestamp of the displayed time range.
	 */
	public final IRWProperty<Long> pEnd = new SimpleRWProperty<Long>(this)
	{
		@Override
		protected void changed(Long aOldValue, Long aNewValue)
		{
			markDirty();
		}
	};
	
	/**
	 * The list of event browsers displayed in this mural.
	 */
	public final IListProperty<BrowserData> pEventBrowsers = new ArrayListProperty<BrowserData>(this)
	{
		@Override
		protected void contentChanged()
		{
			markDirty();
		}
	};
	
	/**
	 * This event is fired when the mural receices a click and has no 
	 * associated action to perform.
	 */
	public final IEvent<MouseEvent> eClicked = new SimpleEvent<MouseEvent>(); 

	/**
	 * This event is fired when an event is clicked.
	 */
	public final IEvent<ILogEvent> eEventClicked = new SimpleEvent<ILogEvent>();
	
	private Cleaner itsImageCleaner = new Cleaner()
	{
		@Override
		protected void clean()
		{
			if (itsImage != null) itsImage.setUpToDate(false);
			repaint();
		}
	};
	
	private final IGUIManager itsGUIManager;
	
	/**
	 * Mural image (one version per display).
	 */
	private ImageData itsImage; 
	
	/**
	 * This timer permits to animate the mural while counts are being retrieved
	 * from the db.
	 */
	private Timer itsTimer;

	/**
	 * The event whose details are currently displayed
	 */
	private ILogEvent itsCurrentEvent;
	private EventDetailsPanel itsCurrentEventPanel;
	
	public EventMural(IGUIManager aGUIManager, Orientation aOrientation)
	{
		super(new NullLayout());
		itsGUIManager = aGUIManager;
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
		
		itsCurrentEventPanel = new EventDetailsPanel();
		itsCurrentEventPanel.setVisible(false);
		add(itsCurrentEventPanel);
	}

	public EventMural(IGUIManager aGUIManager, Orientation aOrientation, IEventBrowser aBrowser)
	{
		this(aGUIManager, aOrientation);
		pEventBrowsers.add(new BrowserData(aBrowser));
	}

	/**
	 * Returns true only if all required information is available (bounds, range, etc).
	 */
	protected boolean isReady()
	{
		return pStart.get() != null 
			&& pEnd.get() != null;
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
		if (pEventBrowsers.isEmpty()) 
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
	
	protected IFireableEvent<MouseEvent> eClicked()
	{
		return (IFireableEvent<MouseEvent>) eClicked;
	}
	
	protected IFireableEvent<ILogEvent> eEventClicked()
	{
		return (IFireableEvent<ILogEvent>) eEventClicked;
	}
	
	private ILogEvent getEventAt(int aX)
	{
		if (! itsImage.isUpToDate()) return null;
		
		Long t1 = pStart.get();
		Long t2 = pEnd.get();
		
		long w = t2-t1;
		
		// The timestamp corresponding to the mouse cursor
		long t = t1+(long)(1f*w*aX/getWidth());
		long d = (long)(5f*w/getWidth());

		return getEventAt(t, d);
	}
	
	/**
	 * Returns the event for which details should be displayed at the specified timestamp.
	 * Returns null by default.
	 * @param aTimestamp The timestamp of the event to retrieve
	 * @param aTolerance The allowed tolerance for the actual event timestamp
	 */
	protected ILogEvent getEventAt(long aTimestamp, long aTolerance)
	{
		return null;
	}
	
	private void updateEventInfo(int aX)
	{
		setCurrentEvent(getEventAt(aX));
	}
	
	public void setCurrentEvent(ILogEvent aEvent)
	{
		if (aEvent == itsCurrentEvent) return;
		itsCurrentEvent = aEvent;
		
		if (itsCurrentEvent == null) itsCurrentEventPanel.setVisible(false);
		else
		{
			// Find out event position.
			long t = itsCurrentEvent.getTimestamp();
			Long t1 = pStart.get();
			Long t2 = pEnd.get();
			
			long w = t2-t1;

			int theX = (int) ((t-t1)*getWidth()/w);
			
			itsCurrentEventPanel.setEvent(itsCurrentEvent);
			Dimension theSize = itsCurrentEventPanel.getPreferredSize();
			if (theX+theSize.width > getWidth()) theX = getWidth()-theSize.width;
			if (theX < 0) theX = 0;
			itsCurrentEventPanel.setBounds(theX, 5, theSize.width, theSize.height);
			itsCurrentEventPanel.setVisible(true);
		}
		repaint();
	}
	
	@Override
	public void mouseMoved(MouseEvent aE)
	{
		if (MouseModifiers.getModifiers(aE) == MouseModifiers.CTRL)
		{
			updateEventInfo(aE.getX());
		}
		else setCurrentEvent(null);
	}

	@Override
	public void mouseExited(MouseEvent aE)
	{
		setCurrentEvent(null);
	}
	
	@Override
	public void mousePressed(MouseEvent aE)
	{
		MouseModifiers theModifiers = MouseModifiers.getModifiers(aE);
		if (theModifiers == MouseModifiers.CTRL)
		{
			updateEventInfo(aE.getX());
			if (itsCurrentEvent != null) eEventClicked().fire(itsCurrentEvent);
		}
		else eClicked().fire(aE);
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent aE)
	{
		System.out.println(".mouseWheelMoved: "+aE.getWheelRotation());
		if (MouseModifiers.getModifiers(aE) == MouseModifiers.CTRL)
		{
			zoom(-aE.getWheelRotation(), aE.getX());
		}
	}

	protected void zoom(int aAmount, int aX)
	{
		if (aAmount == 0) return;
		System.out.println("Amount: "+aAmount);
		
		Long t1 = pStart.get();
		Long t2 = pEnd.get();
		
		long w = t2-t1;
		
		// The timestamp corresponding to the mouse cursor
		long t = t1+(long)(1f*w*aX/getWidth());
		
		float k = (float) Math.pow(2, -0.5*aAmount);
		long nw = (long) (k*w);
			
		long s = t - ((long) ((t-t1)*k));
		pEnd.set(s+nw);
		pStart.set(s);
	}



	/**
	 * @param aSum If true, values of each series are summed and the resulting
	 * color is a proportional mix of all colors.
	 * If false, the series are "stacked" using the painter's algorithm.
	 */
	public static long[][] paintMural (
			Graphics2D aGraphics, 
			Rectangle aBounds,
			long aT1,
			long aT2,
			Collection<BrowserData> aBrowserData,
			boolean aSum)
	{
		if (aT1 == aT2) return null;
		long[][] theValues = new long[aBrowserData.size()][];
		Color[] theColors = new Color[aBrowserData.size()];
		int[] theMarkSizes = new int[aBrowserData.size()];
		
		int i = 0;
		for (BrowserData theBrowserData : aBrowserData)
		{
			// TODO: check conversion
			System.out.println("[EventMural] Requesting counts: "+aT1+"-"+aT2);
			theValues[i] = theBrowserData.browser.getEventCounts(aT1, aT2, aBounds.width, false);
			theColors[i] = theBrowserData.color;
			theMarkSizes[i] = theBrowserData.markSize;
			i++;
		}
		
		if (aSum) paintMuralAvg(aGraphics, aBounds, theValues, theColors);
		else paintMuralStack(aGraphics, aBounds, theValues, theColors, theMarkSizes);
		
		return theValues;
	}
	
	/**
	 * Paints the mural, summing the values of all series and averaging the colors
	 */
	public static void paintMuralAvg(
			Graphics2D aGraphics, 
			Rectangle aBounds, 
			long[][] aValues, 
			Color[] aColors)
	{
		if (aValues.length == 0) return;
		
		int theHeight = aBounds.height;
		int theY = aBounds.y;
		int bh = 4; // base height

		long theMaxT = 0;
		
		// Determine maximum value
		for (int i = 0; i < aValues[0].length; i++)
		{
			long t = 0; 
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
			int h = (int) ((theHeight-bh) * t / theMaxT);
			aGraphics.setColor(c2);
			aGraphics.fillRect(aBounds.x + i, theY+theHeight-bh-h, 1, h);
		}
	}
	
	/**
	 * Paints the mural, overlaying, or stacking, all the series.
	 */
	public static void paintMuralStack (
			Graphics2D aGraphics, 
			Rectangle aBounds, 
			long[][] aValues, 
			Color[] aColors,
			int[] aMarkSizes)
	{
		int theCount = aValues.length;
		if (theCount == 0) return;
		
		int theTotalMarkSize = 0;
		for (int theSize : aMarkSizes) theTotalMarkSize += theSize;
		
		int theHeight = aBounds.height-theTotalMarkSize;
		int theY = aBounds.y;
		
		long theMaxT = 0;
		
		// Determine maximum value
		for (int i = 0; i < aValues[0].length; i++)
		{
			for (int j = 0; j < aValues.length; j++) theMaxT = Math.max(theMaxT, aValues[j][i]);
		}
		
		Object theOriginalAA = aGraphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		for (int i = 0; i < aValues[0].length; i++)
		{
			int theCurrentMarkY = theHeight;
			for (int j=0;j<theCount;j++)
			{
				long t = aValues[j][i];
				Color c1 = aColors[j];
				Color c2 = UIUtils.getLighterColor(c1);
				
				// Draw mark
				if (t>0)
				{
					aGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					aGraphics.setColor(c1);
					aGraphics.fill(makeTriangle(aBounds.x + i, theY+theCurrentMarkY, aMarkSizes[j]));
					
					// Draw proportional bar
					aGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
					int h = (int) ((theHeight * t) / theMaxT);
					aGraphics.setColor(c2);
					aGraphics.fillRect(aBounds.x + i, theY+theHeight-h, 1, h);
				}

				theCurrentMarkY += aMarkSizes[j];
			}
		}
		aGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, theOriginalAA);
	}
	
	private static Shape makeTriangle(float aX, float aY, float aW)
	{
		GeneralPath thePath = new GeneralPath();
		thePath.moveTo(aX, aY);
		thePath.lineTo(aX+(aW/2), aY+aW);
		thePath.lineTo(aX-(aW/2), aY+aW);
		thePath.closePath();
		
		return thePath;
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
			
			// Ensure we have a fast image buffer
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
			// Setup graphics
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
			Long theStart = aMural.pStart.get();
			Long theEnd = aMural.pEnd.get();
			
			System.out.println("[ImageUpdater] doUpdateImage ["+theStart+"-"+theEnd+"]");
			
			// Paint
			long[][] theValues = paintMural(
					theGraphics, 
					new Rectangle(0, 0, u, v), 
					theStart, 
					theEnd, 
					aMural.pEventBrowsers,
					false);

			theImageData.setUpToDate(true);
			theImageData.setValues(theValues);
			aMural.repaint();
		}
	}
	
	private static class ImageData
	{
		private BufferedImage itsImage;
		
		/**
		 * The count values displayed in the image.
		 */
		private long[][] itsValues;
		
		/**
		 * Whether the image/values are up to date.
		 */
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

		public long[][] getValues()
		{
			return itsValues;
		}

		public void setValues(long[][] aValues)
		{
			itsValues = aValues;
		}
	}
	
	private class EventDetailsPanel extends JPanel
	{
		private ILogEvent itsEvent;
		
		private JLabel itsKindLabel;
		private JLabel itsThreadLabel;
		private JLabel itsDetailsLabel;

		private EventFormatter itsFormatter;

		public EventDetailsPanel()
		{
			super(new GridStackLayout(1));
			setBackground(Color.WHITE);
			setBorder(BorderFactory.createLineBorder(Color.black));
			
			itsKindLabel = new JLabel();
			itsKindLabel.setFont(FontConfig.SMALL_FONT.getAWTFont());
			
			itsThreadLabel = new JLabel();
			itsThreadLabel.setFont(FontConfig.SMALL_FONT.getAWTFont());
			
			itsDetailsLabel = new JLabel();
			itsDetailsLabel.setFont(FontConfig.STD_FONT.getAWTFont());
			
			add(itsKindLabel);
			add(itsThreadLabel);
			add(itsDetailsLabel);
			itsFormatter = new EventFormatter(itsGUIManager.getSession().getLogBrowser());
		}
		
		public void setEvent(ILogEvent aEvent)
		{
			itsEvent = aEvent;
			itsThreadLabel.setText("Thread: "+itsEvent.getThread().getName());
			itsDetailsLabel.setText(itsFormatter.getHtmlText(itsEvent));
			
			BytecodeRole theRole = LocationUtils.getEventRole(aEvent);
			ImageResource theIcon = GUIUtils.getRoleIcon(theRole);
			itsDetailsLabel.setIcon(theIcon != null ? theIcon.asIcon(15) : null);
		}
	}
}
