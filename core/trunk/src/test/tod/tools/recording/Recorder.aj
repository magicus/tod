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
package tod.tools.recording;

import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

import tod.core.DebugFlags;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ExternalPointer;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.core.database.structure.IStructureDatabase;

/**
 * Records the call to database primitives so that they can be later
 * replayed (for benchmarking).
 * @author gpothier
 */
public aspect Recorder
{
	pointcut recordedCall(Object t):
		(call(* ILogBrowser.*(..))
		|| call(* IEventBrowser.*(..))
		|| call(* IStructureDatabase.*(..))
		|| call(* IParentEvent.getChildrenBrowser())
		|| call(* ILogEvent.getHost())
		|| call(* ILogEvent.getThread())
		|| call(* ILogEvent.getParent())
		|| call(* ILogEvent.getParentPointer())
		|| call(* ILogEvent.getPointer())
		|| call(* ExternalPointer.*(..))
		|| call(* tod.impl.dbgrid.event.BehaviorCallEvent.CallInfo.*(..))
		)
		&& target(t);
	
	after(Object t) returning(Object r): recordedCall(t)
	{
		if (DebugFlags.TRACE_DBCALLS)
		{
			MethodSignature theSignature = (MethodSignature) thisJoinPoint.getSignature();
			RecorderHelper.getInstance().recordCall(
					t, 
					theSignature.getName(),
					theSignature.getParameterTypes(),
					thisJoinPoint.getArgs(), 
					r,
					""+thisJoinPoint.getSourceLocation());
		}
	}
	
	pointcut recordedConstructor():
		call(tod.impl.dbgrid.event.BehaviorCallEvent.CallInfoBuilder.new(..));
	
	after() returning(Object r): recordedConstructor()
	{
		if (DebugFlags.TRACE_DBCALLS)
		{
			ConstructorSignature theSignature = (ConstructorSignature) thisJoinPoint.getSignature();
			RecorderHelper.getInstance().recordNew(
					theSignature.getDeclaringTypeName(),
					theSignature.getParameterTypes(),
					thisJoinPoint.getArgs(), 
					r,
					""+thisJoinPoint.getSourceLocation());
		}
	}
	

}
