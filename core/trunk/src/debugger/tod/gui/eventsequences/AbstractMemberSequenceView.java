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

import java.awt.Color;

import javax.swing.JComponent;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.structure.IMemberInfo;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;

/**
 * Abstract base class for event sequence views that displays events relative to a class member.
 * @author gpothier
 */
public abstract class AbstractMemberSequenceView extends AbstractSingleBrowserSequenceView
{
	private final IObjectInspector itsInspector;
	
	public AbstractMemberSequenceView(IGUIManager aGUIManager, Color aColor, IObjectInspector aInspector)
	{
		super(aGUIManager, aColor);
		itsInspector = aInspector;
	}

	@Override
	protected IEventBrowser getBrowser()
	{
		return itsInspector.getBrowser(getMember());
	}

	/**
	 * Returns the member whose events are displayed in this sequence.
	 */
	public abstract IMemberInfo getMember();
	
	/**
	 * Helper method that creates a graphic object suitable for 
	 * representing the given object.
	 */
	protected JComponent createBaloon(Object aObject)
	{
		return Hyperlinks.object(
				Hyperlinks.SWING, 
				getLogBrowser(), 
				getGUIManager().getJobProcessor(),
				itsInspector.getObject(), 
				aObject, 
				null,
				true);
	}
}
