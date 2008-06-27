package tod.plugin.pytod;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.IEditorPart;

import tod.core.database.structure.SourceRange;
import tod.core.session.ISession;
import tod.plugin.ISourceRevealer;
import tod.plugin.SourceRevealerUtils;
import tod.utils.TODUtils;

public class PythonSourceRevealer implements ISourceRevealer
{
	public boolean canHandle(SourceRange aSourceRange)
	{
		return aSourceRange.sourceFile.endsWith(".py");		
	}

	public boolean reveal(ISession aSession, SourceRange aSourceRange) throws CoreException, BadLocationException
	{
		IEditorPart theEditor = findEditor(SourceRevealerUtils.getJavaProjects(aSession), aSourceRange);
		if (theEditor == null) return false;
		
		SourceRevealerUtils.revealLine(theEditor, aSourceRange.startLine);
		return true;
	}
	
	public static IEditorPart findEditor(List<IJavaProject> aJavaProjects, SourceRange aSourceRange)
	throws CoreException
	{
		IFile theFile = SourceRevealerUtils.findSourceFile(aJavaProjects, aSourceRange.sourceFile);
		if (theFile != null) return EditorUtility.openInEditor(theFile, false);
		else
		{
			TODUtils.logf(0, "The type %s has not been found in the available sources "
					+ "of the Eclipse workspace.\n Path were " + "to find the sources was: %s",
					aSourceRange.sourceFile, aJavaProjects);
			return null;
		}
	}
}
