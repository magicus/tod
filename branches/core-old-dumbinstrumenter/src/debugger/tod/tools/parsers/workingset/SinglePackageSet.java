package tod.tools.parsers.workingset;

/**
 * A set that accepts all the classes of a given package.
 * 
 * @author gpothier
 */
public class SinglePackageSet extends AbstractPackageSet
{
    public SinglePackageSet(String aName)
    {
        super(aName);
    }

    protected boolean acceptPackage(String aReferencePackage, String aPackageName)
    {
        return aReferencePackage.equals(aPackageName);
    }
}