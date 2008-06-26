package tod.plugin.pytod;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;

import tod.core.database.structure.SourceRange;
import tod.core.session.ISession;
import tod.plugin.ISourceRevealer;

public class PythonSourceRevealer implements ISourceRevealer
{
	public boolean canHandle(SourceRange aSourceRange)
	{
		return aSourceRange.sourceFile.endsWith(".py");		
	}

	public void reveal(ISession aSession, SourceRange aSourceRange)
			throws CoreException, BadLocationException
	{
		
	}

}
