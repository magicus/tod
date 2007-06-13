/*
TOD plugin - Eclipse pluging for TOD
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
package tod.plugin.fxviews;

import javax.swing.JComponent;

import tod.gui.IGUIManager;
import tod.plugin.views.AbstractNavigatorView;

/**
 * New view of the trace navigator using JavaFX.
 * @author gpothier
 */
public class TraceNavigatorFXView extends AbstractNavigatorView
{
	/**
	 * Id of the view as defined in plugin.xml
	 */
	public static final String VIEW_ID = "tod.plugin.views.TraceNavigatorFXView";
	
	private EventViewerFX itsEventViewer;
	
	@Override
	protected JComponent createComponent()
	{
		itsEventViewer = new EventViewerFX();
		return itsEventViewer;
	}

	@Override
	public IGUIManager getGUIManager()
	{
		return itsEventViewer;
	}
}