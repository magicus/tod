/*
 * Created on Oct 9, 2004
 */
package tod.core;

import java.io.PrintStream;

/**
 * This enumeration permits to identify one of the standard outputs
 * (out or err).
 * @author gpothier
 */
public enum Output  
{
	OUT
	{
		public PrintStream get()
		{
			return System.out;
		}
		
		public void set(PrintStream aStream)
		{
			System.setOut(aStream);
		}
	}, 
	ERR
	{
		public PrintStream get()
		{
			return System.err;
		}
		
		public void set(PrintStream aStream)
		{
			System.setErr(aStream);
		}
	};

	
	public abstract PrintStream get();
	public abstract void set(PrintStream aStream);
}

