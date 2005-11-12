/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.seed.ObjectInspectorSeed;
import reflex.lib.logging.miner.gui.view.LogView;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.MemberInfo;
import tod.core.model.structure.ObjectId;
import tod.core.model.structure.TypeInfo;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IObjectInspector;
import tod.gui.BrowserData;
import tod.gui.Hyperlinks;
import tod.gui.SVGHyperlink;
import zz.csg.api.IDisplay;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ItemAction;
import zz.utils.properties.IRWProperty;
import zz.utils.ui.text.XFont;

public abstract class AbstractMemberSequenceView implements IEventSequenceView
{
	public static final XFont FONT = XFont.DEFAULT_XPLAIN.deriveFont(10);
	
	private MyMural itsMural;
	private final IDisplay itsDisplay;
	private final IObjectInspector itsInspector;
	private final Color itsColor;
	private final LogView itsLogView;
	
	public AbstractMemberSequenceView(IDisplay aDisplay, LogView aLogView, IObjectInspector aInspector, Color aColor)
	{
		itsDisplay = aDisplay;
		itsLogView = aLogView;
		itsInspector = aInspector;
		itsColor = aColor;
	}

	public LogView getLogView()
	{
		return itsLogView;
	}

	public IRectangularGraphicObject getEventStripe()
	{
		if (itsMural == null) 
		{
			itsMural = new MyMural(itsDisplay);
			IEventBrowser theBrowser = itsInspector.getBrowser(getMember());
			itsMural.pEventBrowsers().add(new BrowserData(theBrowser, itsColor));			
		}
		return itsMural;
	}
	
	public IRWProperty<Long> pStart()
	{
		return itsMural.pStart();
	}
	
	public IRWProperty<Long> pEnd()
	{
		return itsMural.pEnd();
	}

	/**
	 * Subclasses that override this method should call super and 
	 * add items to the returned collection.
	 */
	public Collection<ItemAction> getActions()
	{
		Collection<ItemAction> theActions = new ArrayList<ItemAction>();
		return theActions;
	}

	public Image getIcon()
	{
		return null;
	}

	/**
	 * Returns the member whose events are displayed in this sequence.
	 */
	public abstract MemberInfo getMember();
	
	/**
	 * Hook for subclasses to provide baloons.
	 * @see EventMural#getBaloon(ILogEvent)
	 */
	protected abstract IRectangularGraphicObject getBaloon(ILogEvent aEvent);

	/**
	 * Helper method that creates a graphic object suitable for 
	 * representing the given object.
	 */
	protected IRectangularGraphicObject createBaloon(Object aObject)
	{
		IGUIManager theGUIManager = itsLogView.getGUIManager();
		IEventTrace theEventTrace = itsLogView.getEventTrace();
			
		return Hyperlinks.object(theGUIManager, theEventTrace, itsInspector.getObject(), aObject, FONT);
	}

	
	private class MyMural extends EventMural
	{
		public MyMural(IDisplay aDisplay)
		{
			super (aDisplay);
		}
		
		@Override
		protected IRectangularGraphicObject getBaloon(ILogEvent aEvent)
		{
			return AbstractMemberSequenceView.this.getBaloon(aEvent);
		}
	}
}
