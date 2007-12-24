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
