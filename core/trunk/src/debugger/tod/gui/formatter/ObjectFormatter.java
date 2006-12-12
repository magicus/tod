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

import tod.core.database.structure.ObjectId;
import zz.utils.AbstractFormatter;

/**
 * @author gpothier
 */
public class ObjectFormatter extends AbstractFormatter
{
	private static ObjectFormatter INSTANCE = new ObjectFormatter();

	public static ObjectFormatter getInstance()
	{
		return INSTANCE;
	}

	private ObjectFormatter()
	{
	}

	protected String getText(Object aObject, boolean aHtml)
	{
		if (aObject == null) return "null";
		else if (aObject instanceof ObjectId.ObjectHash)
		{
			ObjectId.ObjectHash theHash = (ObjectId.ObjectHash) aObject;
			return "Object (hash: "+theHash.getHascode()+")";
		}
		else if (aObject instanceof ObjectId.ObjectUID)
		{
			ObjectId.ObjectUID theObjectUID = (ObjectId.ObjectUID) aObject;
			return "Object (uid: "+theObjectUID.getId()+")";
		}
		else return ""+aObject;
	}

}
