package tod.plugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;

import tod.core.database.structure.SourceRange;
import tod.core.session.ISession;

/**
 * Interface that should be implemented by plugins that are capable
 * to link events to source code lines 
 * @author minostro
 */
public interface ISourceRevealer
{
	/**
	 * Whether this revealer can handle the given source range.
	 */
	public boolean canHandle(SourceRange aSourceRange);
	
	/**
	 * Reveal a particular source location.
	 */
	public void reveal(ISession aSession, SourceRange aSourceRange) throws CoreException, BadLocationException;
}
