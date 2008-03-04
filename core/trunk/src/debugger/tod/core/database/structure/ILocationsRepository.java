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
package tod.core.database.structure;



/**
 * Permits to obtain the location info objects that have been registered during a 
 * {@link tod.core.session.ISession}.
 * @deprecated Replaced by {@link IStructureDatabase}
 * @author gpothier
 */
public interface ILocationsRepository
{
	/**
	 * Retrieves a type given its id.
	 */
	public ITypeInfo getType(int aId);
	
	/**
	 * Returns the type object that corresponds to the given name.
	 */
	public ITypeInfo getType(String aName);
	
	/**
	 * Returns all registered types.
	 */
	public Iterable<ITypeInfo> getTypes();
	
	/**
	 * Retrieves a field given its id.
	 */
	public IFieldInfo getField(int aFieldId);
	
	/**
	 * Returns all registered fields.
	 */
	public Iterable<IFieldInfo> getFields();
	
	/**
	 * Retrieves a behavior given its id.
	 */
	public IBehaviorInfo getBehavior(int aBehaviorId);
	
	/**
	 * Returns all registered behaviours.
	 */
	public Iterable<IBehaviorInfo> getBehaviours();
	
	/**
	 * Returns all registered files.
	 */
	public Iterable<String> getFiles();
}
