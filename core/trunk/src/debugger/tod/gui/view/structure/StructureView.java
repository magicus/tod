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
import tod.gui.locationselector.LocationSelectorPanel;
import tod.gui.seed.StructureSeed;
import tod.gui.view.LogView;
import zz.utils.properties.PropertyUtils;
import zz.utils.properties.PropertyUtils.Connector;
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
	private LocationSelectorPanel itsSelectorPanel;
	private Connector<ILocationInfo> itsConnector;
	
	public StructureView(IGUIManager aGUIManager, ILogBrowser aLog, StructureSeed aSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aSeed;
	}
	
	public StructureSeed getSeed()
	{
		return itsSeed;
	}
	
	@Override
	public void init()
	{
		itsSelectorPanel = new LocationSelectorPanel(getLogBrowser().getStructureDatabase(), true);
		itsInfoHolder = new JPanel(new StackLayout());
		
		JSplitPane theSplitPane = new SavedSplitPane(JSplitPane.HORIZONTAL_SPLIT, getGUIManager(), PROPERTY_SPLITTER_POS);
		theSplitPane.setResizeWeight(0.5);
		theSplitPane.setLeftComponent(itsSelectorPanel);
		theSplitPane.setRightComponent(itsInfoHolder);
		
		setLayout(new StackLayout());
		add(theSplitPane);
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		itsConnector = PropertyUtils.connect(getSeed().pSelectedLocation(), itsSelectorPanel.pSelectedLocation(), true);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsConnector.disconnect();
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
