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
package tod.gui.seed;

import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.IGUIManager;

/**
 * A factory of {@link LogViewSeed}s.
 * 
 * @author gpothier
 */
public class LogViewSeedFactory 
{
	private static LogViewSeed createSeed(
			IGUIManager aGUIManager,
			ILogBrowser aLog,
			String aTitle,
			IEventFilter aFilter)
	{
		return new FilterSeed(aGUIManager, aLog, aTitle, aFilter);
	}
	
	/**
	 * Returns a seed that can be used to view the events that are related to
	 * the specified location info.
	 */
	public static LogViewSeed getDefaultSeed(
			IGUIManager aGUIManager,
			ILogBrowser aLog,
			ILocationInfo aInfo)
	{
		if (aInfo instanceof ITypeInfo)
		{
			ITypeInfo theTypeInfo = (ITypeInfo) aInfo;
			return createSeed(
					aGUIManager, 
					aLog,
					"Instantiations of "+theTypeInfo.getName(),
					aLog.createInstantiationsFilter(theTypeInfo));
		}
		else if (aInfo instanceof IBehaviorInfo)
		{
			IBehaviorInfo theBehaviourInfo = (IBehaviorInfo) aInfo;
			return createSeed(
					aGUIManager, 
					aLog, 
					"Calls of "+theBehaviourInfo.getName(),
					aLog.createBehaviorCallFilter(theBehaviourInfo));
		}
		else if (aInfo instanceof IFieldInfo)
		{
			IFieldInfo theFieldInfo = (IFieldInfo) aInfo;

			return createSeed(
					aGUIManager, 
					aLog, 
					"Assignments of "+theFieldInfo.getName(),
					aLog.createFieldFilter(theFieldInfo));
		}
		else return null;
	}
}
