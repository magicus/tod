package tod.gui.view.structure;

import java.util.Comparator;

import tod.Util;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IMemberInfo;
import zz.utils.tree.SimpleTreeNode;

/**
 * Compares class members
 * Fields are always before behaviors, otherwise lexicographic order is used.
 * @author gpothier
 */
public class MemberComparator implements Comparator
{
	public static MemberComparator FIELD = new MemberComparator(true);
	public static MemberComparator BEHAVIOR = new MemberComparator(false);

	/**
	 * If true, compares against package names (package names always appear before
	 * class names).
	 */
	private boolean itsForField;
	
	private MemberComparator(boolean aForPackage)
	{
		itsForField = aForPackage;
	}
	
	public int compare(Object o1, Object o2)
	{
		SimpleTreeNode<ILocationInfo> node = (SimpleTreeNode<ILocationInfo>) o1;
		
		ILocationInfo l = node.pValue().get();
		boolean f = l instanceof IFieldInfo;
		String n1 = Util.getFullName((IMemberInfo) l);
			
		String n2 = (String) o2;
		
		if (f != itsForField) return f ? 1 : -1;
		else return n1.compareTo(n2);
	}
}