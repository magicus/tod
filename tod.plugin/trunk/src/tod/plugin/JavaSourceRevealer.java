package tod.plugin;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.IEditorPart;

import tod.core.database.structure.SourceRange;
import tod.core.session.ISession;
import tod.utils.TODUtils;

public class JavaSourceRevealer implements ISourceRevealer
{
	public boolean canHandle(SourceRange aSourceRange)
	{
		return aSourceRange.sourceFile.endsWith(".java");
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
		String theTypeName = aSourceRange.typeName;
		
		// For inner classes, we just try to open the root class 
		int theIndex = theTypeName.indexOf('$');
		if (theIndex >= 0) theTypeName = theTypeName.substring(0, theIndex);
		
		IType theType = TODPluginUtils.getType(aJavaProjects, theTypeName);
		if (theType == null)
		{
			// Hack for AspectJ
			// TODO: try to put that into tod.plugin.ajdt
			theTypeName = theTypeName.replace('.', '/');
			IFile theFile = SourceRevealerUtils.findSourceFile(aJavaProjects, theTypeName + ".aj");
			if (theFile != null) return EditorUtility.openInEditor(theFile, false);
			else
			{
				TODUtils.logf(0, "The type %s has not been found in the available sources "
						+ "of the Eclipse workspace.\n Path were " + "to find the sources was: %s",
						aSourceRange.typeName, aJavaProjects);
				return null;
			}
		}
		
		// Eclipse 3.3 only
		return JavaUI.openInEditor(theType, false, false);
		
		// This is safe for pre-3.3
//		return EditorUtility.openInEditor(theType, false);
	}

	
}
