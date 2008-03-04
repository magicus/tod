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
package tod.gui.formatter;

import tod.Util;
import tod.agent.BehaviorKind;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import zz.utils.AbstractFormatter;

/**
 * Formatter for {@link tod.core.database.structure.ILocationInfo}
 * @author gpothier
 */
public class LocationFormatter extends AbstractFormatter
{
	private static LocationFormatter INSTANCE = new LocationFormatter();

	public static LocationFormatter getInstance()
	{
		return INSTANCE;
	}

	private LocationFormatter()
	{
	}

	protected String getText(Object aObject, boolean aHtml)
	{
		if (aObject instanceof IFieldInfo)
		{
			IFieldInfo theInfo = (IFieldInfo) aObject;
			return "field "+theInfo.getName();
		}
		else if (aObject instanceof ITypeInfo)
		{
			ITypeInfo theInfo = (ITypeInfo) aObject;
			return "class/interface "+Util.getPrettyName(theInfo.getName());
		}
		else if (aObject instanceof IBehaviorInfo)
		{
			IBehaviorInfo theInfo = (IBehaviorInfo) aObject;
			BehaviorKind theBehaviourType = theInfo.getBehaviourKind();
			return theBehaviourType.getName() + " " + theInfo.getName();
		}
		else if (aObject instanceof IThreadInfo)
		{
			IThreadInfo theInfo = (IThreadInfo) aObject;
			String theName = theInfo.getName();
			return theName != null ? 
					"Thread "+theName+" ("+theInfo.getId()+")" 
					: "Thread ("+theInfo.getId()+")";
		}
		else return "Not handled: "+aObject; 
	}

}
