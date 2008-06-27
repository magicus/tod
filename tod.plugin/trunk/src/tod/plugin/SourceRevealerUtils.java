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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import tod.core.database.structure.SourceRange;
import tod.core.session.IProgramLaunch;
import tod.core.session.ISession;
import tod.utils.TODUtils;
import zz.eclipse.utils.EclipseUtils;

/**
 * Utility class that permits to asynchronously reveal particualr source
 * locations
 * 
 * @author gpothier
 */
public class SourceRevealerUtils
{
	private static SourceRevealerUtils INSTANCE = new SourceRevealerUtils();

	public static SourceRevealerUtils getInstance()
	{
		return INSTANCE;
	}

	private SourceRevealerUtils()
	{
	}

	private ISourceRevealer itsCurentRevealer;

	private boolean itsRevealScheduled = false;

	private void reveal(
			ISourceRevealer aRevealer, 
			final ISession aSession, 
			final SourceRange aSourceRange)
	{
		itsCurentRevealer = aRevealer;
		if (!itsRevealScheduled)
		{
			itsRevealScheduled = true;
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					try
					{
						itsCurentRevealer.reveal(aSession, aSourceRange);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					itsRevealScheduled = false;
				}
			});
		}
	}

	
	/**
	 * Opens a given location. Should be safe for JDT and PDE projects.
	 */
	public static void reveal(ISession aSession, SourceRange aSourceRange)
	{
		TODUtils.log(1, "[SourceRevealerUtils.reveal(ISession, SourceRange)]" + aSourceRange);
		
		IExtensionRegistry theRegistry = Platform.getExtensionRegistry();
		IConfigurationElement[] theExtensions = 
			theRegistry.getConfigurationElementsFor("tod.plugin.SourceRevealer");

		for (IConfigurationElement theElement : theExtensions)
		{
			ISourceRevealer theRevealer;
			try
			{
				theRevealer = (ISourceRevealer) theElement.createExecutableExtension("class");
			}
			catch (CoreException e)
			{
				throw new RuntimeException(e);
			}
			
			if (! theRevealer.canHandle(aSourceRange)) continue;
				
			getInstance().reveal(theRevealer, aSession, aSourceRange);
		}
	}

	public static IFile findFile(List<IJavaProject> aJavaProjects, String aName)
	{
		for (IJavaProject theJavaProject : aJavaProjects)
		{
			IProject theProject = theJavaProject.getProject();
			IFile[] theFiles = EclipseUtils.findFiles(aName, theProject);
			if (theFiles.length > 0) return theFiles[0];
		}

		return null;
	}

	/**
	 * Similar to {@link #findFile(List, String)}, but only searches in source
	 * folders.
	 */
	public static IFile findSourceFile(List<IJavaProject> aJavaProjects, String aName)
	{
		for (IJavaProject theJavaProject : aJavaProjects)
		{
			try
			{
				IFile[] theFiles = EclipseUtils.findSourceFiles(aName, theJavaProject);
				if (theFiles.length > 0) return theFiles[0];
			}
			catch (Exception e)
			{
				System.out.println("Revealer exception....");
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Returns all the java projects of the given session.
	 */
	public static List<IJavaProject> getJavaProjects(ISession aSession)
	{
		List<IJavaProject> theJavaProjects = new ArrayList<IJavaProject>();
		
	    Set<IProgramLaunch> theLaunches = aSession.getLaunches();
	    for (IProgramLaunch theLaunch : theLaunches)
		{
	    	EclipseProgramLaunch theEclipseLaunch = (EclipseProgramLaunch) theLaunch;
	    	for (IProject theProject : theEclipseLaunch.getProjects())
			{
	    		IJavaProject theJavaProject = JavaCore.create(theProject);
				if (theJavaProject != null && theJavaProject.exists())
				{
					theJavaProjects.add(theJavaProject);
				}
			}
		}

	    return theJavaProjects;
	}

	/**
	 * Reveals the specified line in the given editor
	 */
	public static void revealLine(IEditorPart aEditor, int aLine) throws BadLocationException
	{
		if (aEditor instanceof ITextEditor)
		{
			ITextEditor theTextEditor = (ITextEditor) aEditor;
			IDocumentProvider theProvider = theTextEditor.getDocumentProvider();
			IDocument theDocument = theProvider.getDocument(theTextEditor.getEditorInput());
			int theStart = theDocument.getLineOffset(aLine-1);
			theTextEditor.selectAndReveal(theStart, 0);
		}
	}
	

}
