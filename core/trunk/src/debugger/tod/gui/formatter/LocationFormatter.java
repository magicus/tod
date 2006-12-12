/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui.formatter;

import tod.core.BehaviourKind;
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
			return "class/interface "+theInfo.getName();
		}
		else if (aObject instanceof IBehaviorInfo)
		{
			IBehaviorInfo theInfo = (IBehaviorInfo) aObject;
			BehaviourKind theBehaviourType = theInfo.getBehaviourKind();
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
