/*
 * Created on Apr 2, 2005
 */
package tod.tools.parsers.workingset;

import java.util.ArrayList;
import java.util.List;

public class SetOperation
{
    /**
     * Whether this operation includes or excludes classes.
     */
    private boolean itsInclude;

    /**
     * List of {@link AbstractClassSet}
     */
    private List<AbstractClassSet> itsSubsets = new ArrayList<AbstractClassSet>();

    public SetOperation(String aInclude, List<AbstractClassSet> aSubsets)
    {
        if ("+".equals(aInclude)) itsInclude = true;
        else if ("-".equals(aInclude)) itsInclude = false;
        else throw new RuntimeException("Operation type should be + or -, got: " + aInclude);
        itsSubsets = aSubsets;
    }

    /**
     * Returns whether one of the subsets of this operation accepts the given
     * class.
     */
    public boolean accept(String aClassname)
    {
    	for (AbstractClassSet theSubset : itsSubsets)
		{
            if (theSubset.accept(aClassname)) return true;
        }
        return false;
    }

    public boolean isInclude()
    {
        return itsInclude;
    }

}