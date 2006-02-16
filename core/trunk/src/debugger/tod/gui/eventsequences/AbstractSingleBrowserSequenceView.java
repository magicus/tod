/*
 * Created on Feb 16, 2006
 */
package tod.gui.eventsequences;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import tod.core.model.trace.IEventBrowser;
import tod.gui.BrowserData;
import tod.gui.view.LogView;
import zz.csg.api.IDisplay;

/**
 * A browser sequence view for a single browser.
 * @author gpothier
 */
public abstract class AbstractSingleBrowserSequenceView extends AbstractSequenceView
{
	private final Color itsColor;

	public AbstractSingleBrowserSequenceView(IDisplay aDisplay, LogView aLogView, Color aColor)
	{
		super(aDisplay, aLogView);
		itsColor = aColor;
	}

	@Override
	protected final List<BrowserData> getBrowsers()
	{
		return Collections.singletonList(new BrowserData(getBrowser(), itsColor));
	}

	/**
	 * Subclasses must specify the browser to use by implementing this method.
	 */
	protected abstract IEventBrowser getBrowser();

}
