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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.gui.GUIUtils;
import tod.gui.formatter.CustomObjectFormatter;
import tod.gui.locationselector.LocationSelectorPanel;
import zz.utils.Utils;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.PropertyUtils;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.PropertyEditor;
import zz.utils.ui.StackLayout;
import zz.utils.ui.crmlist.AbstractJavaCRMListModel;
import zz.utils.ui.crmlist.CRMList;
import zz.utils.ui.crmlist.CRMListModel;
import zz.utils.ui.popup.StickyPopup;

/**
 * Editor for a {@link CustomObjectFormatter}.
 * @author gpothier
 */
public class FormatterEditor extends JPanel
{
	private final IStructureDatabase itsStructureDatabase;
	private final CustomObjectFormatter itsFormatter;
	private StickyPopup itsLocationSelectorPopup;
	
	private AbstractJavaCRMListModel itsModel;
	
	private List<String> itsRecognizedTypes = new ArrayList<String>();
	private IRWProperty<String> pName = new SimpleRWProperty<String>()
	{
		@Override
		protected void changed(String aOldValue, String aNewValue)
		{
			itsFormatter.setName(aNewValue);
		}
	};
	
	
	public FormatterEditor(IStructureDatabase aStructureDatabase, CustomObjectFormatter aFormatter)
	{
		itsStructureDatabase = aStructureDatabase;
		itsFormatter = aFormatter;
		createUI();
	}

	private void createUI()
	{
		// Put initial state into our intermediate model
		Utils.fillCollection(itsRecognizedTypes, itsFormatter.getRecognizedTypes());
		Collections.sort(itsRecognizedTypes);
		pName.set(itsFormatter.getName());
		
		JTabbedPane theTabbedPane = new JTabbedPane();
		theTabbedPane.addTab("Short formatter", new CodeEditor(itsFormatter)
		{
			@Override
			protected String load()
			{
				return itsFormatter.getShortCode();
			}

			@Override
			protected void save(String aCode)
			{
				itsFormatter.setShortCode(aCode);
			}
		});
		
		theTabbedPane.addTab("Long formatter", new CodeEditor(itsFormatter)
		{
			@Override
			protected String load()
			{
				return itsFormatter.getLongCode();
			}

			@Override
			protected void save(String aCode)
			{
				itsFormatter.setLongCode(aCode);
			}
		});

		setLayout(new BorderLayout());
		add(theTabbedPane, BorderLayout.CENTER);
		add(createTypeSelector(), BorderLayout.EAST);
		
		JPanel theNamePanel = new JPanel(GUIUtils.createSequenceLayout());
		theNamePanel.add(new JLabel("Name: "));
		theNamePanel.add(PropertyEditor.createTextField(pName));
		add(theNamePanel, BorderLayout.NORTH);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsFormatter.clearRecognizedTypes();
		for (String theType : itsRecognizedTypes) itsFormatter.addRecognizedType(theType);
	}
	
	/**
	 * Creates the component that permits to select the recognized types
	 * of the formatter
	 * @return
	 */
	private JComponent createTypeSelector()
	{
		CRMList theList = new CRMList()
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
		
		LocationSelectorPanel theLocationSelectorPanel = new LocationSelectorPanel(itsStructureDatabase, false)
		{
			@Override
			public void show(ILocationInfo aLocation)
			{
				super.show(aLocation);
				itsLocationSelectorPopup.hide();
				if (aLocation instanceof IClassInfo)
				{
					IClassInfo theClass = (IClassInfo) aLocation;
					itsModel.addElement(theClass.getName());
				}
			}
		};
		
		theLocationSelectorPanel.setPreferredSize(new Dimension(250, 300));
		
		itsLocationSelectorPopup = new StickyPopup(theLocationSelectorPanel, theList.getCreateButton());
		itsModel = new AbstractJavaCRMListModel<String>(itsRecognizedTypes)
		{
			@Override
			protected String newElement()
			{
				itsLocationSelectorPopup.show();
				return null;
			}
		};
		theList.setModel(itsModel);
		
		JPanel thePanel = new JPanel(new BorderLayout());
		thePanel.add(theList, BorderLayout.CENTER);
		thePanel.add(new JLabel("Recognized types"), BorderLayout.NORTH);
		
		return thePanel;
	}
	
	private static abstract class CodeEditor extends JPanel
	{
		private CustomObjectFormatter itsFormatter;
		private JTextArea itsTextArea;
		
		public CodeEditor(CustomObjectFormatter aFormatter)
		{
			itsFormatter = aFormatter;
			createUI();
		}
		
		public CustomObjectFormatter getFormatter()
		{
			return itsFormatter;
		}
		
		private void createUI()
		{
			itsTextArea = new JTextArea();
			itsTextArea.setText(load());
			
			itsTextArea.addFocusListener(new FocusAdapter()
			{
				@Override
				public void focusLost(FocusEvent aE)
				{
					save(itsTextArea.getText());
				}
			});
			
			setLayout(new StackLayout());
			add(new JScrollPane(itsTextArea));
		}
		
		protected abstract String load();
		protected abstract void save(String aCode);
	}
}
