/*
 * Created on Jun 13, 2007
 */
package tod.plugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import tod.core.database.structure.IBehaviorInfo;

public class GenericSourceRevealer extends SourceRevealer
{
	private List<IJavaProject> itsJavaProject = new ArrayList<IJavaProject>();

	public GenericSourceRevealer(ILaunch aLaunch, IJavaProject aJavaProject)
	{
		super(aLaunch);
		itsJavaProject.add(aJavaProject);
	}

	public GenericSourceRevealer(ILaunch aLaunch, IProject[] aProjects)
	{
		super(aLaunch);
		
		for (IProject theProject : aProjects)
		{
			IJavaProject theJProject = JavaCore.create(theProject);
			if (theJProject != null && theJProject.exists()) 
				itsJavaProject.add(theJProject);
		}
	}

	@Override
	protected void gotoSource(String aTypeName, int aLineNumber)
	{
		if (!itsJavaProject.isEmpty())
		{
			SourceRevealerUtils.reveal(itsJavaProject, aTypeName, aLineNumber-1);
		}
	}
	
	@Override
	protected void gotoSource(IBehaviorInfo aBehavior)
	{
	}

}