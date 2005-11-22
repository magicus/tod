/*
 * Created on Nov 2, 2005
 */
package tod.gui;

import java.awt.Color;

import tod.core.model.structure.IBehaviorInfo;
import tod.core.model.structure.ITypeInfo;
import tod.core.model.structure.ObjectId;
import tod.core.model.trace.IEventTrace;
import tod.gui.seed.ObjectInspectorSeed;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.text.XFont;

/**
 * This class contains static methods that created standard
 * hyperlinks for types, methods, etc.
 * @author gpothier
 */
public class Hyperlinks
{
	public static IRectangularGraphicObject type (IGUIManager aGUIManager, ITypeInfo aType, XFont aFont)
	{
		return SVGHyperlink.create(aGUIManager, null, aType.getName(), aFont, Color.BLUE);
	}
	
	public static IRectangularGraphicObject behavior(IGUIManager aGUIManager, IBehaviorInfo aBehavior, XFont aFont)
	{
		return SVGHyperlink.create(aGUIManager, null, aBehavior.getName(), aFont, Color.BLUE);		
	}
	
	public static IRectangularGraphicObject object(
			IGUIManager aGUIManager, 
			IEventTrace aEventTrace, 
			Object aObject,
			XFont aFont)
	{
		return object(aGUIManager, aEventTrace, null, aObject, aFont);
	}
	
	/**
	 * Creates a hyperlink that permits to jump to an object inspector. 
	 * @param aCurrentObject If provided, reference to the current object will
	 * be displayed as "this" 
	 * @param aObject The object to link to.
	 */
	public static IRectangularGraphicObject object(
			IGUIManager aGUIManager,
			IEventTrace aEventTrace,
			Object aCurrentObject, 
			Object aObject, 
			XFont aFont)
	{
		
		if (aObject instanceof ObjectId)
		{
			ObjectId theId = (ObjectId) aObject;
			ITypeInfo theType = aEventTrace.createObjectInspector(theId).getType();

			String theText;
			if (aCurrentObject != null && aCurrentObject.equals(aObject)) theText = "this";
			else theText = theType.getName() + " (" + theId + ")";

			return SVGHyperlink.create(
					aGUIManager, 
					new ObjectInspectorSeed(aGUIManager, aEventTrace, theId), 
					theText, 
					aFont, 
					Color.BLUE);
		}
		else if (aObject instanceof String)
		{
			String theString = (String) aObject;
			return SVGFlowText.create("\""+theString+"\"", aFont, Color.GRAY);
		}
		else 
		{
			SVGFlowText theFlowText = SVGFlowText.create(""+aObject, aFont, Color.GRAY);
			return theFlowText;
		}

		
	}
}
