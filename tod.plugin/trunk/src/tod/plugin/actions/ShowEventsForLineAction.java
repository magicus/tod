/*
 * Created on Jan 16, 2007
 */
package tod.plugin.actions;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.ui.actions.ActionDelegateHelper;
import org.eclipse.jdt.internal.debug.ui.actions.ToggleBreakpointAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

import tod.plugin.views.TraceNavigatorView;

/**
 * Handler for the "Show events in TOD" ruler action.
 * @author gpothier
 */
public class ShowEventsForLineAction extends AbstractRulerActionDelegate
{
	@Override
	protected IAction createAction(final ITextEditor aEditor, final IVerticalRulerInfo aRulerInfo)
	{
		return new Action()
		{
			@Override
			public boolean isEnabled()
			{
				try
				{
					System.out.println(".isEnabled()");
					// Find out current Java method and line number
					int theLine = aRulerInfo.getLineOfLastMouseButtonActivity()+1;
					IMethod theMethod = getMethod(aEditor, theLine);
					return theMethod != null;
				}
				catch (BadLocationException e)
				{
					throw new RuntimeException(e);
				}
			}
			
			@Override
			public void run()
			{
				try
				{
					// Find out current Java method and line number
					int theLine = aRulerInfo.getLineOfLastMouseButtonActivity()+1;
					IMethod theMethod = getMethod(aEditor, theLine);

					// Find and focus trace navigator view
					IWorkbenchWindow theWindow = aEditor.getSite().getWorkbenchWindow();
					IWorkbenchPage thePage = theWindow.getActivePage();
					TraceNavigatorView theView =
						(TraceNavigatorView) thePage.showView(TraceNavigatorView.VIEW_ID);
					
					// Show events
					theView.showEventsForLine(theMethod, theLine);
				}
				catch (PartInitException e)
				{
					throw new RuntimeException(e);
				}
				catch (BadLocationException e)
				{
					throw new RuntimeException(e);
				}
			}
		};
	}

	/**
	 * Returns the Java type at the specified line of the specified editor.
	 * Inspired from {@link ToggleBreakpointAdapter#getType} 
	 */
	public static IMethod getMethod(ITextEditor aEditor, int aLine) throws BadLocationException
	{
		IDocument theDocument = aEditor.getDocumentProvider().getDocument(aEditor.getEditorInput());
		IRegion theRegion = theDocument.getLineInformation(aLine);
		ITextSelection theSelection = new TextSelection(theDocument, theRegion.getOffset(), 0);
		
		IMember theMember = ActionDelegateHelper.getDefault().getCurrentMember(theSelection);
		
		if (theMember instanceof IMethod)
		{
			IMethod theMethod = (IMethod) theMember;
			return theMethod;
		}
		else return null;
	}
}
