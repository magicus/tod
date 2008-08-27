package tod.plugin.views;

import javax.swing.JComponent;

/**
 * Base class for extra views.
 * The main view is {@link TraceNavigatorView}. Extra views are linked to the main view,
 * ie selecting an event in an extra view puts shows a control flow view in the main view.
 * @author gpothier
 *
 */
public class ExtraView extends AbstractAWTView
{

	@Override
	protected JComponent createComponent()
	{
		throw new UnsupportedOperationException();
	}

}
