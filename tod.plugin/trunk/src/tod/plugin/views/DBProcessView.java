/*
 * Created on Jun 19, 2007
 */
package tod.plugin.views;

import javax.swing.JComponent;

import org.eclipse.ui.part.ViewPart;

import tod.impl.dbgrid.DBProcessManager;
import tod.impl.dbgrid.gui.GridLauncher;
import tod.plugin.TODSessionManager;

/**
 * This view permits to control the database process.
 * @author gpothier
 */
public class DBProcessView extends AbstractAWTView
{
	@Override
	protected JComponent createComponent()
	{
		return new GridLauncher(DBProcessManager.getDefault());
	}
}
