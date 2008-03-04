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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tod.Util;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.structure.ITypeInfo;
import tod.gui.IGUIManager;
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
		List<CustomObjectFormatter> theList = itsFormattersMap.get(aType.getName());
		if (theList == null || theList.isEmpty()) return null;
		else return theList.get(0);
	}
	
	/**
	 * Returns the list of formatters.
	 */
	public List<CustomObjectFormatter> getFormatters()
	{
		return itsFormatters;
	}
	
	public CustomObjectFormatter createFormatter()
	{
		CustomObjectFormatter theFormatter = new CustomObjectFormatter(this);
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
	
	/**
	 * Formats the given object using the formatters/log browser of the given gui manager.
	 */
	public static String formatObjectShort(
			IGUIManager aGUIManager, 
			IObjectInspector aInspector,
			boolean aShowPackageNames)
	{
		ITypeInfo theType = aInspector.getType();
		
		CustomFormatterRegistry theRegistry = aGUIManager.getCustomFormatterRegistry();
		CustomObjectFormatter theFormatter = theRegistry.getFormatter(theType);
		
		if (theFormatter != null)
		{
			return theFormatter.formatShort(aGUIManager, aInspector);
		}
		else
		{		
			String theName = aShowPackageNames ? theType.getName() : Util.getSimpleName(theType.getName());
			return theName + " (" + aInspector.getObject().getId() + ")";
		}
	}

}
