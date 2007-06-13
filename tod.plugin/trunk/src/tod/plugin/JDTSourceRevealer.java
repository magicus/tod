/*
 * Created on Jun 13, 2007
 */
package tod.plugin;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;

import tod.core.database.structure.IBehaviorInfo;

public class JDTSourceRevealer extends SourceRevealer
{
	private IJavaProject itsJavaProject;
	
	public JDTSourceRevealer(ILaunch aLaunch, IJavaProject aJavaProject)
	{
		super(aLaunch);
		itsJavaProject = aJavaProject;
	}

	protected IJavaProject getJavaProject()
	{
		return itsJavaProject;
	}

	@Override
	public void gotoSource (String aTypeName, int aLineNumber)
	{
	    SourceRevealerUtils.reveal(getLaunch(), aTypeName, aLineNumber);
	}
	
	@Override
	public void gotoSource (IBehaviorInfo aBehavior)
	{
		SourceRevealerUtils.reveal(
				getJavaProject(), 
				aBehavior.getType().getName(), 
				aBehavior.getName());
	}
}