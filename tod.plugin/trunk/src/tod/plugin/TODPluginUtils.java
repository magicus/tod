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
package tod.plugin;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.Workbench;

import tod.Util;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.session.ISession;
import tod.plugin.views.AbstractNavigatorView;
import tod.plugin.views.TraceNavigatorView;
import tod.utils.ConfigUtils;
import tod.utils.TODUtils;

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
	public static ILocationInfo getLocationInfo (
			ISession aSession,
			IJavaElement aElement) throws JavaModelException
	{
		if (aSession == null) TODUtils.log(0,"Trying to show event while no session is available.");
		ILogBrowser theEventTrace = aSession.getLogBrowser();
		IStructureDatabase theStructureDatabase = theEventTrace.getStructureDatabase();
		
		if (aElement instanceof IMember)
		{
			IMember theMember = (IMember) aElement;
			
			IType theType = theMember.getDeclaringType();
			if (theType == null) return null;
			
			String theTypeName = theType.getFullyQualifiedName();
			IClassInfo theClass = theStructureDatabase.getClass(theTypeName, false);
			if (theClass == null) return null;
			
			TODUtils.logf(0, "Trying to show events for class %s of id %s",theClass.getName(),theClass.getId());
			
			if (theMember instanceof IMethod)
			{
				IMethod theMethod = (IMethod) theMember;
				String theMethodName = theMethod.getElementName();
				ITypeInfo[] theArgumentTypes = getArgumentTypes(
						theStructureDatabase, 
						theMethod.getParameterTypes(), 
						theType);
				
				ITypeInfo theReturnType = getReturnType(theStructureDatabase, theMethod.getReturnType(), theType);
				
				if (theMethodName.equals(Util.getSimpleInnermostName(theTypeName))) 
					theMethodName = "<init>";
				
				return theClass.getBehavior(theMethodName, theArgumentTypes, theReturnType);
			}
			else if (theMember instanceof IInitializer)
			{
				IInitializer theInitializer = (IInitializer) theMember;
				String theInitializerName = theInitializer.getElementName();
				return theClass.getBehavior(theInitializerName, null, null);
			}
			else if (theMember instanceof IField)
			{
				IField theField = (IField) theMember;
				String theFieldName = theField.getElementName();
				return theClass.getField(theFieldName);
			}
		}
		
		return null;
	}
	
	public static ITypeInfo[] getArgumentTypes(
			IStructureDatabase aStructureDatabase,
			String[] aJDTSignatures,
			IType aDeclaringType) throws JavaModelException
	{
		int theCount = aJDTSignatures.length;
		ITypeInfo[] theTypes = new ITypeInfo[theCount];
		
		for (int i=0;i<theCount;i++)
		{
			theTypes[i] = getType(aStructureDatabase, aJDTSignatures[i], aDeclaringType);
		}
		
		return theTypes;
	}

	public static ITypeInfo getReturnType(
			IStructureDatabase aStructureDatabase,
			String aJDTSignature,
			IType aDeclaringType) throws JavaModelException
	{
		return getType(aStructureDatabase, aJDTSignature, aDeclaringType);
	}
	
	public static ITypeInfo getType(
			IStructureDatabase aStructureDatabase,
			String aJDTSignature,
			IType aDeclaringType) throws JavaModelException
	{
		String theTypeName = getResolvedTypeName(aJDTSignature, aDeclaringType);
		return aStructureDatabase.getType(theTypeName, true);
	}
	
	/**
	 * Returns the resolved Java type signature that corresponds 
	 * to a JDT type signature as defined in {@link Signature}.
	 * Inspired from {@link JavaModelUtil#getResolvedTypeName(String, IType)}.
	 */
	public static String getResolvedTypeName(String aJDTSignature, IType aDeclaringType) throws JavaModelException
	{
		int theArrayDepth = Signature.getArrayCount(aJDTSignature);
		char type = aJDTSignature.charAt(theArrayDepth);

		if (type == Signature.C_UNRESOLVED)
		{
			String name = ""; //$NON-NLS-1$
			int bracket = aJDTSignature.indexOf(Signature.C_GENERIC_START, theArrayDepth + 1);
			if (bracket > 0) name = aJDTSignature.substring(theArrayDepth + 1, bracket);
			else
			{
				int semi = aJDTSignature.indexOf(Signature.C_SEMICOLON, theArrayDepth + 1);
				if (semi == -1) { throw new IllegalArgumentException(); }
				name = aJDTSignature.substring(theArrayDepth + 1, semi);
			}
			
			String[][] resolvedNames = aDeclaringType.resolveType(name);
			if (resolvedNames != null && resolvedNames.length > 0) 
			{
				StringBuilder theBuilder = new StringBuilder();
				for (int i=0;i<theArrayDepth;i++) theBuilder.append(Signature.C_ARRAY);
				theBuilder.append(Signature.C_RESOLVED);
				String thePackage = resolvedNames[0][0];
				String theClass = resolvedNames[0][1]; 
				if (thePackage != null)
				{
					theBuilder.append(thePackage);
					theBuilder.append('.');
				}
				theBuilder.append(theClass);
				theBuilder.append(Signature.C_SEMICOLON);
				return theBuilder.toString();
			}
			else return null;
		}
		else
		{
			return aJDTSignature;
		}
	}
	
	
	
	private static final SearchParticipant[] PARTICIPANTS = {SearchEngine.getDefaultSearchParticipant()};
	
	/**
	 * Searches for declarations of the given name and kind in the whole workspace. 
	 */
	public static List searchDeclarations (
			List<IJavaProject> aJavaProject, 
			String aName,
			int aKind) throws CoreException
	{
		SearchPattern thePattern = SearchPattern.createPattern(
				aName.replace('$', '.'), 
				aKind,
				IJavaSearchConstants.DECLARATIONS, 
				SearchPattern.R_EXACT_MATCH);

		IJavaSearchScope theScope = SearchEngine.createJavaSearchScope(
				aJavaProject.toArray(new IJavaElement[aJavaProject.size()]), 
				true);
		
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
	
	public static IType getType (List<IJavaProject> aJavaProject, String aTypeName) throws CoreException
	{
		// Search Java type
		List theList = searchDeclarations(aJavaProject, aTypeName, IJavaSearchConstants.TYPE);

		if (theList.size() == 1) return (IType) theList.get(0);
		else return null;
	}
	
	/**
	 * Find and optionally gives focus to the trace navigator view.
	 */
	public static AbstractNavigatorView getTraceNavigatorView(final boolean aShow)
	{
		final AbstractNavigatorView[] theResult = new AbstractNavigatorView[1];
		Display.getDefault().syncExec(new Runnable ()
		{
			public void run()
			{
				try
				{
					String theViewId = ConfigUtils.readString("tod.plugin-view", TraceNavigatorView.VIEW_ID);
					
					IWorkbenchWindow theWindow = Workbench.getInstance().getActiveWorkbenchWindow();
					IWorkbenchPage thePage = theWindow.getActivePage();
			
					AbstractNavigatorView theView = (AbstractNavigatorView) (aShow ?
							thePage.showView(theViewId)
							: thePage.findView(theViewId));
			
					theResult[0] = theView;
				}
				catch (PartInitException e)
				{
					throw new RuntimeException(e);
				}
			}
		});
		
		return theResult[0];
	}


	
//	public static void gotoSource (IJavaProject aJavaProject, String aTypeName, final int aLineNumber)
//	{
//		try
//		{
//			IType theType = getType(aJavaProject, aTypeName);
//			if (theType == null) return;
//
//			final ICompilationUnit theCompilationUnit = theType.getCompilationUnit();
//			
//			Display.getDefault().asyncExec(new Runnable ()
//			{
//				public void run()
//				{
//					try
//					{
//						IEditorPart theEditorPart = JavaUI.openInEditor(theCompilationUnit);
//
//						// Select precise line
//						if (theEditorPart instanceof ITextEditor && aLineNumber >= 0)
//						{
//							ITextEditor theEditor = (ITextEditor) theEditorPart;
//							
//							IDocumentProvider provider = theEditor.getDocumentProvider();
//							IDocument document = provider.getDocument(theEditor.getEditorInput());
//							int start = document.getLineOffset(aLineNumber-1);
//							theEditor.selectAndReveal(start, 0);
//							
//						}
//					} 
//					catch (Exception e)
//					{
//						e.printStackTrace();
//					}
//				}
//			});
//		}
//		catch (CoreException e)
//		{
//			e.printStackTrace();
//		}
//	}
//	
}
