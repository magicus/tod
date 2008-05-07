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

import java.util.Map;

import tod.core.database.structure.IBehaviorInfo.BytecodeRole;

/**
 * Writable extension of {@link IStructureDatabase}.
 * @author gpothier
 */
public interface IMutableStructureDatabase extends IStructureDatabase
{
	/**
	 * This method either creates a new uninitialized class, or
	 * returns the latest added class of the specified name.
	 * If the class is created it is automatically assigned an id and added
	 * to the database.
	 */
	public IMutableClassInfo getNewClass(String aName);
	
	/**
	 * Adds a new class with a specific id.
	 * This is for platforms where ids are not assigned by the structure
	 * database (eg. python).
	 * Othwerwise, use {@link #getNewClass(String)}.
	 */
	public IMutableClassInfo addClass(int aId, String aName);
	
	/**
	 * Same as {@link #getType(String, boolean)}, but if the type is a class and
	 * does not exist, it is created as by {@link #getNewClass(String)}.
	 */
	public ITypeInfo getNewType(String aName);
	
	/**
	 * Override so as to provide mutable version.
	 */
	public IMutableClassInfo getClass(String aName, boolean aFailIfAbsent);
	
	/**
	 * Override so as to provide mutable version.
	 */
	public IMutableClassInfo getClass(int aId, boolean aFailIfAbsent);

	/**
	 * Creates a new probe and returns its id. 
	 */
	public int addProbe(int aBehaviorId, int aBytecodeIndex, BytecodeRole aRole, int aAdviceSourceId);
	
	/**
	 * Creates a new probe with the specified id.
	 */
	public void addProbe(int aId, int aBehaviorId, int aBytecodeIndex, BytecodeRole aRole, int aAdviceSourceId);
	
	/**
	 * Changes the probe info for the given id.
	 */
	public void setProbe(int aProbeId, int aBehaviorId, int aBytecodeIndex, BytecodeRole aRole, int aAdviceSourceId);
	
	/**
	 * Retrieves the probe at the given location, or create a new one if necessary.
	 * The probes created by this method should only be used for exception processing
	 * (when an exception generated event is received, we have no probe id, but we
	 * have behavior and bytecode index).
	 */
	public ProbeInfo getNewExceptionProbe(int aBehaviorId, int aBytecodeIndex);
	
	/**
	 * Sets the map that maps advice ids to source ranges for a given class.
	 * Several calls with overlapping advice ids can be made, provided there is no
	 * inconsistency.
	 */
	public void setAdviceSourceMap(Map<Integer, SourceRange> aMap);

}
