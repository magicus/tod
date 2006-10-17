/*
 * Created on Aug 15, 2005
 */
package tod.plugin.views;

import org.eclipse.jdt.core.IJavaElement;

import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ILocationInfo;
import tod.gui.MinerUI;
import tod.gui.seed.Seed;
import tod.gui.seed.SeedFactory;
import tod.plugin.DebuggingSession;
import tod.plugin.TODPluginUtils;
import tod.plugin.TODSessionManager;
import zz.utils.properties.IProperty;
import zz.utils.properties.PropertyListener;

public class EventViewer extends MinerUI
{
	private final TraceNavigatorView itsTraceNavigatorView;

	public EventViewer(TraceNavigatorView aTraceNavigatorView)
	{
		itsTraceNavigatorView = aTraceNavigatorView;
		TODSessionManager.getInstance().pCurrentSession().addHardListener(new PropertyListener<DebuggingSession>()
				{
					public void propertyChanged(IProperty<DebuggingSession> aProperty, DebuggingSession aOldValue, DebuggingSession aNewValue)
					{
						reset();
					}
				});
	}

	protected DebuggingSession getSession()
	{
		return TODSessionManager.getInstance().pCurrentSession().get();
	}
	
	public void showElement (IJavaElement aElement)
	{
		ILocationInfo theLocationInfo = TODPluginUtils.getLocationInfo(getSession(), aElement);
		Seed theSeed = SeedFactory.getDefaultSeed(this, getBrowser(), theLocationInfo);
		openSeed(theSeed, false);
	}
	
	public void gotoEvent(ILogEvent aEvent)
	{
	    itsTraceNavigatorView.gotoEvent(getSession(), aEvent);
	}
	
	

}
