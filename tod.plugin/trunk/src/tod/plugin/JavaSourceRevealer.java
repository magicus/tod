package tod.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import tod.core.database.structure.SourceRange;
import tod.core.session.IProgramLaunch;
import tod.core.session.ISession;
import tod.utils.TODUtils;

public class JavaSourceRevealer implements ISourceRevealer
{
	public boolean canHandle(SourceRange aSourceRange)
	{
		return aSourceRange.sourceFile.endsWith(".java");
	}

	public void reveal(ISession aSession, SourceRange aSourceRange) throws CoreException, BadLocationException
	{
		IEditorPart theEditor = findEditor(getJavaProjects(aSession), aSourceRange);

		if (theEditor instanceof ITextEditor)
		{
			ITextEditor theTextEditor = (ITextEditor) theEditor;
			IDocumentProvider theProvider = theTextEditor.getDocumentProvider();
			IDocument theDocument = theProvider.getDocument(theTextEditor.getEditorInput());
			int theStart = theDocument.getLineOffset(aSourceRange.startLine - 1);
			theTextEditor.selectAndReveal(theStart, 0);
		}
	}
	
	/**
	 * Returns all the java projects of the given session.
	 */
	private static List<IJavaProject> getJavaProjects(ISession aSession)
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

	public static IEditorPart findEditor(List<IJavaProject> aJavaProjects, SourceRange aSourceRange)
	throws CoreException
	{
		String theTypeName = aSourceRange.sourceFile;
		
		if (theTypeName.endsWith(".aj"))
		{
			// Hack for aspectj files.
			IFile theFile = SourceRevealerUtils.findFile(aJavaProjects, theTypeName);
			return theFile != null ? EditorUtility.openInEditor(theFile, false) : null;
		}
		
		// For inner classes, we just try to open the root class 
		int theIndex = theTypeName.indexOf('$');
		if (theIndex >= 0) theTypeName = theTypeName.substring(0, theIndex);
		
		IType theType = TODPluginUtils.getType(aJavaProjects, theTypeName);
		if (theType == null)
		{
			// Another aspectj hack
			theTypeName = theTypeName.replace('.', '/');
			IFile theFile = SourceRevealerUtils.findSourceFile(aJavaProjects, theTypeName + ".aj");
			if (theFile != null) return EditorUtility.openInEditor(theFile, false);
			else
			{
				TODUtils.logf(0, "The type %s has not been found in the available sources "
						+ "of the Eclipse workspace.\n Path were " + "to find the sources was: %s",
						aSourceRange.sourceFile, aJavaProjects);
				return null;
			}
		}
		
		// Eclipse 3.3 only
		// theEditor = JavaUI.openInEditor(theType, false, false);
		
		return EditorUtility.openInEditor(theType, false);
	}

	
}
