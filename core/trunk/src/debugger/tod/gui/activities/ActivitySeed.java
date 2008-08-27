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
package tod.gui.activities;

import tod.core.database.browser.ILogBrowser;

/**
 * A seed contains all the information needed to generate a view.
 * A seed can be in two states: inactive and active. When a seed
 * is active, it maintains a reference to a view component.
 * As many seeds can be stored in the browsing history of the GUI,
 * an inactive seed should keep as few references as possible to other
 * objects, so as to free resources. 
 * @author gpothier
 */
public abstract class ActivitySeed
{
	private ILogBrowser itsLog;
	
	public ActivitySeed(ILogBrowser aLog)
	{
		itsLog = aLog;
	}
	
	public ILogBrowser getLogBrowser()
	{
		return itsLog;
	}
	
	/**
	 * Returns the class of the component that is capable of displaying this seed. 
	 */
	public abstract Class<? extends ActivityPanel> getComponentClass();
	
	/**
	 * A description of the kind (class) of this seed.
	 * Should not include the details of this particular seed.
	 */
	public abstract String getKindDescription();

	/**
	 * A short description for this seed, should not describe the seed kind 
	 * but only the specifics of this particular seed.
	 */
	public abstract String getShortDescription();
}
