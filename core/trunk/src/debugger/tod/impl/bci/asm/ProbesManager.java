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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;

import tod.core.database.structure.IMutableStructureDatabase;

/**
 * Manages the probes that are created during the 
 * instrumentation of a behavior.
 * The temporary probes created by this manager have a {@link Label}
 * instead of a concrete bytecode index so that the calculation
 * of the concrete bytecode index can be done after the instrumentation is
 * finished. 
 * @author gpothier
 */
public class ProbesManager
{
	private final IMutableStructureDatabase itsStructureDatabase;
	private List<TmpProbeInfo> itsProbes = new ArrayList<TmpProbeInfo>();
	
	public ProbesManager(IMutableStructureDatabase aStructureDatabase)
	{
		itsStructureDatabase = aStructureDatabase;
	}
	
	/**
	 * Creates a new probe.
	 */
	public int createProbe(Label aLabel)
	{
		int theId = itsStructureDatabase.addProbe(-1, -1, -1);
		itsProbes.add(new TmpProbeInfo(theId, aLabel));
		return theId;
	}
	
	public List<TmpProbeInfo> getProbes()
	{
		return itsProbes;
	}
	
	public static class TmpProbeInfo
	{
		public final int id;
		public final Label label;

		public TmpProbeInfo(int aId, Label aLabel)
		{
			id = aId;
			label = aLabel;
		}
	}




}
