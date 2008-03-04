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
package tod.core.database.browser;


import tod.agent.BehaviorKind;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IStructureDatabase.LineNumberInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;


/**
 * Interface for objects that collect static code information, such as
 * methods, classes, fields, etc.
 * @deprecated Replaced by {@link IStructureDatabase}
 * @author gpothier
 */
public interface ILocationRegisterer 
{
	
	public void registerFile (
			int aFileId,
			String aFileName);
	
	public void registerType (
			int aTypeId,
			String aTypeName,
			int aSupertypeId,
			int[] aInterfaceIds);
	
	/**
	 * Registers a behavior.
	 * @param aBehaviourType Type of behavior (constructor, static init, method)
	 * @param aBehaviourId Id assigned to the behavior
	 * @param aTypeId Id of the type that declares the behavior
	 * @param aBehaviourName Name of the behavior
	 * @param aSignature JVM signature of the method.
	 */
	public void registerBehavior (
			BehaviorKind aBehaviourType,
			int aBehaviourId,
			int aTypeId,
			String aBehaviourName,
			String aSignature);

	/**
	 * Registers additional attributes of a behavior.
	 * These attributes cannot be registered at the same time the behavior is registered
	 * because the information might be unstable at that time.
	 */
	public void registerBehaviorAttributes (
			int aBehaviourId,
			LineNumberInfo[] aLineNumberTable,
			LocalVariableInfo[] aLocalVariableTable);
	
	public void registerField (
			int aFieldId,
			int aTypeId,
			String aFieldName);

}
