/*
 * Created on Nov 19, 2007
 */
package tod.agent;

/**
 * Copied from zz.utils.
 * Implements a fixed-size stack of primitive integers.
 * @author gpothier
 */
public class FixedIntStack
{
	private int[] itsData;
	private int itsHeight;
	
	public FixedIntStack(int aInitialSize)
	{
		itsData = new int[aInitialSize];
		itsHeight = 0;
	}
	
	public void push(int aValue)
	{
		itsData[itsHeight++] = aValue;
	}
	
	public int pop()
	{
		return itsData[--itsHeight];
	}
	
	public boolean isEmpty()
	{
		return itsHeight == 0;
	}
	
	public void clear()
	{
		itsHeight = 0;
	}
}
