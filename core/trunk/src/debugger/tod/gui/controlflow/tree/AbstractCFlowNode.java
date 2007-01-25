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
package tod.gui.controlflow.tree;

import tod.core.database.browser.ILogBrowser;
import tod.gui.IGUIManager;
import tod.gui.Hyperlinks.ISeedFactory;
import tod.gui.controlflow.CFlowView;
import zz.csg.impl.SVGGraphicContainer;

/**
 * Base for all the graphic objects used in the representation 
 * of the control flow
 * @author gpothier
 */
public class AbstractCFlowNode extends SVGGraphicContainer
{
	private CFlowView itsView;

	public AbstractCFlowNode(CFlowView aView)
	{
		itsView = aView;
	}

	public ISeedFactory getSeedFactory()
	{
		return itsView.getLogViewSeedFactory();
	}
	
	public IGUIManager getGUIManager()
	{
		return itsView.getGUIManager();
	}

	public ILogBrowser getLogBrowser()
	{
		return itsView.getLogBrowser();
	}
	
	public CFlowView getView()
	{
		return itsView;
	}
	

}
