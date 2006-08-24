/*
 * Created on Nov 18, 2004
 */
package tod.gui.seed;

import tod.core.model.browser.ILogBrowser;
import tod.core.model.structure.ObjectId;
import tod.gui.IGUIManager;
import tod.gui.ObjectInspectorView;
import tod.gui.view.LogView;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * Seed for the {@link tod.gui.ObjectInspectorView}
 * @author gpothier
 */
public class ObjectInspectorSeed extends Seed
{
	private ObjectId itsInspectedObject;
	
	private IRWProperty<Long> pSelectionStart = new SimpleRWProperty<Long>(this);
	private IRWProperty<Long> pSelectionEnd = new SimpleRWProperty<Long>(this);
	private IRWProperty<Long> pCurrentPosition = new SimpleRWProperty<Long>(this);

	
	public ObjectInspectorSeed(IGUIManager aGUIManager, ILogBrowser aLog, ObjectId aInspectedObject)
	{
		super(aGUIManager, aLog);
		itsInspectedObject = aInspectedObject;
		
		pSelectionStart().set(getEventTrace().getFirstTimestamp());
		pSelectionEnd().set((getEventTrace().getFirstTimestamp() + getEventTrace().getLastTimestamp()) / 2);
	}
	
	protected LogView requestComponent()
	{
		ObjectInspectorView theView = new ObjectInspectorView (getGUIManager(), getEventTrace(), this);
		theView.init();
		return theView;
	}
	
	public ObjectId getInspectedObject()
	{
		return itsInspectedObject;
	}

	public IRWProperty<Long> pCurrentPosition()
	{
		return pCurrentPosition;
	}

	public IRWProperty<Long> pSelectionEnd()
	{
		return pSelectionEnd;
	}

	public IRWProperty<Long> pSelectionStart()
	{
		return pSelectionStart;
	}
	
	
}
