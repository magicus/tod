package tod.tools.parsers.workingset;

/**
 * A set that accepts all the classes of a package and its sub-packages.
 * 
 * @author gpothier
 */
public class RecursivePackageSet extends AbstractPackageSet
{
    public RecursivePackageSet(String aName)
    {
        super(aName);
    }

    protected boolean acceptPackage(String aReferencePackage, String aPackageName)
    {
        return aPackageName.startsWith(aReferencePackage);
    }
}