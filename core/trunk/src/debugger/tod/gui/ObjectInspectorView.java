/*
 * Created on Sep 30, 2005
 */
package tod.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.seed.ObjectInspectorSeed;
import reflex.lib.logging.miner.gui.view.LogView;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.MemberInfo;
import tod.core.model.structure.ObjectId;
import tod.core.model.structure.TypeInfo;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IObjectInspector;
import tod.gui.eventsequences.FieldSequenceView;
import tod.gui.eventsequences.MembersDock;
import tod.gui.eventsequences.MethodSequenceView;
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
	
	private JLabel itsTitleLabel;
	
	private MemberSelector itsMemberSelector;
	
	
	public ObjectInspectorView(IGUIManager aGUIManager, IEventTrace aLog, ObjectInspectorSeed aInspectorSeed)
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
		theNorthPanel.add (createTitlePanel());
		
		// Setup time scale
		itsTimeScale = new TimeScale();
		itsTimeScale.pStart().set(getLog().getFirstTimestamp());
		itsTimeScale.pEnd().set(getLog().getLastTimestamp());
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
		itsMemberSelector.pSelectedMembers().addHardListener(new ICollectionListener<MemberInfo>()
				{
					public void elementAdded(ICollection<MemberInfo> aCollection, MemberInfo aElement)
					{
						itsDock.addMember(itsInspector, aElement);
					}

					public void elementRemoved(ICollection<MemberInfo> aCollection, MemberInfo aElement)
					{
						itsDock.removeMember(aElement);
					}
				});
		add (itsMemberSelector, BorderLayout.WEST);
		
	}

	private void setObject (ObjectId aObjectId)
	{
		itsInspector = aObjectId != null ? getLog().createObjectInspector(aObjectId) : null;
		itsTimeScale.pEventBrowsers().clear();
		
		if (itsInspector != null)
		{
			// Update label
			TypeInfo theType = itsInspector.getType();
			itsTitleLabel.setText(String.format(
					"Object inspector for: %s (%s)",
					aObjectId,
					theType.getName()));

			// Update timescale's browsers
			for (MemberInfo theMember : itsInspector.getMembers())
			{
				IEventBrowser theBrowser = itsInspector.getBrowser(theMember);
				Color theColor;
				if (theMember instanceof FieldInfo) theColor = FieldSequenceView.FIELD_COLOR;
				else if (theMember instanceof BehaviorInfo) theColor = MethodSequenceView.METHOD_COLOR;
				else throw new RuntimeException("Not handled: "+theMember); 
				itsTimeScale.pEventBrowsers().add (new BrowserData(theBrowser, theColor));
			}
			
		}
		else 
		{
			itsTitleLabel.setText("Object inspector");
		}
		
		itsMemberSelector.pInspector().set(itsInspector);
		itsMemberSelector.selectFields();

	}
	
	
	private JComponent createTitlePanel()
	{
		itsTitleLabel = new JLabel();
		return itsTitleLabel;
	}
	
	/**
	 * The property that contains the members whose history is displayed.
	 */
	public ISetProperty<MemberInfo> pMembers()
	{
		return itsMemberSelector.pSelectedMembers();
	}
	
}
