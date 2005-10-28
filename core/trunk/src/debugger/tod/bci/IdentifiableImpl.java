/*
 * Created on Oct 24, 2005
 */
package tod.bci;

import reflex.lib.logging.core.impl.mop.Config;
import reflex.lib.logging.core.impl.mop.identification.ObjectIdentifier;
import tod.agent.AgentConfig;
import tod.agent.AgentReady;
import tod.core.IIdentifiableObject;
import tod.core.ILogCollector;
import tod.core.IdGenerator;

public class IdentifiableImpl implements IIdentifiableObject
{
	private int __log_uid;
	

	public IdentifiableImpl()
	{
		if (__log_uid == 0) __log_uid = IdGenerator.createIntId();
	}



	public long __log_uid()
	{
		String s = new String("x");
		System.out.println(s);
		new String ("y");
		return __log_uid;
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
