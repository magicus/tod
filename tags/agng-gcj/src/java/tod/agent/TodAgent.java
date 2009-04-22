/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.agent;

public class TodAgent
{
	private static boolean itsVmStarted = false;
	private static NativeAgentConfig itsConfig;
	
	private static ExceptionProcessor itsExceptionProcessor;
	private static InstrumentationManager itsInstrumentationManager;
	private static ConnectionManager itsConnectionManager;
	
	private static long itsNextOid = 1;
	
	public static void agOnLoad(
			String aPropHost,
			String aPropHostName,
			String aPropNativePort,
			String aPropCachePath,
			int aPropVerbose)
	{
		System.out.println("[TOD] TodAgent: OnLoad ("+aPropHost+", "+aPropHostName+")");
		itsConfig = new NativeAgentConfig();
		itsConfig.setHost(aPropHost);
		itsConfig.setHostName(aPropHostName);
		itsConfig.setNativePort(Integer.parseInt(aPropNativePort));
		itsConfig.setCachePath(aPropCachePath);
		itsConfig.setVerbosity(aPropVerbose);
		
		itsConnectionManager = new ConnectionManager(itsConfig);
		itsConnectionManager.connect();
		
		itsExceptionProcessor = new ExceptionProcessor(itsConfig);
		itsInstrumentationManager = new InstrumentationManager(itsConfig, itsConnectionManager);
	}
	
	public static void agOnUnload()
	{
		itsConnectionManager.sendFlush();
	}
	
	public static void agVMStart(long aJniEnv)
	{
		System.out.println("[TOD] TodAgent: VMStart");
		itsVmStarted = true;
		itsInstrumentationManager.flushTmpTracedMethods(aJniEnv);
	}	

	/**
	 * Transforms the given class.
	 * @param aName Name of the class
	 * @param aOriginal Original bytecode
	 * @return The new bytecode, or null if the class should not be instrumented.
	 */
	public static byte[] agClassLoadHook(long aJniEnv, String aName, byte[] aOriginal)
	{
		return itsInstrumentationManager.instrument(aJniEnv, aName, aOriginal);
	}
	
	public static void agExceptionGenerated(long aJniEnv, long aMethodId, int aLocation, long aThrowable)
	{
		MethodInfo theMethodInfo = VMUtils.jvmtiGetMethodInfo(aMethodId);
		
		if (isVmStarted())
		{
			itsExceptionProcessor.agExceptionGenerated(aJniEnv, theMethodInfo, aLocation, aThrowable);
		}
		else
		{
			itsConfig.log(3, "VM not started yet, not sending exception");
		}
	}
	
	public static boolean isVmStarted()
	{
		return itsVmStarted;
	}
	
	/**
	 * Returns the next object id
	 */
	public synchronized static long agGetNextOid()
	{
		long oid = itsNextOid++;
		
		// Include host id
		oid = (oid << itsConfig.getHostBits()) | itsConfig.getHostId(); 
		
		// We cannot use the 64th bit.
		if (oid >> 63 != 0) 
		{
			System.err.println("OID overflow");
			System.exit(1);
		}

		return oid;
	}
	
	public static int agGetHostId()
	{
		return itsConfig.getHostId();
	}
}
