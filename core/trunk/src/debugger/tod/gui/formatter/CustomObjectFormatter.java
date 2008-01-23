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
import java.util.HashSet;
import java.util.Set;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.structure.ITypeInfo;
import tod.tools.formatting.FormatterFactory;
import tod.tools.formatting.IPyObjectFormatter;
import tod.tools.formatting.ReconstitutedObject;

/**
 * An object formatter defined by the user.
 * @author gpothier
 */
public class CustomObjectFormatter implements Serializable
{
	private static final long serialVersionUID = 3583639814851031766L;
	
	private final CustomFormatterRegistry itsRegistry;
	
	private String itsName;
	
	/**
	 * A Python code snippet that performs the formatting
	 * (short version, should only output one line).
	 * @see FormatterFactory
	 */
	private String itsShortCode;
	
	private transient IPyObjectFormatter itsPyShortFormatter;
	
	/**
	 * A Python code snippet that performs the formatting.
	 * (optional long version, can output multiple lines.)
	 * @see FormatterFactory
	 */
	private String itsLongCode;
	
	private transient IPyObjectFormatter itsPyLongFormatter;
	
	/**
	 * The names of the types handled by this formatter.
	 * We keep names instead of {@link ITypeInfo} because the id of types
	 * can change from one session to the next.
	 */
	private Set<String> itsRecognizedTypes = new HashSet<String>();

	CustomObjectFormatter(CustomFormatterRegistry aRegistry)
	{
		itsRegistry = aRegistry;
	}

	public String getName()
	{
		return itsName;
	}

	public void setName(String aName)
	{
		itsName = aName;
	}


	public String getShortCode()
	{
		return itsShortCode;
	}

	public void setShortCode(String aShortCode)
	{
		itsShortCode = aShortCode;
	}

	public String getLongCode()
	{
		return itsLongCode;
	}

	public void setLongCode(String aLongCode)
	{
		itsLongCode = aLongCode;
	}

	public Iterable<String> getRecognizedTypes()
	{
		return itsRecognizedTypes;
	}

	public void addRecognizedType(String aType)
	{
		if (itsRecognizedTypes.add(aType)) itsRegistry.register(this, aType);
	}
	
	public void removeRecognizedType(String aType)
	{
		if (itsRecognizedTypes.remove(aType)) itsRegistry.register(this, aType);
	}
	
	/**
	 * Returns one of the formatters.
	 */
	private IPyObjectFormatter getFormatter(boolean aLong)
	{
		if (aLong && itsLongCode != null)
		{
			if (itsPyLongFormatter == null)
				itsPyLongFormatter = FormatterFactory.getInstance().createFormatter(itsLongCode);
			return itsPyLongFormatter;
		}
		else
		{
			if (itsPyShortFormatter == null)
				itsPyShortFormatter = FormatterFactory.getInstance().createFormatter(itsShortCode);
			return itsPyShortFormatter;
		}
	}
	
	/**
	 * Formats the given object, using the short formatter.
	 */
	public String formatShort(ILogBrowser aLogBrowser, IObjectInspector aInspector)
	{
		return getFormatter(false).format(new ReconstitutedObject(aInspector));
	}
	
	/**
	 * Formats the given object, using the long formatter.
	 */
	public String formatLong(ILogBrowser aLogBrowser, IObjectInspector aInspector)
	{
		return getFormatter(true).format(new ReconstitutedObject(aInspector));
	}
	
}
