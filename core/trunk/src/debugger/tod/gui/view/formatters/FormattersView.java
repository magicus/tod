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
package tod.gui.view.formatters;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tod.core.database.browser.ILogBrowser;
import tod.gui.IGUIManager;
import tod.gui.MinerUI;
import tod.gui.formatter.CustomFormatterRegistry;
import tod.gui.formatter.CustomObjectFormatter;
import tod.gui.kit.SavedSplitPane;
import tod.gui.seed.FormattersSeed;
import tod.gui.view.LogView;
import zz.utils.ui.StackLayout;
import zz.utils.ui.UniversalRenderer;
import zz.utils.ui.crmlist.AbstractJavaCRMListModel;
import zz.utils.ui.crmlist.CRMList;
import zz.utils.ui.crmlist.CRMListModel;

/**
 * This view permits to edit the set of available formatters.
 * @author gpothier
 */
public class FormattersView extends LogView
{
	private static final String PROPERTY_SPLITTER_POS = "formattersView.splitterPos";

	private final FormattersSeed itsSeed;
	
	private JPanel itsEditorHolder;
	
	public FormattersView(IGUIManager aGUIManager, ILogBrowser aLog, FormattersSeed aSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aSeed;
	}
	
	public FormattersSeed getSeed()
	{
		return itsSeed;
	}

	@Override
	public void init()
	{
		super.init();

		JSplitPane theSplitPane = new SavedSplitPane(JSplitPane.HORIZONTAL_SPLIT, getGUIManager(), PROPERTY_SPLITTER_POS);
		theSplitPane.setResizeWeight(0.5);
		
		theSplitPane.setLeftComponent(createSelector());
		
		itsEditorHolder = new JPanel(new StackLayout());
		theSplitPane.setRightComponent(itsEditorHolder);
		
		setLayout(new StackLayout());
		add(theSplitPane);
	}
	
	private void show(CustomObjectFormatter aFormatter)
	{
		itsEditorHolder.removeAll();
		itsEditorHolder.add(new FormatterEditor(getLogBrowser().getStructureDatabase(), aFormatter));
		revalidate();
		repaint();
	}
	
	private JComponent createSelector()
	{
		final CustomFormatterRegistry theRegistry = getGUIManager().getCustomFormatterRegistry();
		CRMListModel theModel = new AbstractJavaCRMListModel<CustomObjectFormatter>(theRegistry.getFormatters())
		{

			@Override
			public boolean canMoveElement(int aSourceIndex, int aTargetIndex)
			{
				return false;
			}

			@Override
			protected CustomObjectFormatter newElement()
			{
				CustomObjectFormatter theFormatter = theRegistry.createFormatter();
				theFormatter.setName("<New formatter>");
				return theFormatter;
			}
		};
		
		final CRMList theList = new CRMList(theModel)
		{
			@Override
			protected String getUpLabel()
			{
				return null;
			}
			
			@Override
			protected String getDownLabel()
			{
				return null;
			}
		};
		
		theList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent aE)
			{
				if (! aE.getValueIsAdjusting())
				{
					FormattersView.this.show((CustomObjectFormatter) theList.getSelectedValue());
				}
			}
		});
		
		theList.setCellRenderer(new UniversalRenderer<CustomObjectFormatter>()
				{
					@Override
					protected String getName(CustomObjectFormatter aObject)
					{
						return aObject.getName();
					}
				});
		
		return theList;
	}
}
