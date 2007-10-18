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
package tod.gui.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.ObjectId;
import tod.gui.IGUIManager;
import tod.gui.MinerUI;
import tod.gui.seed.StringSearchSeed;
import tod.gui.view.highlighter.EventHighlighter;
import tod.impl.dbgrid.BidiIterator;
import zz.utils.SimpleListModel;
import zz.utils.ui.StackLayout;

public class StringSearchView extends LogView
{
	private static final String PROPERTY_SPLITTER_POS = "StringSearchView.splitterPos";
	
	private StringSearchSeed itsSeed;
	private JSplitPane itsSplitPane;
	private SimpleListModel itsResultsListModel;

	private JList itsList;

	private EventHighlighter itsEventHighlighter;

	public StringSearchView(
			IGUIManager aGUIManager, 
			ILogBrowser aLog, 
			StringSearchSeed aSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aSeed;
		createUI();
	}

	public StringSearchSeed getSeed()
	{
		return itsSeed;
	}
	
	private void createUI()
	{
		itsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		itsSplitPane.setLeftComponent(createSearchPane());
		itsSplitPane.setRightComponent(createStripesPane());
		
		setLayout(new StackLayout());
		add(itsSplitPane);
	}
	
	private JComponent createSearchPane()
	{
		JPanel thePanel = new JPanel(new BorderLayout());
		
		final JTextField theTextField = new JTextField();
		theTextField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				search(theTextField.getText());
			}
		});
		
		thePanel.add(theTextField, BorderLayout.NORTH);
		
		itsResultsListModel = new SimpleListModel();
		itsList = new JList(itsResultsListModel);
		itsList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent aE)
			{
				highlight();
			}
		});
		
		thePanel.add(new JScrollPane(itsList), BorderLayout.CENTER);
		
		return thePanel;
	}
	
	private JComponent createStripesPane()
	{
		itsEventHighlighter = new EventHighlighter(getGUIManager(), getLogBrowser());
		return itsEventHighlighter;
	}
	
	private void highlight()
	{
		Object[] theValues = itsList.getSelectedValues();

		List<IEventFilter> theFilters = new ArrayList<IEventFilter>();
		for(Object theValue : theValues)
		{
			SearchResult theResult = (SearchResult) theValue;
			theFilters.add(getLogBrowser().createObjectFilter(theResult.getObjectId()));
		}
		
		IEventFilter[] theFilterArray = theFilters.toArray(new IEventFilter[theFilters.size()]);
		ICompoundFilter theFilter = getLogBrowser().createUnionFilter(theFilterArray);
		
		itsEventHighlighter.setFilter(theFilter);
	}
	
	private void search(String aText)
	{
		BidiIterator<Long> theIterator = getLogBrowser().searchStrings(aText);
		List<SearchResult> theList = new ArrayList<SearchResult>();
		for(int i=0;i<100;i++)
		{
			if (! theIterator.hasNext()) break;
			theList.add(new SearchResult(new ObjectId(theIterator.next())));
		}
		
		itsResultsListModel.setList(theList);
	}
	
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		
		int theSplitterPos = MinerUI.getIntProperty(
				getGUIManager(), 
				PROPERTY_SPLITTER_POS, 400);
		
		itsSplitPane.setDividerLocation(theSplitterPos);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		
		getGUIManager().setProperty(
				PROPERTY_SPLITTER_POS, 
				""+itsSplitPane.getDividerLocation());
	}
	
	/**
	 * Represents a search result
	 * @author gpothier
	 */
	private class SearchResult
	{
		private ObjectId itsObjectId;
		private String itsValue;
	
		public SearchResult(ObjectId aObjectId)
		{
			itsObjectId = aObjectId;
		}

		public ObjectId getObjectId()
		{
			return itsObjectId;
		}

		public String getValue()
		{
			if (itsValue == null)
			{
				itsValue = (String) getLogBrowser().getRegistered(itsObjectId);
			}
			return itsValue;
		}
		
		@Override
		public String toString()
		{
			return "["+getObjectId()+"] "+getValue();
		}
		
	}
}
