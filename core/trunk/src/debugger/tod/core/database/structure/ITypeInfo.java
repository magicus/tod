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
 * Represents a Java type (class, interface, primitive type).
 * @author gpothier
 */
public interface ITypeInfo extends ILocationInfo
{
	/**
	 * Returns the JVM type name for this type.
	 * Eg. "Ljava/lang/Object;", "I", ...
	 */
	public String getJvmName();
	
	/**
	 * Returns the number of JVM stack slots that an object of
	 * this type occupies.
	 * For instance, object reference is 1, long and double are 2, void is 0.
	 */
	public int getSize();

	/**
	 * Indicates if ths type is a primitive type.
	 */
	public boolean isPrimitive();

	/**
	 * Indicates if ths type is an array type.
	 */
	public boolean isArray();

	/**
	 * Indicates if ths type is the void type.
	 */
	public boolean isVoid();
	
	/**
	 * Creates a clone of this type info object that represents
	 * uncertain information.
	 */
	public IClassInfo createUncertainClone();
	

}