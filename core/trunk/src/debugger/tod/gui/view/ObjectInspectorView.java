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
package tod.gui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import tod.Util;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.BrowserData;
import tod.gui.IGUIManager;
import tod.gui.TimeScale;
import tod.gui.eventsequences.FieldSequenceView;
import tod.gui.eventsequences.MembersDock;
import tod.gui.eventsequences.MethodSequenceView;
import tod.gui.kit.SeedLinkLabel;
import tod.gui.seed.FilterSeed;
import tod.gui.seed.ObjectInspectorSeed;
import zz.utils.list.ICollection;
import zz.utils.list.ICollectionListener;
import zz.utils.properties.ISetProperty;
import zz.utils.properties.PropertyUtils;
import zz.utils.ui.GridStackLayout;

/**
 * This panel presents the history of an object's field writes and/or method calls.
 * @author gpothier
 */
public class ObjectInspectorView extends LogView
{
	private ObjectInspectorSeed itsSeed;
	private IObjectInspector itsInspector;
	private TimeScale itsTimeScale;
	
	private MembersDock itsDock;
	
	private JPanel itsTitlePanel;
	
	private MemberSelector itsMemberSelector;
	
	
	public ObjectInspectorView(IGUIManager aGUIManager, ILogBrowser aLog, ObjectInspectorSeed aInspectorSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aInspectorSeed;
	}
	
	@Override
	public void init()
	{
		createUI();
		setObject(itsSeed.getInspectedObject());
	}
	
	private void createUI()
	{
		setLayout(new BorderLayout());
		
		JPanel theNorthPanel = new JPanel(new GridStackLayout(1));
	
		// Setup title
		itsTitlePanel = new JPanel();
		theNorthPanel.add (itsTitlePanel);
		updateTitlePanel();
		
		// Setup time scale
		itsTimeScale = new TimeScale();
		itsTimeScale.pStart().set(getLogBrowser().getFirstTimestamp());
		itsTimeScale.pEnd().set(getLogBrowser().getLastTimestamp());
		connect (itsSeed.pSelectionStart(), itsTimeScale.pSelectionStart(), true);
		connect (itsSeed.pSelectionEnd(), itsTimeScale.pSelectionEnd(), true);
		connect (itsSeed.pCurrentPosition(), itsTimeScale.pCurrentPosition(), true);
		theNorthPanel.add (itsTimeScale);
		
		add (theNorthPanel, BorderLayout.NORTH);
		
		// Setup center view
		itsDock = new MembersDock(this);
		add (itsDock, BorderLayout.CENTER);
		
		// These are hard connectors, as we are the owner of the timescale & dock
		PropertyUtils.connect(itsTimeScale.pSelectionStart(), itsDock.pStart(), true);
		PropertyUtils.connect(itsTimeScale.pSelectionEnd(), itsDock.pEnd(), true);
		
		// Setup member selector
		itsMemberSelector = new MemberSelector();
		itsMemberSelector.pSelectedMembers().addHardListener(new ICollectionListener<IMemberInfo>()
				{
					public void elementAdded(ICollection<IMemberInfo> aCollection, IMemberInfo aElement)
					{
						itsDock.addMember(itsInspector, aElement);
					}

					public void elementRemoved(ICollection<IMemberInfo> aCollection, IMemberInfo aElement)
					{
						itsDock.removeMember(aElement);
					}
				});
		connectSet(itsSeed.pSelectedMembers(), itsMemberSelector.pSelectedMembers(), true);
		add (itsMemberSelector, BorderLayout.WEST);
		
	}

	private void setObject (ObjectId aObjectId)
	{
		itsInspector = aObjectId != null ? getLogBrowser().createObjectInspector(aObjectId) : null;
		itsTimeScale.pEventBrowsers().clear();
		
		if (itsInspector != null)
		{
			// Update label
			ITypeInfo theType = itsInspector.getType();

			// Update timescale's browsers
			for (IMemberInfo theMember : itsInspector.getMembers())
			{
				IEventBrowser theBrowser = itsInspector.getBrowser(theMember);
				Color theColor;
				if (theMember instanceof IFieldInfo) theColor = FieldSequenceView.FIELD_COLOR;
				else if (theMember instanceof IBehaviorInfo) theColor = MethodSequenceView.METHOD_COLOR;
				else throw new RuntimeException("Not handled: "+theMember); 
				itsTimeScale.pEventBrowsers().add (new BrowserData(theBrowser, theColor));
			}
		}
		
		itsMemberSelector.pInspector().set(itsInspector);
//		itsMemberSelector.selectFields();

		updateTitlePanel();
	}
	
	private void updateTitlePanel()
	{
		itsTitlePanel.removeAll();
		itsTitlePanel.setLayout(new FlowLayout());
		
		String theTitle;
		ObjectId theObject;
		
		if (itsInspector != null)
		{
			// Update label
			ITypeInfo theType = itsInspector.getType();
			theObject = itsInspector.getObject();
			theTitle = String.format(
					"Object inspector for: %s (%s)",
					theObject,
					Util.getPrettyName(theType.getName()));
		}
		else 
		{
			theTitle = "Object inspector";
			theObject = null;
		}
		
		JLabel theTitleLabel = new JLabel(theTitle);
		itsTitlePanel.add(theTitleLabel);
		
		if (theObject != null)
		{
			FilterSeed theShowEventsSeed = new FilterSeed(
					getGUIManager(), 
					getLogBrowser(), 
					getLogBrowser().createTargetFilter(theObject));
			
			itsTitlePanel.add (new SeedLinkLabel(
					getGUIManager(), 
					"Show all events", 
					theShowEventsSeed));
			
			FilterSeed theShowHistorySeed = new FilterSeed(
					getGUIManager(),
					getLogBrowser(),
					getLogBrowser().createObjectFilter(theObject));
			
			itsTitlePanel.add (new SeedLinkLabel(
					getGUIManager(), 
					"Show history", 
					theShowHistorySeed));
						
		}
			
		itsTitlePanel.revalidate();
		itsTitlePanel.repaint();
	}
	
	/**
	 * The property that contains the members whose history is displayed.
	 */
	public ISetProperty<IMemberInfo> pMembers()
	{
		return itsMemberSelector.pSelectedMembers();
	}
	
}
