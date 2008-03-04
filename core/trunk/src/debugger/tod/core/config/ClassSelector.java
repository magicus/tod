/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
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
