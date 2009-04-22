/*
 * Created on Apr 2, 2005
 */
package tod.tools.parsers.workingset;

import java.util.ArrayList;
import java.util.List;

public class CompoundClassSet extends AbstractClassSet
{
    /**
     * List of {@link SetOperation}
     */
    private List<SetOperation> itsOperations = new ArrayList<SetOperation>();

    public CompoundClassSet(List<SetOperation> aOperations)
    {
        itsOperations = aOperations;
    }

    public boolean accept(String aClassname)
    {
        SetOperation theFirstOperation = itsOperations.get(0);

        for (int i = itsOperations.size() - 1; i >= 0; i--)
        {
            SetOperation theOperation = itsOperations.get(i);

            if (theOperation.accept(aClassname)) return theOperation.isInclude();
        }

        // If the first operation is an include, default is accept nothing
        // If the first operation is an exclude, default is accept all.
        return !theFirstOperation.isInclude();
    }
}