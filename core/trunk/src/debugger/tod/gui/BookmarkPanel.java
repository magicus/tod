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
package tod.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import tod.core.database.event.ILogEvent;
import tod.gui.kit.messages.EventSelectedMsg;
import tod.gui.view.IEventListView;
import tod.gui.view.LogView;
import zz.utils.SimpleComboBoxModel;

/**
 * This panel permits to create and select bookmarks.
 * @author gpothier
 */
public class BookmarkPanel extends JPanel
{
	private IEventListView itsCurrentView;
	private JComboBox itsComboBox;
	private SimpleComboBoxModel itsModel = new SimpleComboBoxModel(new ArrayList());
	
	private Map<String, ILogEvent> itsBookmarks = new HashMap<String, ILogEvent>();

	public BookmarkPanel()
	{
		createUI();
	}

	private void createUI()
	{
		itsComboBox = new JComboBox(itsModel);
		itsComboBox.setEditable(true);
		itsComboBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				selectBookmark((String) itsComboBox.getSelectedItem());
			}
		});
		
		itsComboBox.setToolTipText(
				"<html>" +
				"<b>Bookmarks.</b> Type a name and press enter <br>" +
				"to create a new bookmark, or select an existing <br>" +
				"bookmark in the list.");
		
		itsComboBox.setEnabled(false);
		
		add(itsComboBox);
	}
	
	public void setView(LogView aView)
	{
		if (aView instanceof IEventListView)
		{
			IEventListView theView = (IEventListView) aView;
			itsCurrentView = theView;
		}
		else itsCurrentView = null;
		
		itsComboBox.setSelectedIndex(-1);
		itsComboBox.setEnabled(itsCurrentView != null);
	}
	
	private void selectBookmark(String aName)
	{
		if (itsCurrentView == null) return;
		if (aName == null || aName.length() == 0) return;
		
		ILogEvent theEvent = itsBookmarks.get(aName);
		if (theEvent == null)
		{
			theEvent = itsCurrentView.getSelectedEvent();
			itsBookmarks.put(aName, theEvent);
			itsModel.getList().add(aName);
			Collections.sort(itsModel.getList());
			itsModel.fireContentsChanged();
			itsComboBox.getEditor().setItem(null);
		}
		else
		{
			itsCurrentView.selectEvent(theEvent, new EventSelectedMsg.SM_SelectInBookmarks(aName));
		}
		
		itsComboBox.setSelectedItem(null);
	}
	
}
