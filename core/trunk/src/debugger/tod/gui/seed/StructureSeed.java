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
import tod.core.database.structure.ILocationInfo;
import tod.gui.IGUIManager;
import tod.gui.view.structure.StructureView;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * Seed for {@link StructureView}
 * @author gpothier
 */
public class StructureSeed extends LogViewSeed
{
	private final IRWProperty<ILocationInfo> pSelectedLocation = new SimpleRWProperty<ILocationInfo>();

	public StructureSeed(IGUIManager aGUIManager, ILogBrowser aLog)
	{
		super(aGUIManager, aLog);
	}
	
	/**
	 * This property defines the currently selected location (class, method, etc.).
	 */
	public IRWProperty<ILocationInfo> pSelectedLocation()
	{
		return pSelectedLocation;
	}

	@Override
	protected StructureView requestComponent()
	{
		StructureView theView = new StructureView(getGUIManager(), getLogBrowser(), this);
		theView.init();
		return theView;
	}
	
	
}
