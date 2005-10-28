/*
 * Created on Aug 13, 2005
 */
package tod.plugin.views;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import reflex.lib.logging.miner.gui.BrowserNavigator;
import reflex.lib.logging.miner.gui.seed.SeedFactory;
import tod.core.model.event.IEvent_Location;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.LocationInfo;
import tod.core.model.structure.TypeInfo;
import tod.plugin.TODPlugin;
import tod.plugin.TODPluginUtils;
import tod.plugin.TextSelectionUtils;

/**
 * This view is the trace navigator.
 * @author gpothier
 */
public class TraceNavigatorView extends ViewPart implements ISelectionListener
{
	private Frame itsFrame;
	
	/**
	 * This flag permits to avoid infinite recursion or misbehaviors
	 * of selection in java source vs. {@link #gotoEvent(ILogEvent)}.
	 */
	private boolean itsMoving = false;

	private EventViewer itsEventViewer;
	
	@Override
	public void createPartControl(Composite parent) 
	{
		System.out.println("Add listener");
		ISelectionService theSelectionService = getViewSite().getWorkbenchWindow().getSelectionService();
		theSelectionService.addPostSelectionListener(this);
		
		Composite theEmbedded = new Composite(parent, SWT.EMBEDDED | SWT.CENTER);
		parent.setLayout(new FillLayout());
		
		itsFrame = SWT_AWT.new_Frame(theEmbedded);
		Panel theRootPanel = new Panel(new BorderLayout());
		itsFrame.add(theRootPanel);
		
		itsEventViewer = new EventViewer(this);
		theRootPanel.add(itsEventViewer);
	}
	
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
	
	public void gotoEvent(ILogEvent aEvent)
	{
	    if (itsMoving) return;
	    itsMoving = true;
	    
	    if (aEvent instanceof IEvent_Location)
		{
			IEvent_Location theEvent = (IEvent_Location) aEvent;
			TODPluginUtils.gotoSource(theEvent);
		}
	    
	    itsMoving = false;
	}

}