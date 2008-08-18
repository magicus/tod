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
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.LocationUtils;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ObjectId;
import tod.gui.IGUIManager;
import tod.gui.eventlist.EventListPanel;
import tod.gui.kit.Bus;
import tod.gui.kit.SavedSplitPane;
import tod.gui.kit.messages.ShowCFlowMsg;
import tod.gui.seed.LogViewSeed;
import tod.gui.seed.StringSearchSeed;
import tod.impl.database.IBidiIterator;
import tod.utils.TODUtils;
import zz.utils.SimpleListModel;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.ui.StackLayout;

public class StringSearchView extends LogView<StringSearchSeed>
{
	private static final String PROPERTY_SPLITTER_POS = "StringSearchView.splitterPos";

	private SimpleListModel itsResultsListModel;

	private JList itsList;

	private EventListPanel itsEventListPanel;

	public StringSearchView(IGUIManager aGUIManager)
	{
		super(aGUIManager);
		createUI();
	}

	private void createUI()
	{
		JSplitPane theSplitPane =
				new SavedSplitPane(
						JSplitPane.HORIZONTAL_SPLIT,
						getGUIManager(),
						PROPERTY_SPLITTER_POS);
		theSplitPane.setLeftComponent(createSearchPane());
		theSplitPane.setRightComponent(createEventListPane());
		setLayout(new StackLayout());
		add(theSplitPane);
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
		itsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

	private JComponent createEventListPane()
	{
		itsEventListPanel =
				new EventListPanel(getGUIManager(), getBus(), getLogBrowser(), getJobScheduler());

		itsEventListPanel.eEventActivated().addListener(new IEventListener<ILogEvent>()
		{
			public void fired(IEvent< ? extends ILogEvent> aEvent, ILogEvent aData)
			{
				Bus.get(StringSearchView.this).postMessage(new ShowCFlowMsg(aData));
			}
		});

		itsEventListPanel.pSelectedEvent().addHardListener(new IPropertyListener<ILogEvent>()
		{
			public void propertyChanged(
					IProperty<ILogEvent> aProperty,
					ILogEvent aOldValue,
					ILogEvent aNewValue)
			{
				LocationUtils.gotoSource(getGUIManager(), aNewValue);
				TODUtils.logf(0, "sourceRevealer called from StringSearchView");
			}

			public void propertyValueChanged(IProperty<ILogEvent> aProperty)
			{
			}
		});

		return itsEventListPanel;
	}

	private void highlight()
	{
		itsList.repaint();
		Object[] theValues = itsList.getSelectedValues();

		List<IEventFilter> theFilters = new ArrayList<IEventFilter>();
		for (Object theValue : theValues)
		{
			SearchResult theResult = (SearchResult) theValue;
			theFilters.add(getLogBrowser().createObjectFilter(theResult.getObjectId()));
		}

		IEventFilter[] theFilterArray = theFilters.toArray(new IEventFilter[theFilters.size()]);
		ICompoundFilter theFilter = getLogBrowser().createUnionFilter(theFilterArray);

		itsEventListPanel.setBrowser(getLogBrowser().createBrowser(theFilter));

	}

	private void search(String aText)
	{
		IBidiIterator<Long> theIterator = getLogBrowser().searchStrings(aText);
		List<SearchResult> theList = new ArrayList<SearchResult>();
		for (int i = 0; i < 100; i++)
		{
			if (!theIterator.hasNext()) break;
			theList.add(new SearchResult(new ObjectId(theIterator.next())));
		}

		itsResultsListModel.setList(theList);
	}

	/**
	 * Represents a search result
	 * 
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
			return "[" + getObjectId() + "] " + getValue();
		}

	}
}
