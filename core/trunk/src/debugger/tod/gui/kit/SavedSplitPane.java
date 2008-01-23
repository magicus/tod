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
package tod.gui.kit;

import javax.swing.JSplitPane;

import tod.gui.IGUIManager;
import tod.gui.MinerUI;

/**
 * A split pane that saves its divider location in a {@link IGUIManager}
 * @author gpothier
 */
public class SavedSplitPane extends JSplitPane
{
	private final IGUIManager itsGUIManager;
	private final String itsPropertyName;


	public SavedSplitPane(IGUIManager aManager, String aPropertyName)
	{
		itsGUIManager = aManager;
		itsPropertyName = aPropertyName;
	}

	
	public SavedSplitPane(int aNewOrientation, IGUIManager aManager, String aPropertyName)
	{
		super(aNewOrientation);
		itsGUIManager = aManager;
		itsPropertyName = aPropertyName;
	}


	@Override
	public void addNotify()
	{
		int theSplitterPos = MinerUI.getIntProperty(itsGUIManager, itsPropertyName, 400);
		setDividerLocation(theSplitterPos);
		
		super.addNotify();
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		
		itsGUIManager.setProperty(itsPropertyName, ""+getDividerLocation());
	}
	

}
