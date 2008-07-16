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
package tod.gui.view.dyncross;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.LocationUtils;
import tod.core.database.structure.IAdviceInfo;
import tod.core.database.structure.IAspectInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.tree.StructureTreeBuilders;
import tod.gui.BrowserData;
import tod.gui.IGUIManager;
import tod.gui.components.LocationTreeTable;
import tod.gui.eventlist.IntimacyLevel;
import tod.gui.kit.SavedSplitPane;
import tod.gui.seed.DynamicCrosscuttingSeed;
import tod.gui.seed.DynamicCrosscuttingSeed.Highlight;
import tod.gui.view.LogView;
import tod.gui.view.highlighter.EventHighlighter;
import zz.utils.list.IList;
import zz.utils.list.IListListener;
import zz.utils.properties.IProperty;
import zz.utils.properties.PropertyListener;
import zz.utils.tree.ITree;
import zz.utils.tree.SimpleTreeNode;
import zz.utils.ui.StackLayout;

/**
 * This view displays the dynamic crosscutting of aspects
 * (aka. aspect murals).
 * @author gpothier
 */
public class DynamicCrosscuttingView extends LogView<DynamicCrosscuttingSeed>
implements IListListener<Highlight>
{
	private static final int COLUMN_HIGHLIGHT = 0;
	
	private MyHighlighter itsHighlighter;
	
	/**
	 * We maintain this temporary binding between the UI and the seed's highlights list.
	 */
	private Map<ILocationInfo, Highlight> itsHighlightsMap = new HashMap<ILocationInfo, Highlight>();
	
	public DynamicCrosscuttingView(IGUIManager aGUIManager)
	{
		super(aGUIManager);
	}

	@Override
	protected void connectSeed(DynamicCrosscuttingSeed aSeed)
	{
		connect(aSeed.pStart, itsHighlighter.pStart());
		connect(aSeed.pEnd, itsHighlighter.pEnd());
		
		itsHighlighter.reloadContext();
		
		itsHighlightsMap.clear();
		for (Highlight theHighlight : aSeed.pHighlights)
		{
			itsHighlightsMap.put(theHighlight.getLocation(), theHighlight);
		}
		
		aSeed.pHighlights.addHardListener(this);
		
		setupHighlights();
	}

	@Override
	protected void disconnectSeed(DynamicCrosscuttingSeed aSeed)
	{
		disconnect(aSeed.pStart, itsHighlighter.pStart());
		disconnect(aSeed.pEnd, itsHighlighter.pEnd());
		aSeed.pHighlights.removeListener(this);
	}

	@Override
	public void init()
	{
		super.init();
		
		JSplitPane theSplitPane = new SavedSplitPane(getGUIManager(), "dynamicCrosscuttingView.splitterPos");

		// Left part
		MyTreeTable theTreeTable = new MyTreeTable(StructureTreeBuilders.createAspectTree(
				getLogBrowser().getStructureDatabase(), 
				true));          
		
		Dimension theSize = new HighlightEditor(this).getPreferredSize();
		theTreeTable.setRowHeight(theSize.height + 1);
		theTreeTable.setColumnWidth(COLUMN_HIGHLIGHT, theSize.width);
		
		HighlightCellEditor theEditor = new HighlightCellEditor();
		theTreeTable.setDefaultRenderer(Highlight.class, theEditor);
		theTreeTable.setDefaultEditor(Highlight.class, theEditor);
		
		theTreeTable.pSelectedLocation.addHardListener(new PropertyListener<ILocationInfo>()
				{
					@Override
					public void propertyChanged(
							IProperty<ILocationInfo> aProperty, 
							ILocationInfo aOldValue,
							final ILocationInfo aNewValue)
					{
						if (aNewValue != null) 
						{
							// Delay a bit showing the source, as it causes issues.
							new Thread("DynCC goto source scheduler")
							{
								@Override
								public void run()
								{
									try
									{
										sleep(300);
										LocationUtils.gotoSource(getGUIManager(), aNewValue);
									}
									catch (InterruptedException e)
									{
										throw new RuntimeException(e);
									}
								}
							}.start();
						}
					}
				});
		
		JScrollPane theScrollPane = new JScrollPane(theTreeTable);
		
		// Right part
		JPanel theRightPanel = new JPanel(new BorderLayout());
//		theRightPanel.add(new LegendPanel(), BorderLayout.SOUTH);
		
		itsHighlighter = new MyHighlighter(getGUIManager(), getLogBrowser());
		theRightPanel.add(itsHighlighter, BorderLayout.CENTER);
		
		theSplitPane.setLeftComponent(theScrollPane);
		theSplitPane.setRightComponent(theRightPanel);
		
		setLayout(new StackLayout());
		add(theSplitPane);
	}
	
	private IEventBrowser createBrowser(Highlight aHighlight)
	{
		ICompoundFilter theUnionFilter = getLogBrowser().createUnionFilter();
		for (int theSourceId : LocationUtils.getAdviceSourceIds(aHighlight.getLocation()))
		{
			if (hasAllRoles(aHighlight))
			{
				theUnionFilter.add(getLogBrowser().createAdviceCFlowFilter(theSourceId));
				theUnionFilter.add(getLogBrowser().createAdviceSourceIdFilter(theSourceId));
			}
			else
			{
				ICompoundFilter theRolesFilter = getLogBrowser().createUnionFilter();
				for (BytecodeRole theRole : aHighlight.getRoles()) 
				{
					if (theRole == BytecodeRole.ADVICE_EXECUTE)
					{
						theRolesFilter.add(getLogBrowser().createAdviceCFlowFilter(theSourceId));
					}

					theRolesFilter.add(getLogBrowser().createIntersectionFilter(
							getLogBrowser().createRoleFilter(theRole),
							getLogBrowser().createAdviceSourceIdFilter(theSourceId)));
				}
				
				theUnionFilter.add(theRolesFilter);
			}
		}
		return getLogBrowser().createBrowser(theUnionFilter);
	}
	
	/**
	 * Whether this highlight has all the available roles.
	 */
	private boolean hasAllRoles(Highlight aHighlight)
	{
		for (BytecodeRole theRole : IntimacyLevel.ROLES)
		{
			if(! aHighlight.getRoles().contains(theRole)) return false;
		}
		return true;
	}
	
	void setHighlight(ILocationInfo aLocation, Highlight aHighlight)
	{
		Highlight thePrevious = itsHighlightsMap.get(aLocation);
		if (thePrevious != null) getSeed().pHighlights.remove(thePrevious);
		itsHighlightsMap.put(aLocation, aHighlight);
		if (aHighlight != null) getSeed().pHighlights.add(aHighlight);
	}
	
	/**
	 * Initial setup of highlights
	 */
	private void setupHighlights()
	{
		for(Highlight theHighlight : getSeed().pHighlights)
		{
			itsHighlighter.pHighlightBrowsers.add(new BrowserData(
					createBrowser(theHighlight),
					theHighlight.getColor(),
					BrowserData.DEFAULT_MARK_SIZE+1));
		}
	}
	
	public void elementAdded(IList<Highlight> aList, int aIndex, Highlight aElement)
	{
		itsHighlighter.pHighlightBrowsers.add(
				aIndex, 
				new BrowserData(createBrowser(aElement), aElement.getColor(), BrowserData.DEFAULT_MARK_SIZE+1));
	}

	public void elementRemoved(IList<Highlight> aList, int aIndex, Highlight aElement)
	{
		itsHighlighter.pHighlightBrowsers.remove(aIndex);
	}
	
	/**
	 * Our subclass of {@link LocationTreeTable} that has two additional columns:
	 * - enable and choose intimacy
	 * - choose color
	 * @author gpothier
	 */
	private class MyTreeTable extends LocationTreeTable
	{
		public MyTreeTable(ITree<SimpleTreeNode<ILocationInfo>, ILocationInfo> aTree)
		{
			super(aTree);
		}

		@Override
		protected int getColumnCount()
		{
			return 1;
		}

		@Override
		protected Class getColumnClass(int aColumn)
		{
			switch(aColumn) 
			{
			case COLUMN_HIGHLIGHT: return Highlight.class;
			default: throw new RuntimeException("Not handled: "+aColumn);
			}
		}

		@Override
		protected Object getValueAt(ILocationInfo aLocation, int aColumn)
		{
			switch(aColumn) 
			{
			case COLUMN_HIGHLIGHT: 
				Highlight theHighlight = itsHighlightsMap.get(aLocation);
				System.out.println("location: "+aLocation+" -> "+theHighlight);
				return theHighlight;
			default: throw new RuntimeException("Not handled: "+aColumn);
			}
		}

		@Override
		protected boolean isCellEditable(ILocationInfo aLocation, int aColumn)
		{
			return (aLocation instanceof IAspectInfo) || (aLocation instanceof IAdviceInfo);
		}
	}

//	private class LegendPanel extends JPanel
//	implements IListListener<Highlight>
//	{
//		public LegendPanel()
//		{
//			super(new FlowLayout());
//			for(Highlight theHighlight : itsSeed.pHighlights)
//			{
//				add(new LegendItem(theHighlight, theHighlight.getColor()));
//			}
//		}
//		
//		@Override
//		public void addNotify()
//		{
//			super.addNotify();
//			itsSeed.pHighlights.addHardListener(this);
//		}
//		
//		@Override
//		public void removeNotify()
//		{
//			super.removeNotify();
//			itsSeed.pHighlights.removeListener(this);
//		}
//
//		public void elementAdded(IList<Highlight> aList, int aIndex, Highlight aElement)
//		{
//			add(new LegendItem(aElement, aElement.getColor()), null, aIndex);
//			revalidate();
//			repaint();
//		}
//
//		public void elementRemoved(IList<Highlight> aList, int aIndex, Highlight aElement)
//		{
//			remove(aIndex);
//			revalidate();
//			repaint();
//		}
//		
//	}
//	
//	private static class LegendItem extends JPanel
//	{
//		private final Highlight itsHighlight;
//		
//		public LegendItem(Highlight aHighlight, Color aMarkColor)
//		{
//			super(new FlowLayout(FlowLayout.LEFT));
//			
//			itsHighlight = aHighlight;
//			
//			JPanel theColorPanel = new JPanel(null);
//			theColorPanel.setBackground(aMarkColor);
//			theColorPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//			theColorPanel.setPreferredSize(new Dimension(30, 20));
//			add(theColorPanel);
//			
//			add(new JLabel(itsHighlight.toString()));
//		}
//	}
//	
	private class MyHighlighter extends EventHighlighter
	{
		public MyHighlighter(IGUIManager aGUIManager, ILogBrowser aLogBrowser)
		{
			super(aGUIManager, aLogBrowser);
		}

		@Override
		protected void perThread()
		{
			if (getSeed() != null) setMuralPainter(new AdviceCFlowMuralPainter(getSeed().pHighlights));
			super.perThread();
		}
	}
	
	/**
	 * The table cell editor/renderer for the intimacy level column.
	 * @author gpothier
	 */
	private class HighlightCellEditor extends AbstractCellEditor
	implements TableCellRenderer, TableCellEditor
	{
		private HighlightEditor itsRenderer = new HighlightEditor(DynamicCrosscuttingView.this);
		private HighlightEditor itsEditor = new HighlightEditor(DynamicCrosscuttingView.this);
		private JPanel itsNoValueEditor = new JPanel();
		
		private void setup(
				JComponent aEditor,
				JTable aTable,
				boolean aIsSelected,
				boolean aHasFocus)
		{
			aEditor.setBackground(aIsSelected ? aTable.getSelectionBackground() : aTable.getBackground());
		}
		
		public Component getTableCellRendererComponent(
				JTable aTable,
				Object aValue,
				boolean aIsSelected,
				boolean aHasFocus, 
				int aRow,
				int aColumn)
		{
//			if (aValue == NO_VALUE) return itsNoValueEditor;
			
			setup(itsRenderer, aTable, aIsSelected, aHasFocus);
			itsRenderer.setValue((Highlight) aValue);
			return itsRenderer;
		}

		public Component getTableCellEditorComponent(
				JTable aTable,
				Object aValue,
				boolean aIsSelected,
				int aRow,
				int aColumn)
		{
//			if (aValue == NO_VALUE) return itsNoValueEditor;

			setup(itsEditor, aTable, aIsSelected, true);
			ILocationInfo theLocation = (ILocationInfo) aTable.getModel().getValueAt(aRow, 0);
			itsEditor.setLocationInfo(theLocation);
			itsEditor.setValue((Highlight) aValue);
			return itsEditor;
		}

		public Object getCellEditorValue()
		{
			return itsEditor.getValue();
		}
	}

}
