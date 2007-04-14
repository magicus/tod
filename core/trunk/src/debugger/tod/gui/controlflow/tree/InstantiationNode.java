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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.GUIUtils;
import tod.gui.Hyperlinks;
import tod.gui.JobProcessor;
import tod.gui.controlflow.CFlowView;
import zz.utils.ui.WrappedFlowLayout;
import zz.utils.ui.ZLabel;
import zz.utils.ui.text.XFont;

public class InstantiationNode extends BehaviorCallNode
{

	public InstantiationNode(
			CFlowView aView, 
			JobProcessor aJobProcessor,
			IBehaviorCallEvent aEvent)
	{
		super(aView, aJobProcessor, aEvent);
	}

	public IInstantiationEvent getEvent()
	{
		return (IInstantiationEvent) super.getEvent();
	}
	
	@Override
	protected JComponent createBehaviorNamePrefix()
	{
		return GUIUtils.createLabel("new ");
	}
	
	@Override
	protected JComponent createBehaviorName()
	{
		IClassInfo theType = getBehavior().getType();
		return GUIUtils.createLabel(theType.getName());
	}
	
	@Override
	protected JComponent createResultPrefix()
	{
		return GUIUtils.createLabel("Created ");
	}

	@Override
	protected Object getResult()
	{
		return getEvent().getInstance();
	}
}
