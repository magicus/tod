/*
 * Created on Aug 15, 2005
 */
package tod.plugin;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import tod.core.LocationRegistrer;
import tod.core.model.event.IBehaviorEnterEvent;
import tod.core.model.event.IEvent_Location;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.LocationInfo;
import tod.core.model.structure.TypeInfo;
import tod.core.model.trace.IEventTrace;
import tod.session.ISession;

/**
 * Utilities for the TOD plugin
 * @author gpothier
 */
public class TODPluginUtils
{
	/**
	 * Returns the TOD location info that corresponds to the given element in the
	 * given session.
	 * @param aSession The debugging session in which the element should be looked for
	 * @param aElement The JDT java element to look for.
	 * @return TOD location info, or null if none is found.
	 */
	public static LocationInfo getLocationInfo (ISession aSession, IJavaElement aElement)
	{
		IEventTrace theLog = aSession.getEventTrace();
		LocationRegistrer theLocationRegistrer = theLog.getLocationRegistrer();
		
		if (aElement instanceof IMember)
		{
			IMember theMember = (IMember) aElement;
			
			IType theType = theMember.getDeclaringType();
			if (theType == null) return null;
			
			String theTypeName = theType.getFullyQualifiedName();
			TypeInfo theTypeInfo = theLocationRegistrer.getType(theTypeName);
			if (theTypeInfo == null) return null;
			
			System.out.println(theTypeInfo);
			
			if (theMember instanceof IMethod)
			{
				IMethod theMethod = (IMethod) theMember;
				String theMethodName = theMethod.getElementName();
				return theTypeInfo.getBehavior(theMethodName);
			}
			else if (theMember instanceof IInitializer)
			{
				IInitializer theInitializer = (IInitializer) theMember;
				String theInitializerName = theInitializer.getElementName();
				return theTypeInfo.getBehavior(theInitializerName);
			}
			else if (theMember instanceof IField)
			{
				IField theField = (IField) theMember;
				String theFieldName = theField.getElementName();
				return theTypeInfo.getField(theFieldName);
			}
		}
		
		return null;
	}
	
	private static final SearchParticipant[] PARTICIPANTS = {SearchEngine.getDefaultSearchParticipant()};
	
	/**
	 * Searches for declarations of the given name and kind in the whole workspace. 
	 */
	public static List searchDeclarations (String aName, int aKind) throws CoreException
	{
		SearchPattern thePattern = SearchPattern.createPattern(
				aName, 
				aKind,
				IJavaSearchConstants.DECLARATIONS, 
				SearchPattern.R_EXACT_MATCH);

		IJavaSearchScope theScope = SearchEngine.createWorkspaceScope();
		SearchEngine theSearchEngine = new SearchEngine();
		SimpleResultCollector theCollector = new SimpleResultCollector ();

		theSearchEngine.search(
				thePattern, 
				PARTICIPANTS,
				theScope, 
				theCollector,
				null);
		
		return theCollector.getResults();
	}
	
	public static void gotoSource (IEvent_Location aEvent)
	{
	    IBehaviorEnterEvent theFather = aEvent.getFather();
	    if (theFather == null) return;
	    
	    int theBytecodeIndex = aEvent.getOperationBytecodeIndex();
	    BehaviorInfo theBehavior = theFather.getBehavior();
	    int theLineNumber = theBehavior.getLineNumber(theBytecodeIndex);
	    TypeInfo theType = theBehavior.getType();
	    
	    gotoSource(theType.getName(), theLineNumber);
	}
	
	public static void gotoSource (String aTypeName, final int aLineNumber)
	{
		try
		{
			// Search Java type
			List theList = searchDeclarations(aTypeName, IJavaSearchConstants.TYPE);

			if (theList.size() == 1)
			{
				IType theSourceType = (IType) theList.get(0);
				final ICompilationUnit theCompilationUnit = theSourceType.getCompilationUnit();
				
				Display.getDefault().asyncExec(new Runnable ()
				{
					public void run()
					{
						try
						{
							IEditorPart theEditorPart = JavaUI.openInEditor(theCompilationUnit);

							// Select precise line
							if (theEditorPart instanceof ITextEditor && aLineNumber >= 0)
							{
								ITextEditor theEditor = (ITextEditor) theEditorPart;
								
								IDocumentProvider provider = theEditor.getDocumentProvider();
								IDocument document = provider.getDocument(theEditor.getEditorInput());
								int start = document.getLineOffset(aLineNumber);
								theEditor.selectAndReveal(start, 0);
								
							}
						} 
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				});
				
			}
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}

//
//		IWorkbenchPage page= editor.getSite().getPage();
//		page.activate(editor);

	}

}
