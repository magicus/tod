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
package tod.tools.parsers.workingset;

import java.util.ArrayList;
import java.util.List;

public class SetOperation
{
    /**
     * Whether this operation includes or excludes classes.
     */
    private boolean itsInclude;

    /**
     * List of {@link AbstractClassSet}
     */
    private List<AbstractClassSet> itsSubsets = new ArrayList<AbstractClassSet>();

    public SetOperation(String aInclude, List<AbstractClassSet> aSubsets)
    {
        if ("+".equals(aInclude)) itsInclude = true;
        else if ("-".equals(aInclude)) itsInclude = false;
        else throw new RuntimeException("Operation type should be + or -, got: " + aInclude);
        itsSubsets = aSubsets;
    }

    /**
     * Returns whether one of the subsets of this operation accepts the given
     * class.
     */
    public boolean accept(String aClassname)
    {
    	for (AbstractClassSet theSubset : itsSubsets)
		{
            if (theSubset.accept(aClassname)) return true;
        }
        return false;
    }

    public boolean isInclude()
    {
        return itsInclude;
    }

}