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
package tod.gui.view.dyncross;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.SourceRange;
import tod.core.database.structure.IStructureDatabase.AspectInfo;
import tod.gui.IGUIManager;
import tod.gui.kit.SavedSplitPane;
import tod.gui.view.LogView;
import zz.utils.SimpleListModel;
import zz.utils.Utils;
import zz.utils.ui.StackLayout;

/**
 * This view displays the dynamic crosscutting of aspects
 * (aka. aspect murals).
 * @author gpothier
 */
public class DynamicCrosscuttingView extends LogView
{
	public DynamicCrosscuttingView(IGUIManager aGUIManager, ILogBrowser aLog)
	{
		super(aGUIManager, aLog);
	}

	@Override
	public void init()
	{
		super.init();
		
		IStructureDatabase theStructureDatabase = getLogBrowser().getStructureDatabase();
		Map<String, AspectInfo> theMap = theStructureDatabase.getAspectInfoMap();
		
		// Create aspects list
		List<AspectInfo> theAspectsList = new ArrayList<AspectInfo>();
		Utils.fillCollection(theAspectsList, theMap.values());
		
		// Create advices list
		List<SourceRange> theAdvicesList = new ArrayList<SourceRange>();
		for (AspectInfo theAspectInfo : theAspectsList)
		{
			for (int theAdviceSourceId : theAspectInfo.getAdviceIds())
			{
				theAdvicesList.add(theStructureDatabase.getAdviceSource(theAdviceSourceId));
			}
		}
		
		MouseListener theAddListener = new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent aE)
			{
				if (aE.getClickCount() == 2)
				{
					JList theList = (JList) aE.getSource();
					addHighlight((Highlight) theList.getSelectedValue());
				}
			}
		};
		
		JList theAspectsJList = new JList(new SimpleListModel(theAspectsList));
		JList theAdvicesJList = new JList(new SimpleListModel(theAdvicesList));
		
		theAspectsJList.addMouseListener(theAddListener);
		theAdvicesJList.addMouseListener(theAddListener);
		
		JTabbedPane theTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		theTabbedPane.addTab("Aspects", new JScrollPane(theAspectsJList));
		theTabbedPane.addTab("Advices", new JScrollPane(theAdvicesJList));
		
		JSplitPane theSplitPane = new SavedSplitPane(getGUIManager(), "dynamicCrosscuttingView.splitterPos");
		
		theSplitPane.setLeftComponent(theTabbedPane);
		
		setLayout(new StackLayout());
		add(theSplitPane);
		
	}
	
	private void addHighlight(Highlight aHighlight)
	{
		
	}
	
	private static abstract class Highlight
	{
		
	}
	
	private static class AspectHighlight extends Highlight
	{
		
	}
	
	private static class AdviceHighlight extends Highlight
	{
		
	}
}
