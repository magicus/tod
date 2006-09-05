package btree;
/*
 * Created on May 1, 2006
 */

public class Config
{
	/**
	 * Branching factor of the BTree.
	 */
	private static int t = 0;
	
	public static void setT(int aT)
	{
		t = aT;
	}
	
	public static int t()
	{
		return t;
	}
	
	public static int maxKeys()
	{
		return 2*t() - 1;
	}
	
	public static int minKeys()
	{
		return t() - 1;
	}
	
	public static int maxChildren()
	{
		return 2*t();
	}
	
	public static int minChildren()
	{
		return t();
	}
	
	public static int pageSize()
	{
		return 4 // keys count
			+ 1 // leaf flag
			+ 16 * maxKeys() // keys and values
			+ 4 * maxChildren(); // children pointers
	}
}
