/*
 * Created on Jan 30, 2007
 */
package tod.plugin.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class ShowPreviousEventForLineActionDelegate extends AbstractRulerActionDelegate
{
	@Override
	protected IAction createAction(
			ITextEditor aEditor, 
			IVerticalRulerInfo aRulerInfo)
	{
		return new ShowPreviousEventForLineAction(aEditor, aRulerInfo);
	}

	private static class ShowPreviousEventForLineAction extends AbstractRulerAction
	{
		public ShowPreviousEventForLineAction(ITextEditor aEditor, IVerticalRulerInfo aRulerInfo)
		{
			super(aEditor, aRulerInfo);
		}

		@Override
		public void run()
		{
			getMinerUI(true).showPreviousEventForLine(getCurrentBehavior(), getCurrentLine());
		}
		
		@Override
		protected boolean shouldEnable()
		{
			return getCurrentMethod() != null && getMinerUI(false).canShowPreviousEventForLine();
		}
	}
}
