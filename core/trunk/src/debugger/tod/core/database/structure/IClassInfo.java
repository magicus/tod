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
package tod.core.database.structure;


/**
 * Represents a Java class or interface.
 * @author gpothier
 */
public interface IClassInfo extends ITypeInfo
{
	/**
	 * Returns the bytecode of the (instrumented version of the) class.
	 */
	public byte[] getBytecode();
	
	/**
	 * Whether this object represents an interface, or a class.
	 */
	public boolean isInterface();
	
	/**
	 * Whether this class is in the instrumentation scope.
	 */
	public boolean isInScope();
	
	/**
	 * Returns the MD5 checksum of the class' original bytecode.
	 */
	public String getChecksum();
	
	/**
	 * Indicates the time at which this particular version
	 * of the class has been loaded into the system.
	 * This is important for cases where class redefinition is used.
	 * TODO: Consolidate this. We should have a map of all currently connected
	 * VMs with the version of the classes they use.
	 * @return A timestamp, as measured by the debugged VM.
	 */
	public long getStartTime();
	
	/**
	 * Returns the superclass of this class.
	 */
	public IClassInfo getSupertype();
	
	/**
	 * Returns all the interfaces directly implemented by this class.
	 * The returned list is immutable.
	 */
	public IClassInfo[] getInterfaces();
	
	/**
	 * Searches a field
	 * @param aName Name of the searched field.
	 * @return The field, or null if not found.
	 */
	public IFieldInfo getField(String aName);

	/**
	 * Searches a behavior according to its signature.
	 */
	public IBehaviorInfo getBehavior(String aName, ITypeInfo[] aArgumentTypes);

	/**
	 * Returns all the fields of this class (excluding inherited ones).
	 */
	public Iterable<IFieldInfo> getFields();

	/**
	 * Returns all the behaviors of this class (excluding inherited ones).
	 */
	public Iterable<IBehaviorInfo> getBehaviors();
	
}