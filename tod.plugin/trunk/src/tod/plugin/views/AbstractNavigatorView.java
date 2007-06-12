/*
 * Created on May 28, 2007
 */
package tod.plugin.views;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;

import javax.swing.JComponent;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import tod.core.database.event.ILogEvent;
import tod.gui.IGUIManager;
import tod.plugin.DebuggingSession;
import zz.utils.ui.StackLayout;

public abstract class AbstractNavigatorView extends ViewPart implements ISelectionListener
{
	private Frame itsFrame;
	
	/**
	 * This flag permits to avoid infinite recursion or misbehaviors
	 * of selection in java source vs. {@link #gotoEvent(ILogEvent)}.
	 */
	private boolean itsMoving = false;

	@Override
	public void createPartControl(Composite parent) 
	{
		System.out.println("Add listener");
		ISelectionService theSelectionService = getViewSite().getWorkbenchWindow().getSelectionService();
		theSelectionService.addPostSelectionListener(this);
		
		final Composite theEmbedded = new Composite(parent, SWT.EMBEDDED | SWT.CENTER);
		parent.setLayout(new FillLayout());
		
		itsFrame = SWT_AWT.new_Frame(theEmbedded);
		Panel theRootPanel = new Panel(new BorderLayout());
		itsFrame.setLayout(new StackLayout());
		itsFrame.add(theRootPanel);

		theEmbedded.addControlListener(new ControlListener()
		{
			public void controlMoved(ControlEvent aE)
			{
			}

			public void controlResized(ControlEvent aE)
			{
				itsFrame.setSize(theEmbedded.getSize().x, theEmbedded.getSize().y);
			}
			
		});
		
		theRootPanel.add(createComponent());
	}
	
	protected abstract JComponent createComponent();
	
	public abstract IGUIManager getGUIManager();
	
	@Override
	public void dispose()
	{
		ISelectionService theSelectionService = getViewSite().getWorkbenchWindow().getSelectionService();
		theSelectionService.removePostSelectionListener(this);
	}
	
	@Override
	public void setFocus()
	{
	}

	/**
	 * Called when the selected element in the workbench changes.
	 */
	public void selectionChanged(IWorkbenchPart aPart, ISelection aSelection)
	{
	    if (itsMoving) return;
	    itsMoving = true;
	    
//		IJavaElement theJavaElement = null;
//		
//		if (aSelection instanceof IStructuredSelection)
//		{
//			IStructuredSelection theSelection = (IStructuredSelection) aSelection;
//			Object theElement = theSelection.getFirstElement();
//			if (theElement instanceof IJavaElement)
//			{
//				theJavaElement = (IJavaElement) theElement;
//			}
//		}
//		else if (aSelection instanceof ITextSelection)
//		{
//			ITextSelection theTextSelection = (ITextSelection) aSelection;
//			theJavaElement = TextSelectionUtils.getJavaElement(aPart, theTextSelection);
//		}
//		
//		if (theJavaElement != null) itsEventViewer.showElement(theJavaElement);
		itsMoving = false;
	}
	
	public void gotoEvent(DebuggingSession aSession, ILogEvent aEvent)
	{
	    if (itsMoving) return;
	    itsMoving = true;

	    aSession.gotoSource(aEvent);
	    
	    itsMoving = false;
	}

}