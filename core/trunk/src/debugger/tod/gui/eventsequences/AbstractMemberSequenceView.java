/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import java.awt.Color;

import tod.core.model.structure.IMemberInfo;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.IObjectInspector;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;
import tod.gui.view.LogView;
import zz.csg.api.IDisplay;
import zz.csg.api.IRectangularGraphicObject;

/**
 * Abstract base class for event sequence views that displays events relative to a class member.
 * @author gpothier
 */
public abstract class AbstractMemberSequenceView extends AbstractSingleBrowserSequenceView
{
	private final IObjectInspector itsInspector;
	
	public AbstractMemberSequenceView(IDisplay aDisplay, LogView aLogView, Color aColor, IObjectInspector aInspector)
	{
		super(aDisplay, aLogView, aColor);
		itsInspector = aInspector;
	}

	@Override
	protected IEventBrowser getBrowser()
	{
		return itsInspector.getBrowser(getMember());
	}

	/**
	 * Returns the member whose events are displayed in this sequence.
	 */
	public abstract IMemberInfo getMember();
	
	/**
	 * Helper method that creates a graphic object suitable for 
	 * representing the given object.
	 */
	protected IRectangularGraphicObject createBaloon(Object aObject)
	{
		LogView theLogView = getLogView();
		IGUIManager theGUIManager = theLogView.getGUIManager();
		IEventTrace theEventTrace = theLogView.getTrace();
			
		return Hyperlinks.object(theGUIManager, theEventTrace, itsInspector.getObject(), aObject, FONT);
	}
}
