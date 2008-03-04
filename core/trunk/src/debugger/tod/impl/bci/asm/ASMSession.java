/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.bci.asm;



public class ASMSession //extends AbstractSession
{
//	private final ASMDebuggerConfig itsConfig;
//	private final ILogCollector itsCollector;
//	
//	private final ILogBrowser itsBrowser;
//	private final ILocationsRepository itsLocationsRepository;
//	
//	private MyLogReceiver itsLogReceiver;
//	private MyNativePeer itsNativePeer;
//
//	private String itsCachedClassesPath;
//	private File itsCachedLocationsPath;
//
//	public ASMSession(
//			URI aUri, 
//			String aGlobalWorkingSet,
//			String aIdentificationWorkingSet,
//			String aTraceWorkingSet,
//			ILogCollector aCollector,
//			ILogBrowser aBrowser,
//			ILocationsRepository aLocationsRepository)
//	{
//		super(aUri);
//		
//		itsCollector = aCollector;
//		itsBrowser = aBrowser;
//		itsLocationsRepository = aLocationsRepository;
//		
//		if (aUri != null)
//		{
//			File theCachePath = new File(aUri);
//			itsCachedClassesPath = theCachePath.getPath();
//			
//			itsCachedLocationsPath = new File(theCachePath, "loc.dat");
//		}
//		
//		itsConfig = new ASMDebuggerConfig(
//				new PrintThroughCollector(itsCollector),
////				itsCollector,
//				itsCachedLocationsPath, 
//				"[-tod.** -remotebci.** +tod.test.** +tod.demo.**]",
//				"[-java.** -javax.** -sun.** -com.sun.**]");
//
//		try
//		{
//			itsLogReceiver = new MyLogReceiver (itsConfig, new ServerSocket(8058));
//			itsNativePeer = new MyNativePeer(8059);
//		}
//		catch (IOException e)
//		{
//			throw new RuntimeException(e);
//		}
//	}
//	
//	public ILogBrowser getLogBrowser()
//	{
//		return itsBrowser;
//	}
//
//	public ILocationsRepository getLocations()
//	{
//		return itsLocationsRepository;
//	}
//	
//	public void disconnect()
//	{
//		itsNativePeer.interrupt();
//		itsLogReceiver.interrupt();
//	}
//	
//	public String getCachedClassesPath()
//	{
//		return itsCachedClassesPath;
//	}
//
//	private class MyNativePeer extends NativeAgentPeer
//	{
//		private static final int EXCEPTION_GENERATED = 20;
//		private static final byte OBJECT_HASH = 1;
//		private static final byte OBJECT_UID = 2;
//
//		private boolean itsFinished = false;
//		
//		public MyNativePeer(int aPort) throws IOException
//		{
//			super (aPort, true, null, itsConfig.getInstrumenter());
//		}
//
//		@Override
//		protected String cfgCachePath()
//		{
//			return getCachedClassesPath();
//		}
//		
//		@Override
//		protected boolean cfgSkipCoreClasses()
//		{
//			return false;
//		}
//		
//		@Override
//		protected void processExceptionGenerated(
//				long aTimestamp, 
//				long aThreadId,
//				String aClassName,
//				String aMethodName, 
//				String aMethodSignature, 
//				int aBytecodeIndex,
//				Object aException)
//		{
//			int theTypeId = itsConfig.getLocationPool().getTypeId(aClassName);
//			int theBehaviorId = itsConfig.getLocationPool().getBehaviorId(
//					theTypeId, 
//					aMethodName, 
//					aMethodSignature);
//			
//			itsConfig.getCollector().logExceptionGenerated(
//					aTimestamp, 
//					aThreadId, 
//					theBehaviorId, 
//					aBytecodeIndex, 
//					aException);
//		}
//
//		@Override
//		protected boolean accept()
//		{
//			return ! itsFinished;
//		}
//		
//		@Override
//		protected void disconnected()
//		{
//			itsFinished = true;
//			disconnect();
//		}
//	}
//	
//	private class MyLogReceiver extends LogReceiver
//	{
//		private boolean itsFinished = false;
//
//		public MyLogReceiver(ASMDebuggerConfig aConfig, ServerSocket aServerSocket)
//		{
//			super (aConfig.getCollector(), aServerSocket);
//		}
//		
//		@Override
//		protected boolean accept()
//		{
//			return ! itsFinished;
//		}
//		
//		@Override
//		protected void disconnected()
//		{
//			itsFinished = true;
//			disconnect();
//		}
//	}
}