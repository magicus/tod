/*
 * Created on Aug 13, 2005
 */
package tod.plugin.views;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import reflex.lib.logging.core.api.collector.LocationInfo;
import reflex.lib.logging.core.api.collector.LocationRegistrer;
import reflex.lib.logging.core.api.collector.TypeInfo;
import reflex.lib.logging.miner.api.IBrowsableLog;
import tod.plugin.TODPlugin;
import tod.plugin.TODPluginUtils;
import tod.plugin.TODSession;

/**
 * This view is the trace navigator.
 * @author gpothier
 */
public class TraceNavigatorView extends ViewPart implements ISelectionListener
{

	@Override
	public void createPartControl(Composite parent) 
	{
		System.out.println("Add listener");
		ISelectionService theSelectionService = getViewSite().getWorkbenchWindow().getSelectionService();
		theSelectionService.addPostSelectionListener(this);
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
		System.out.println(aSelection);
		if (aSelection instanceof IStructuredSelection)
		{
			IStructuredSelection theSelection = (IStructuredSelection) aSelection;
			Object theElement = theSelection.getFirstElement();
			if (theElement instanceof IJavaElement)
			{
				IJavaElement theJavaElement = (IJavaElement) theElement;
				showElement(theJavaElement);
			}
		}
	}
	
	private void showElement (IJavaElement aElement)
	{
		LocationInfo theLocationInfo = TODPluginUtils.getLocationInfo(TODPlugin.getDefault().getSession(), aElement);
		System.out.println(theLocationInfo);
	}
	

}