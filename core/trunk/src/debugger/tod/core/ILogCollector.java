/*
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
package tod.core;

import tod.agent.HighLevelCollector;
import tod.agent.Output;

/**
 * Interface for incoming events. It closely matches {@link HighLevelCollector}.
 * @author gpothier
 */
public interface ILogCollector
{
	public void methodCall(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aProbeId,
			boolean aDirectParent,
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget,
			Object[] aArguments);
	
	public void instantiation(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aProbeId,
			boolean aDirectParent,
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget,
			Object[] aArguments);
	
	public void superCall(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aProbeId,
			boolean aDirectParent,
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget,
			Object[] aArguments);
	
	public void behaviorExit(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aProbeId,
			int aBehaviorId,
			boolean aHasThrown,
			Object aResult);
	
	public void fieldWrite(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aProbeId,
			int aFieldId,
			Object aTarget,
			Object aValue);
	
	public void newArray(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aProbeId,
			Object aTarget,
			int aBaseTypeId,
			int aSize);
	
	public void arrayWrite(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aProbeId,
			Object aTarget,
			int aIndex,
			Object aValue);
	
	public void localWrite(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aProbeId,
			int aVariableId,
			Object aValue);
	
	public void exception(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			String aMethodName, 
			String aMethodSignature,
			String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex,
			Object aException);
	
	public void output(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			Output aOutput,
			byte[] aData);
	
	public void thread(
			int aThreadId, 
			long aJVMThreadId,
			String aName);
	
	/**
	 * Registers an object whose state cannot be otherwise determined (eg String, Exception)
	 */
	public void register(long aObjectUID, Object aObject, long aTimestamp);
}
