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
package tod.gui.eventsequences;

import java.awt.Image;
import java.util.Collection;

import javax.swing.JComponent;

import zz.utils.ItemAction;
import zz.utils.properties.IRWProperty;

/**
 * A view of an horizontal event sequence.
 * Each indivudual components of the view must be requested
 * through corresponding methods. For instance, to obtain the main
 * graphic object (the one that displays the events), use
 * {@link #getEventStripe()};
 * for obtaining the available actions, use {@link #getActions()}.
 * @author gpothier
 */
public interface IEventSequenceView 
{
	/**
	 * Starting timestamp of the displayed time range.
	 */
	public IRWProperty<Long> pStart ();
	
	/**
	 * Ending timestamp of the displayed time range.
	 */
	public IRWProperty<Long> pEnd ();
	
	/**
	 * Returns the horizontal stripe that displays events.
	 */
	public JComponent getEventStripe();
	
	/**
	 * Returns a collection of available actions for this sequence view.
	 */
	public Collection<ItemAction> getActions();
	
	/**
	 * Returns an icon representing this sequence view.
	 */
	public Image getIcon();
	
	/**
	 * Returns the title of this sequence view.
	 */
	public String getTitle();
}
