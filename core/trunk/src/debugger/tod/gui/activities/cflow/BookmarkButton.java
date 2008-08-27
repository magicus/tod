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
package tod.gui.activities.cflow;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tod.core.IBookmarks;
import tod.core.IBookmarks.EventBookmark;
import tod.core.database.event.ILogEvent;
import tod.gui.GUIUtils;
import tod.gui.Resources;
import zz.utils.ui.AbstractOptionPanel;
import zz.utils.ui.SimpleColorChooserPanel;
import zz.utils.ui.UIUtils;
import zz.utils.ui.popup.ButtonPopupComponent;

public class BookmarkButton extends ButtonPopupComponent
{
	private ILogEvent itsCurrentEvent;
	private IBookmarks itsBookmarks;

	public BookmarkButton(IBookmarks aBookmarks)
	{
		super(new JButton(Resources.ICON_BOOKMARK.asIcon(20)));
		itsBookmarks = aBookmarks;
		getButton().setToolTipText("Bookmark current event");
		getButton().setMargin(UIUtils.NULL_INSETS);
		setPopupComponent(new BookmarkPopup());
	}

	public void setCurrentEvent(ILogEvent aCurrentEvent)
	{
		itsCurrentEvent = aCurrentEvent;
		getButton().setEnabled(itsCurrentEvent != null);
	}
	
	private class BookmarkPopup extends AbstractOptionPanel
	{
		private JTextField itsNameTextField;
		private SimpleColorChooserPanel itsColorChooserPanel;

		public BookmarkPopup()
		{
		}

		@Override
		protected JComponent createComponent()
		{
			JPanel thePanel = new JPanel(GUIUtils.createStackLayout());
			
			thePanel.add(new JLabel("Choose a name for this event (optional):"));
			itsNameTextField = new JTextField(20);
			thePanel.add(itsNameTextField);
			
			thePanel.add(new JLabel(" "));
			thePanel.add(new JLabel("Choose a color for this event (optional):"));
			JPanel theP1 = new JPanel(new FlowLayout());
			itsColorChooserPanel = new SimpleColorChooserPanel("None");
			theP1.add(itsColorChooserPanel);
			thePanel.add(theP1);
			
			return thePanel;
		}
		
		@Override
		protected void ok()
		{
			super.ok();
			Color theColor = itsColorChooserPanel.pColor().get();
			String theName = itsNameTextField.getText();
			if (theName.length() == 0) theName = null;
			
			itsBookmarks.addBookmark(new EventBookmark(theColor, theName, null, itsCurrentEvent, false));
			hidePopup();
		}
		
		@Override
		protected void cancel()
		{
			super.cancel();
			hidePopup();
		}
	}
}
