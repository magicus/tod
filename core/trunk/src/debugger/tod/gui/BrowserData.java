/*
 * Created on Oct 16, 2005
 */
package tod.gui;

import java.awt.Color;

import tod.core.model.browser.IEventBrowser;

/**
 * Data agregate for browsers that are used in an {@link tod.gui.eventsequences.EventMural}
 * or a {@link tod.gui.TimeScale}. Apart from an {@link tod.core.model.browser.IEventBrowser}
 * it contains a color that indicates how the events of the broswser should be rendered.
 * @author gpothier
 */
public class BrowserData
{
	private IEventBrowser itsBrowser;
	private Color itsColor;
	
	public BrowserData(IEventBrowser aBrowser, Color aColor)
	{
		itsBrowser = aBrowser;
		itsColor = aColor;
	}

	public IEventBrowser getBrowser()
	{
		return itsBrowser;
	}

	public Color getColor()
	{
		return itsColor;
	}
}
