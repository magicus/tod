/*
 * Created on Nov 10, 2004
 */
package reflex.lib.logging.miner.gui.view;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.formatter.ObjectFormatter;
import reflex.lib.logging.miner.gui.kit.SeedLinkLabel;
import reflex.lib.logging.miner.gui.seed.ObjectInspectorSeed;
import reflex.lib.logging.miner.gui.seed.Seed;
import tod.core.model.structure.ObjectId;
import tod.core.model.trace.IEventTrace;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.PropertyUtils;

/**
 * Base class for views.
 * @see reflex.lib.logging.miner.gui.seed.Seed
 * @author gpothier
 */
public abstract class LogView extends JPanel
{
	private final IEventTrace itsLog;
	private final IGUIManager itsGUIManager;
	
	private List<PropertyUtils.Connector> itsConnectors; 

	
	public LogView(IGUIManager aGUIManager, IEventTrace aLog)
	{
		itsGUIManager = aGUIManager;
		itsLog = aLog;
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		if (itsConnectors != null) 
			for (PropertyUtils.Connector theConnector : itsConnectors) theConnector.connect();
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		if (itsConnectors != null) 
			for (PropertyUtils.Connector theConnector : itsConnectors) theConnector.disconnect();
	}
	
	/**
	 * Prepares a connection between two properties. The connection is effective only once this
	 * component is shown. The connection is removed when this component is hidden.
	 * @see PropertyUtils.Connector
	 */
	protected <T> void connect (IRWProperty<T> aSource, IRWProperty<T> aTarget, boolean aSymmetric)
	{
		if (itsConnectors == null) itsConnectors = new ArrayList<PropertyUtils.Connector>(); 
		PropertyUtils.SimpleValueConnector<T> theConnector = new PropertyUtils.SimpleValueConnector<T>(aSource, aTarget, aSymmetric, true);
		itsConnectors.add (theConnector);
	}
	
	public IEventTrace getLog()
	{
		return itsLog;
	}
	
	public IGUIManager getGUIManager()
	{
		return itsGUIManager;
	}
	
	/**
	 * This method is called after the object is instantiated.
	 * It should be used to create the user interface.
	 */
	public void init()
	{
	}
	
	/**
	 * Creates a standard panel that shows a title and a link label.
	 */
	protected JComponent createTitledLink (String aTitle, String aLinkName, Seed aSeed)
	{
		return createTitledPanel(aTitle, new SeedLinkLabel(getGUIManager(), aLinkName, aSeed));
	}
	
	/**
	 * Creates a standard panel that shows a title and another component.
	 */
	protected JComponent createTitledPanel (String aTitle, JComponent aComponent)
	{
		JPanel thePanel = new JPanel(new FlowLayout (FlowLayout.LEADING));
		thePanel.add (new JLabel (aTitle));
		thePanel.add (aComponent);
		
		return thePanel;
	}
	
	
	
	/**
	 * Creates a title label, with a big font.
	 */
	protected JComponent createTitleLabel (String aTitle)
	{
		return new JLabel ("<html><font size='+1'>"+aTitle);
	}
	
	/**
	 * Creates a link that jumps to an inspector for the specified object.
	 */
	protected JComponent createInspectorLink (Object aObject)
	{
		if (aObject instanceof ObjectId)
		{
			ObjectId theObjectId = (ObjectId) aObject;
			return new SeedLinkLabel (
					getGUIManager(), 
					ObjectFormatter.getInstance().getPlainText(aObject), 
					new ObjectInspectorSeed(getGUIManager(), getLog(), theObjectId));
		}
		else return new JLabel (ObjectFormatter.getInstance().getPlainText(aObject));
	}

}
