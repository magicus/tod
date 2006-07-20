package tod.tools.parsers.workingset;

/**
 * Base class for sets that reason only on package names
 * 
 * @author gpothier
 */
public abstract class AbstractPackageSet extends AbstractClassSet
{
    private String itsPackageName;

    public AbstractPackageSet(String aName)
    {
        itsPackageName = aName;
    }

    public final boolean accept(String aClassname)
    {
        int theIndex = aClassname.lastIndexOf('.');
        String thePackageName = theIndex >= 0 ? aClassname.substring(0, theIndex) : "";
        return acceptPackage(itsPackageName, thePackageName);
    }

    /**
     * Whether to accept or reject a class of a given package
     * 
     * @param aReferencePackage
     *            The package that was given in the constructor
     * @param aPackageName
     *            The package to accept or reject
     */
    protected abstract boolean acceptPackage(String aReferencePackage, String aPackageName);
}