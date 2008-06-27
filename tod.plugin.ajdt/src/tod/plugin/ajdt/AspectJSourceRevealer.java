/*
 * Created on Jun 27, 2008
 */
package tod.plugin.ajdt;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.IEditorPart;

import tod.core.database.structure.SourceRange;
import tod.core.session.ISession;
import tod.plugin.ISourceRevealer;
import tod.plugin.SourceRevealerUtils;
import tod.plugin.TODPluginUtils;
import tod.utils.TODUtils;

public class AspectJSourceRevealer implements ISourceRevealer
{

	public boolean canHandle(SourceRange aSourceRange)
	{
		return aSourceRange.sourceFile.endsWith(".aj");		
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
