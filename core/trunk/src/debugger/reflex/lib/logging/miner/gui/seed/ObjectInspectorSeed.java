/*
 * Created on Nov 18, 2004
 */
package reflex.lib.logging.miner.gui.seed;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.view.LogView;
import tod.core.model.structure.ObjectId;
import tod.core.model.trace.IEventTrace;
import tod.gui.ObjectInspectorView;
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

	
	public ObjectInspectorSeed(IGUIManager aGUIManager, IEventTrace aLog, ObjectId aInspectedObject)
	{
		super(aGUIManager, aLog);
		itsInspectedObject = aInspectedObject;
		
		pSelectionStart().set(getLog().getFirstTimestamp());
		pSelectionEnd().set((getLog().getFirstTimestamp() + getLog().getLastTimestamp()) / 2);
	}
	
	protected LogView requestComponent()
	{
		ObjectInspectorView theView = new ObjectInspectorView (getGUIManager(), getLog(), this);
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
