/*
 * Created on Nov 4, 2005
 */
package tod.plugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Utility class that permits to asynchronously reveal particualr source locations
 * @author gpothier
 */
public class SourceRevealer
{
	private static SourceRevealer INSTANCE = new SourceRevealer();

	public static SourceRevealer getInstance()
	{
		return INSTANCE;
	}

	private SourceRevealer()
	{
	}
	
	private Revealer itsCurentRevealer;
	private boolean itsRevealScheduled = false;
	
	private void reveal (Revealer aRevealer)
	{
		itsCurentRevealer = aRevealer;
		if (! itsRevealScheduled)
		{
			itsRevealScheduled = true;
			Display.getDefault().asyncExec(new Runnable ()
					{
						public void run()
						{
							try
							{
								itsCurentRevealer.reveal();
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
	
	public static void reveal (
			final IJavaProject aJavaProject, 
			final String aTypeName, 
			final int aLineNumber)
	{
		getInstance().reveal (new Revealer()
				{
					public void reveal() throws CoreException, BadLocationException
					{
						IType theType = TODPluginUtils.getType(aJavaProject, aTypeName);
						if (theType == null) return;

						final ICompilationUnit theCompilationUnit = theType.getCompilationUnit();
						
						IEditorPart theEditorPart = JavaUI.openInEditor(theCompilationUnit);

						// Select precise line
						if (theEditorPart instanceof ITextEditor && aLineNumber >= 0)
						{
							ITextEditor theEditor = (ITextEditor) theEditorPart;
							
							IDocumentProvider provider = theEditor.getDocumentProvider();
							IDocument document = provider.getDocument(theEditor.getEditorInput());
							int start = document.getLineOffset(aLineNumber-1);
							theEditor.selectAndReveal(start, 0);
							
						}
					}
				});
	}
	
	public static void reveal (
			final IJavaProject aJavaProject, 
			final String aTypeName, 
			final String aMethodName)
	{
		getInstance().reveal (new Revealer()
				{
					public void reveal() throws CoreException, BadLocationException
					{
						IType theType = TODPluginUtils.getType(aJavaProject, aTypeName);
						if (theType == null) return;
						
						IMethod theMethod = theType.getMethod(aMethodName, null);
						if (theMethod == null) return;
						
						JavaUI.openInEditor(theMethod);
					}
				});
	}
	
	private interface Revealer
	{
		public void reveal() throws CoreException, BadLocationException;
	}
}
