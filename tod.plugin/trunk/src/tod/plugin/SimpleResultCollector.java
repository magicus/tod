/*
 * Created on 12-05-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tod.plugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.core.search.SearchRequestor;


/**
 * Collects the results of a search. 
 * @author MToledo
 */
public class SimpleResultCollector extends SearchRequestor
{
	private List itsResults = new ArrayList();

	public void acceptSearchMatch(SearchMatch aMatch) throws CoreException
	{
		itsResults.add(aMatch.getElement());
	}
	
	public List getResults()
	{
		return itsResults;
	}
}