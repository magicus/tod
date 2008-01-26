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
package tod.tools.formatting;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;
import tod.gui.formatter.CustomFormatterRegistry;

/**
 * Represents an object of the debugged VM at a given point in time.
 * @author gpothier
 */
public class ReconstitutedObject
{
	private final IGUIManager itsGUIManager;
	private final IObjectInspector itsInspector;
	private final IClassInfo itsClass;
	
	public ReconstitutedObject(IGUIManager aGUIManager, IObjectInspector aInspector)
	{
		itsGUIManager = aGUIManager;
		itsInspector = aInspector;
		itsClass = itsInspector != null ? (IClassInfo) itsInspector.getType() : null;
	}

	public Object get(String aFieldName)
	{
		IFieldInfo theField = itsClass.getField(aFieldName);
		Object[] theEntryValues = itsInspector.getEntryValue(theField);
		if (theEntryValues == null || theEntryValues.length > 1) throw new RuntimeException("What do we do? "+theEntryValues);
		
		Object theValue = theEntryValues[0];
		
		ILogBrowser theLogBrowser = itsInspector.getLogBrowser();
		
		// Check if this is a registered object.
		if (theValue instanceof ObjectId)
		{
			ObjectId theObjectId = (ObjectId) theValue;
			Object theRegistered = theLogBrowser.getRegistered(theObjectId);
			if (theRegistered != null) theValue = theRegistered;
		}

		if (theValue instanceof ObjectId)
		{
			ObjectId theObjectId = (ObjectId) theValue;
			IObjectInspector theInspector = theLogBrowser.createObjectInspector(theObjectId);
			theInspector.setReferenceEvent(itsInspector.getReferenceEvent());
			return FormatterFactory.getInstance().wrap(itsGUIManager, theInspector);
		}
		else 
		{
			return Hyperlinks.object(
					Hyperlinks.TEXT, 
					itsGUIManager, 
					itsGUIManager.getJobProcessor(),
					null,
					theValue,
					itsInspector.getReferenceEvent(), 
					false);
		}
	}
	
	/**
	 * Formats this reconstituted object using the custom formatters.
	 */
	public String format()
	{
		return CustomFormatterRegistry.formatObjectShort(itsGUIManager, itsInspector, false);
	}
	
	@Override
	public String toString()
	{
		if (itsInspector == null) return "Reconstitution of null";
		return "Reconstitution of "+itsInspector.getObject()+" at "+itsInspector.getReferenceEvent().getTimestamp();
	}
}
