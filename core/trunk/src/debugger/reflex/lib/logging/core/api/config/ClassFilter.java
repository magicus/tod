/*
 * Created on Oct 13, 2004
 */
package reflex.lib.logging.core.api.config;

import java.util.ArrayList;
import java.util.List;

import reflex.api.hookset.ClassSelector;
import reflex.api.model.RClass;
import reflex.lib.logging.core.impl.mop.ReflexLocationPool;
import reflex.tools.selectors.NameCS;
import reflex.tools.selectors.PackageCS;

/**
 * Defines a filter that accepts specific classes.
 * @author gpothier
 */
public class ClassFilter implements ClassSelector
{
	private List<ClassSelector> itsSelectors = 
		new ArrayList<ClassSelector>();

	/**
	 * Indicates if this filter is currently empty,
	 * ie. doesn't accept any class.
	 */
	public boolean isEmpty() 
	{
		return itsSelectors.isEmpty();
	}
	
	/**
	 * Enables logging for the specified package.
	 * @param aRecursive Whether to consider subpackages
	 */
	public void addPackage (String aPackageName, boolean aRecursive)
	{
		StaticConfig.getInstance().checkState();
		itsSelectors.add (new PackageCS(aPackageName, aRecursive));
	}
	
	/**
	 * Enables logging for the specified packages.
	 * @param aRecursive Whether to consider subpackages
	 */
	public void addPackages (String[] aPackageNames, boolean aRecursive)
	{
		StaticConfig.getInstance().checkState();
		itsSelectors.add (new PackageCS(aPackageNames, aRecursive));
	}

	/**
	 * Enables logging for the specified class.
	 */
	public void addClass (String aClassName)
	{
		StaticConfig.getInstance().checkState();
		itsSelectors.add (new NameCS(aClassName));
	}

	/**
	 * Enables logging for the specified classes.
	 */
	public void addClasses (String[] aClassNames)
	{
		StaticConfig.getInstance().checkState();
		itsSelectors.add (new NameCS(aClassNames));
	}
	
	public boolean accept(RClass aClass)
	{
		for (ClassSelector theSelector : itsSelectors)
		{
			if (theSelector.accept(aClass))
			{
				ReflexLocationPool.getLocationId(aClass);
				return true;
			}
		}
		return false;
	}



}
