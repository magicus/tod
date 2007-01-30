/*
 * Created on Jan 16, 2007
 */
package tod.plugin.actions;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

import tod.core.database.structure.IBehaviorInfo;
import tod.plugin.TODPluginUtils;

/**
 * Handler for the "Show events in TOD" ruler action.
 * @author gpothier
 */
public class ShowEventsForLineActionDelegate extends AbstractRulerActionDelegate
{
	@Override
	protected IAction createAction(
			ITextEditor aEditor, 
			IVerticalRulerInfo aRulerInfo)
	{
		return new ShowEventsForLineAction(aEditor, aRulerInfo);
	}

	private static class ShowEventsForLineAction extends AbstractRulerAction
	{
		public ShowEventsForLineAction(ITextEditor aEditor, IVerticalRulerInfo aRulerInfo)
		{
			super(aEditor, aRulerInfo);
		}

		@Override
		public void run()
		{
			getMinerUI(true).showEventsForLine(getCurrentBehavior(), getCurrentLine());
		}
		
		@Override
		protected boolean shouldEnable()
		{
			return getCurrentMethod() != null;
		}
		
	}
	
}
