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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tod.core.database.structure.ITypeInfo;
import zz.utils.ListMap;

/**
 * Manages the set of available custom object formatters.
 * @see CustomObjectFormatter
 * @author gpothier
 */
public class CustomFormatterRegistry implements Serializable
{
	private static final long serialVersionUID = 3343639814828874536L;
	
	private List<CustomObjectFormatter> itsFormatters = new ArrayList<CustomObjectFormatter>();
	
	private ListMap<String, CustomObjectFormatter> itsFormattersMap =
		new ListMap<String, CustomObjectFormatter>();
	
	/**
	 * Returns the default formatter for the given type.
	 */
	public CustomObjectFormatter getFormatter(ITypeInfo aType)
	{
		List<CustomObjectFormatter> theList = itsFormattersMap.get(aType.getId());
		if (theList == null || theList.isEmpty()) return null;
		else return theList.get(0);
	}
	
	public CustomObjectFormatter createFormatter()
	{
		CustomObjectFormatter theFormatter = new CustomObjectFormatter(this);
		itsFormatters.add(theFormatter);
		return theFormatter;
	}
	
	/**
	 * Registers a formatter for the given type.
	 */
	void register(CustomObjectFormatter aFormatter, String aType)
	{
		itsFormattersMap.add(aType, aFormatter);
	}
	
	void unregister(CustomObjectFormatter aFormatter, String aType)
	{
		itsFormattersMap.remove(aType, aFormatter);
	}
}
