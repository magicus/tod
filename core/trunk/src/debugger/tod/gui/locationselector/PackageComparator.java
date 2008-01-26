package tod.gui.locationselector;

import java.util.Comparator;

import tod.core.database.structure.ILocationInfo;
import zz.utils.tree.SimpleTreeNode;

/**
 * Compares packages and classes.
 * Packages are always before classes, otherwise lexicographic order is used.
 * @author gpothier
 */
public class PackageComparator implements Comparator
{
	public static PackageComparator PACKAGE = new PackageComparator(true);
	public static PackageComparator CLASS = new PackageComparator(false);

	/**
	 * If true, compares against package names (package names always appear before
	 * class names).
	 */
	private boolean itsForPackage;
	
	private PackageComparator(boolean aForPackage)
	{
		itsForPackage = aForPackage;
	}
	
	public int compare(Object o1, Object o2)
	{
		SimpleTreeNode<ILocationInfo> node = (SimpleTreeNode<ILocationInfo>) o1;
		String name = (String) o2;
		
		ILocationInfo l = node.pValue().get();
		boolean p = l instanceof PackageInfo;
		
		if (p != itsForPackage) return p ? 1 : -1;
		else return l.getName().compareTo(name);
	}
}