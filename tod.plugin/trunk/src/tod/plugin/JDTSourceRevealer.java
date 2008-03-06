/*
 * Created on Jun 13, 2007
 */
package tod.plugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.SourceRange;

@Deprecated
public class JDTSourceRevealer extends SourceRevealer
{
	private List<IJavaProject> itsJavaProject = new ArrayList<IJavaProject>();
	
	public JDTSourceRevealer(ILaunch aLaunch, IJavaProject aJavaProject)
	{
		super(aLaunch);
		itsJavaProject.add( aJavaProject);
	}

	protected List<IJavaProject> getJavaProject()
	{
		return itsJavaProject;
	}

	@Override
	public void gotoSource (SourceRange aSourceRange)
	{
	    SourceRevealerUtils.reveal(getLaunch(), aSourceRange);
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