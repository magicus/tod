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
 * Represents an in-construction {@link IClassInfo}.
 * @author gpothier
 */
public interface IMutableClassInfo extends IClassInfo, IMutableLocationInfo
{
	/**
	 * Sets up the basic information about this class.
	 * This operation can be performed only once (and throws
	 * an exception if it is called more than once).
	 */
	public void setup(
			boolean aIsInterface,
			boolean aIsInScope,
			String aChecksum, 
			IClassInfo[] aInterfaces,
			IClassInfo aSuperclass);
	
	/**
	 * Sets up the bytecode information of this class.
	 */
	public void setBytecode(byte[] aBytecode);
	
	/**
	 * This method either creates a new uninitialized behavior, or
	 * returns the behavior of the specified name/descriptor.
	 * If the behavior is created it is automatically assigned an id and added
	 * to the database.
	 * @param aDescriptor The descriptor (signature) of the behavior. 
	 * For now this is the ASM-provided descriptor.
	 */
	public IMutableBehaviorInfo getNewBehavior(String aName, String aDescriptor);
	
	/**
	 * Adds a new behavior with a specific id.
	 * This is for platforms where ids are not assigned by the structure
	 * database (eg. python).
	 * Othwerwise, use {@link #getNewBehavior(String, String)}.
	 */
	public IMutableBehaviorInfo addBehavior(int aId, String aName, String aDescriptor);
	
	/**
	 * This method either creates a new uninitialized field, or 
	 * returns the field that has the specified name.
	 * if the field is created it is automatically assigned an id and added
	 * to the database.
	 */
	public IMutableFieldInfo getNewField(String aName, ITypeInfo aType);

	/**
	 * Adds a new field with a specific id.
	 * This is for platforms where ids are not assigned by the structure
	 * database (eg. python).
	 * Othwerwise, use {@link #getNewField(String, ITypeInfo)}.
	 */
	public IMutableFieldInfo addField(int aId, String aName, ITypeInfo aType);
	
}
