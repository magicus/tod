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
package tod.gui.seed;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.IGUIManager;
import tod.gui.view.LogView;
import tod.gui.view.ObjectInspectorView;
import zz.utils.properties.HashSetProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.ISetProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * Seed for the {@link tod.gui.view.ObjectInspectorView}
 * @author gpothier
 */
public class ObjectInspectorSeed extends LogViewSeed
{
	private ObjectId itsInspectedObject;
	
	private IRWProperty<Long> pSelectionStart = new SimpleRWProperty<Long>(this);
	private IRWProperty<Long> pSelectionEnd = new SimpleRWProperty<Long>(this);
	private IRWProperty<Long> pCurrentPosition = new SimpleRWProperty<Long>(this);
	
	private ISetProperty<IMemberInfo> pSelectedMembers = new HashSetProperty<IMemberInfo>(this);


	
	public ObjectInspectorSeed(IGUIManager aGUIManager, ILogBrowser aLog, ObjectId aInspectedObject)
	{
		super(aGUIManager, aLog);
		itsInspectedObject = aInspectedObject;
		
		pSelectionStart().set(getLogBrowser().getFirstTimestamp());
		pSelectionEnd().set(getLogBrowser().getLastTimestamp());
	}
	
	protected LogView requestComponent()
	{
		ObjectInspectorView theView = new ObjectInspectorView (getGUIManager(), getLogBrowser(), this);
		theView.init();
		return theView;
	}
	
	public ObjectId getInspectedObject()
	{
		return itsInspectedObject;
	}

	public IRWProperty<Long> pCurrentPosition()
	{
		return pCurrentPosition;
	}

	public IRWProperty<Long> pSelectionEnd()
	{
		return pSelectionEnd;
	}

	public IRWProperty<Long> pSelectionStart()
	{
		return pSelectionStart;
	}

	public ISetProperty<IMemberInfo> pSelectedMembers()
	{
		return pSelectedMembers;
	}
	
	
}
