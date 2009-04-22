/*
 * Created on Jul 19, 2006
 */
package tod.core.config;

import tod.tools.parsers.ParseException;
import tod.tools.parsers.workingset.AbstractClassSet;
import tod.tools.parsers.workingset.WorkingSetFactory;
import zz.utils.IFilter;

/**
 * A class selector is a filter that accepts or rejects fully qualified
 * class names.
 * @author gpothier
 */
public interface ClassSelector extends IFilter<String>
{
	/**
	 * A class selector that accepts any class.
	 * @author gpothier
	 */
	public static class AllCS implements ClassSelector
	{
		private static AllCS INSTANCE = new AllCS();

		public static AllCS getInstance()
		{
			return INSTANCE;
		}

		private AllCS()
		{
		}
		
		public boolean accept(String aValue)
		{
			return true;
		}
	}
	
	/**
	 * A class selector that accepts classes that have a specific name.
	 * @author gpothier
	 */
	public static class NameCS implements ClassSelector
	{
		private String[] itsNames;

		public NameCS(String aName)
		{
			itsNames = new String[] {aName};
		}

		public NameCS(String... aNames)
		{
			itsNames = aNames;
		}
		
		public boolean accept(String aValue)
		{
			for (String theName : itsNames) if (theName.equals(aValue)) return true;
			return false;
		}
	}
	
	/**
	 * A class selector that accepts only classes that belong to specific packages.
	 * @author gpothier
	 */
	public static class PackageCS implements ClassSelector
	{
	    /**
	     * Indicates if the subpackages should be accepted as well.
	     */
	    private boolean itsRecursive;
	    
	    private String[] itsNames;

	    public PackageCS(String aName, boolean aRecursive)
	    {
	    	this(new String[] {aName}, aRecursive);
	    }

	    public PackageCS(String[] aNames, boolean aRecursive)
	    {
	    	itsNames = aNames;
	        itsRecursive = aRecursive;
	    }

	    public boolean accept(String aName)
		{
	    	for(String theName : itsNames) 
	    	{
	    		if (acceptName(theName, aName)) return true;
	    	}
	    	return false;
		}

		protected boolean acceptName(String aReferenceName, String aCandidateName)
	    {
	        if (itsRecursive) return aCandidateName.startsWith(aReferenceName);
	        else
	        {
	            int theLength = aReferenceName.length();
	            return aCandidateName.startsWith(aReferenceName) && aCandidateName.lastIndexOf('.') <= theLength;
	        }
	    }
	}
	
	/**
	 * A class selector base on a working set.
	 * 
	 * @see reflex.std.run.WorkingSetHandler
	 * @author gpothier
	 */
	public static class WorkingSetClassSelector implements ClassSelector
	{
	    private AbstractClassSet itsClassSet;

	    public WorkingSetClassSelector(String aWorkingSet) throws ParseException
	    {
	        itsClassSet = WorkingSetFactory.parseWorkingSet(aWorkingSet);
	    }

	    public boolean accept(String aName)
	    {
	        return itsClassSet.accept(aName);
	    }

	}
}
