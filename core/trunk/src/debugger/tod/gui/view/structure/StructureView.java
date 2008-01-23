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
package tod.gui.view.structure;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ILocationInfo;
import tod.gui.IGUIManager;
import tod.gui.kit.SavedSplitPane;
import tod.gui.seed.StructureSeed;
import tod.gui.view.LogView;
import zz.utils.ui.StackLayout;

/**
 * Provides access to the structural database.
 * @author gpothier
 */
public class StructureView extends LogView
{
	private static final String PROPERTY_SPLITTER_POS = "structureView.splitterPos";
	private final StructureSeed itsSeed;
	private JPanel itsInfoHolder;
	
	public StructureView(IGUIManager aGUIManager, ILogBrowser aLog, StructureSeed aSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aSeed;
	}
	
	@Override
	public void init()
	{
		LocationSelectorPanel theSelectorPanel = new LocationSelectorPanel(
				getLogBrowser().getStructureDatabase(), 
				this);
		
		
		itsInfoHolder = new JPanel(new StackLayout());
		
		JSplitPane theSplitPane = new SavedSplitPane(JSplitPane.HORIZONTAL_SPLIT, getGUIManager(), PROPERTY_SPLITTER_POS);
		theSplitPane.setResizeWeight(0.5);
		theSplitPane.setLeftComponent(theSelectorPanel);
		theSplitPane.setRightComponent(itsInfoHolder);
		
		setLayout(new StackLayout());
		add(theSplitPane);
	}
	
	/**
	 * Shows the information corresponding to the given node.
	 */
	public void showNode(ILocationInfo aLocation)
	{
		if (aLocation instanceof IBehaviorInfo)
		{
			IBehaviorInfo theBehavior = (IBehaviorInfo) aLocation;
			showPanel(new BehaviorPanel(theBehavior));
		}
	}
	
	/**
	 * Changes the currently displayed info panel
	 */
	private void showPanel(JComponent aComponent)
	{
		itsInfoHolder.removeAll();
		itsInfoHolder.add(aComponent);
		itsInfoHolder.revalidate();
		itsInfoHolder.repaint();
	}
}
