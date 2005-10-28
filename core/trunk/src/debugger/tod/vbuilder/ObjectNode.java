/*
 * Created on Jun 5, 2005
 */
package tod.vbuilder;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import EDU.Washington.grad.gjb.cassowary.ClStrength;
import EDU.Washington.grad.gjb.cassowary.ExCLNonlinearExpression;

import tod.core.model.structure.ObjectId;
import zz.csg.api.constraints.ConstraintParser;
import zz.csg.api.constraints.ConstraintSystem;
import zz.csg.api.constraints.SymbolNotFoundException;
import zz.csg.api.constraints.parser.ParseException;
import zz.csg.api.figures.IGOFlowText;
import zz.csg.api.figures.IGORectangle;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;
import zz.csg.impl.figures.SVGRectangle;
import zz.utils.ui.text.XFont;

/**
 * A graphic object that represents an object.
 * @author gpothier
 */
public class ObjectNode extends SVGGraphicContainer
implements IObjectNode
{
	private static final XFont FONT = new XFont(new Font("serif", Font.PLAIN, 4), false);
	
	private ObjectId itsId;
	private Map<String, Object> itsAttributes = new HashMap<String, Object>();
	private final Cell itsCell;
	
	private ConstraintSystem itsConstraintSystem = new ConstraintSystem();
	private ConstraintParser itsConstraintParser = new ConstraintParser();

	public ObjectNode(Cell aCell, ObjectId aId)
	{
		itsCell = aCell;
		itsId = aId;
		
		IGORectangle theRectangle = new SVGRectangle();
		theRectangle.pStrokePaint().set(Color.RED);
		theRectangle.pStrokeWidth().set(1.0);
		pChildren().add(theRectangle);
		
		IGOFlowText theText = new SVGFlowText();
		theText.pText().set(""+aId);
		theText.pFont().set(FONT);
		theText.pStrokePaint().set(Color.BLACK);
		theText.setSizeComputer(IGOFlowText.DefaultSizeComputer.getInstance());
		pChildren().add(theText);
		
		itsConstraintParser.addVar("pw", pBounds().pW());
		itsConstraintParser.addVar("ph", pBounds().pH());
				
		itsConstraintParser.addVar("rx", theRectangle.pBounds().pX());
		itsConstraintParser.addVar("ry", theRectangle.pBounds().pY());
		itsConstraintParser.addVar("rw", theRectangle.pBounds().pW());
		itsConstraintParser.addVar("rh", theRectangle.pBounds().pH());
				
		itsConstraintParser.addVar("tx", theText.pBounds().pX());
		itsConstraintParser.addVar("ty", theText.pBounds().pY());
		itsConstraintParser.addCnst("TW", theText.pBounds().pW());
		itsConstraintParser.addCnst("TH", theText.pBounds().pH());
		
		itsConstraintSystem.addConstraint(itsConstraintParser.createConstraint("pw >= rx + rw"));
		itsConstraintSystem.addConstraint(itsConstraintParser.createConstraint("ph >= ry + rh"));
		
		itsConstraintSystem.addConstraint(itsConstraintParser.createConstraint("rw = TW + 2"));
		itsConstraintSystem.addConstraint(itsConstraintParser.createConstraint("rh = TH + 2"));
		
		itsConstraintSystem.addConstraint(itsConstraintParser.createConstraint("rx = 1"));
		itsConstraintSystem.addConstraint(itsConstraintParser.createConstraint("ry = 1"));
		
		itsConstraintSystem.addConstraint(itsConstraintParser.createConstraint("tx = rx + 1"));
		itsConstraintSystem.addConstraint(itsConstraintParser.createConstraint("ty = ry + 1"));
	}

	public Cell getCell()
	{
		return itsCell;
	}

	public ObjectId getId()
	{
		return itsId;
	}
	
	public <T> T get(NodeAttribute<T> aAttribute)
	{
		return (T) get(aAttribute.getName());
	}

	public <T> T set(NodeAttribute<T> aAttribute, T aValue)
	{
		return (T) set(aAttribute.getName(), aValue);
	}

	public Object get(String aKey)
	{
		return itsAttributes.get(aKey);
	}
	
	public Object set(String aKey, Object aValue)
	{
		Object thePreviousValue = itsAttributes.put(aKey, aValue);
		getCell().queueEvent(new CellEvent.AttributeChanged(this, aKey, thePreviousValue, aValue));
		return thePreviousValue;
	}

	public Map<String, Object> getAttributes()
	{
		return itsAttributes;
	}
	
	/**
	 * Communication with groovy: operator overloading of "a[b]".
	 */
	public Object getAt(String b)
	{
		return get(b);
	}
	
	/**
	 * Communication with groovy: operator overloading of "a[b] = c".
	 */
	public void putAt (String b, Object c)
	{
		set(b, c);
	}
	
	@Override
	public String toString()
	{
		return super.toString() + " id: "+itsId;
	}
}
