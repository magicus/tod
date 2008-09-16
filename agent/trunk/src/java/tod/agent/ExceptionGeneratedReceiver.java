/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.agent;

/**
 * This class provides a method that is called by the JNI side when
 * an exception is generated.
 * @author gpothier
 */
public class ExceptionGeneratedReceiver
{
	static
	{
		AgentConfig.getCollector();
	}
	
	/**
	 * Sets the ignore next exception flag of the current thread.
	 * This is called by instrumented classes.
	 */
	public static void ignoreNextException()
	{
		if (AgentReady.COLLECTOR_READY) AgentConfig.getCollector().ignoreNextException();
	}
	
	public static void exceptionGenerated(
			String aMethodName,
			String aMethodSignature,
			String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex,
			Throwable aThrowable)
	{
		try
		{
//			System.out.println(String.format("Exception generated: %s.%s, %d", aMethodDeclaringClassSignature, aMethodName, aOperationBytecodeIndex));
			if (AgentReady.COLLECTOR_READY) AgentConfig.getCollector().logExceptionGenerated(
					aMethodName,
					aMethodSignature, 
					aMethodDeclaringClassSignature,
					aOperationBytecodeIndex, 
					aThrowable);
		}
		catch (Throwable e)
		{
			System.err.println("[TOD] Exception in ExceptionGeneratedReceiver.exceptionGenerated:");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
