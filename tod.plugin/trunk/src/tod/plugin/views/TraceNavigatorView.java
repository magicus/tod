/*
TOD plugin - Eclipse pluging for TOD
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.plugin.views;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import tod.core.database.event.ILogEvent;
import tod.plugin.DebuggingSession;
import tod.plugin.TODPluginUtils;

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
	
	public void gotoEvent(DebuggingSession aSession, ILogEvent aEvent)
	{
	    if (itsMoving) return;
	    itsMoving = true;

		TODPluginUtils.gotoSource(aSession, aEvent);
	    
	    itsMoving = false;
	}

}