package tod.tools.parsers.workingset;

/**
 * This class set accepts a single class
 */
public class SingleClassSet extends AbstractClassSet
{
    private String itsClassName;

    public SingleClassSet(String aName)
    {
        itsClassName = aName;
    }

    public boolean accept(String aClassname)
    {
        return itsClassName.equals(aClassname);
    }
}