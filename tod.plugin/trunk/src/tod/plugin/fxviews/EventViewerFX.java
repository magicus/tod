/*
 * Created on May 28, 2007
 */
package tod.plugin.fxviews;

import tod.fxgui.FXGUI;
import tod.plugin.DebuggingSession;
import tod.plugin.TODSessionManager;
import zz.utils.properties.IProperty;
import zz.utils.properties.PropertyListener;

public class EventViewerFX extends FXGUI
{
	public EventViewerFX()
	{
		TODSessionManager.getInstance().pCurrentSession().addHardListener(new PropertyListener<DebuggingSession>()
		{
			public void propertyChanged(IProperty<DebuggingSession> aProperty, DebuggingSession aOldValue, DebuggingSession aNewValue)
			{
				setSession(aNewValue);
			}
		});		
	}

}
