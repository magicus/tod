/*
 * Created on Oct 24, 2005
 */
package tod.bci;

import tod.agent.AgentReady;
import tod.core.IIdentifiableObject;

public class IdentifiableImpl implements IIdentifiableObject
{
	private int __log_uid;
	

	public IdentifiableImpl()
	{
		System.out.println("IdentifiableImpl.IdentifiableImpl()");
	}



	public long __log_uid()
	{
		IdentifiableImpl theImpl = new IdentifiableImpl();
		return -1;
	}
	
	public void foo2() throws Exception
	{
	    if (AgentReady.READY)
	    {
	    	System.out.println();
	    }
	    System.out.println();
	}
	
	public void foo() throws Exception 
	{
	    try
	    {
	    	foo2();
	    	return;
	    }
	    finally
	    {
	        foo();
	    }
	}
}
