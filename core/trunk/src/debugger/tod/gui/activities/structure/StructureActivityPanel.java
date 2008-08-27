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
package tod.gui.activities.structure;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ILocationInfo;
import tod.gui.IContext;
import tod.gui.IGUIManager;
import tod.gui.activities.ActivityPanel;
import tod.gui.components.locationselector.LocationSelectorPanel;
import tod.gui.kit.SavedSplitPane;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.PropertyListener;
import zz.utils.ui.StackLayout;

/**
 * Provides access to the structural database.
 * @author gpothier
 */
public class StructureActivityPanel extends ActivityPanel<StructureSeed>
{
	private static final String PROPERTY_SPLITTER_POS = "structureView.splitterPos";
	private JPanel itsInfoHolder;
	private LocationSelectorPanel itsSelectorPanel;
	
	private IPropertyListener<ILocationInfo> itsSelectedLocationListener = new PropertyListener<ILocationInfo>()
	{
		public void propertyChanged(IProperty<ILocationInfo> aProperty, ILocationInfo aOldValue, ILocationInfo aNewValue)
		{
			showNode(aNewValue);
		}
	};
	
	public StructureActivityPanel(IContext aContext)
	{
		super(aContext);
	}

	@Override
	protected void connectSeed(StructureSeed aSeed)
	{
		aSeed.pSelectedLocation().addHardListener(itsSelectedLocationListener);
		connect(aSeed.pSelectedLocation(), itsSelectorPanel.pSelectedLocation());
	}

	@Override
	protected void disconnectSeed(StructureSeed aSeed)
	{
		aSeed.pSelectedLocation().removeListener(itsSelectedLocationListener);
		disconnect(aSeed.pSelectedLocation(), itsSelectorPanel.pSelectedLocation());
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
	
	/**
	 * Shows the information corresponding to the given node.
	 */
	public void showNode(ILocationInfo aLocation)
	{
		if (aLocation instanceof IBehaviorInfo)
		{
			IBehaviorInfo theBehavior = (IBehaviorInfo) aLocation;
			
			JTabbedPane theTabbedPane = new JTabbedPane();
			theTabbedPane.addTab("Bytecode", new DisassemblyPanel(theBehavior));
			theTabbedPane.addTab("Line numbers", new LineNumberInfoPanel(theBehavior));
			theTabbedPane.addTab("Local variables", new VariableInfoPanel(theBehavior));
			showPanel(theTabbedPane);
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
