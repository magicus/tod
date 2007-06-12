/*
 * Created on Jan 30, 2007
 */
package tod.plugin.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class ShowNextEventForLineActionDelegate extends AbstractRulerActionDelegate
{
	@Override
	protected IAction createAction(
			ITextEditor aEditor, 
			IVerticalRulerInfo aRulerInfo)
	{
		return new ShowNextEventForLineAction(aEditor, aRulerInfo);
	}
	
	private static class ShowNextEventForLineAction extends AbstractRulerAction
	{
		public ShowNextEventForLineAction(ITextEditor aEditor, IVerticalRulerInfo aRulerInfo)
		{
			super(aEditor, aRulerInfo);
		}

		@Override
		public void run()
		{
			getGUIManager(true).showNextEventForLine(getCurrentBehavior(), getCurrentLine());
		}
		
		@Override
		protected boolean shouldEnable()
		{
			return getCurrentMethod() != null && getGUIManager(false).canShowNextEventForLine();
		}
	}

}
