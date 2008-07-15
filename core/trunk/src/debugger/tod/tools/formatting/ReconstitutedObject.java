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
package tod.tools.formatting;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.browser.ICompoundInspector.EntryValue;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;
import tod.gui.formatter.CustomFormatterRegistry;
import zz.utils.Utils;

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
		if (theField == null) 
		{
			if (itsClass.isInScope()) Utils.rtex("Field %s not found in class %s.", aFieldName, itsClass.getName());
			else Utils.rtex("Class %s is not in scope, cannot access field %s.", itsClass.getName(), aFieldName);
		}
		
		EntryValue[] theEntryValues = itsInspector.getEntryValue(theField);
		if (theEntryValues == null || theEntryValues.length > 1) throw new RuntimeException("What do we do? "+theEntryValues);
		
		Object theValue = theEntryValues.length == 1 ? theEntryValues[0].value : null;
		
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
		else if (theValue != null)
		{
			return Hyperlinks.object(
					itsGUIManager, 
					Hyperlinks.TEXT, 
					itsGUIManager != null ? itsGUIManager.getJobProcessor() : null,
					null,
					theValue,
					itsInspector.getReferenceEvent(), 
					false);
		}
		else return "null";
	}
	
	/**
	 * Formats this reconstituted object using the custom formatters.
	 */
	public String format()
	{
		return CustomFormatterRegistry.formatObjectShort(itsGUIManager, itsInspector, false);
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if (aObj instanceof ReconstitutedObject)
		{
			ReconstitutedObject theOther = (ReconstitutedObject) aObj;
			return theOther.itsInspector.getObject().equals(itsInspector.getObject());
		}
		else return false;
	}
	
	@Override
	public String toString()
	{
		if (itsInspector == null) return "Reconstitution of null";
		return "Reconstitution of "+itsInspector.getObject()+" at "+itsInspector.getReferenceEvent().getTimestamp();
	}
}
