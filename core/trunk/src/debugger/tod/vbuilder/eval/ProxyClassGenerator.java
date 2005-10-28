/*
 * Created on Jun 13, 2005
 */
package tod.vbuilder.eval;

import javassist.CannotCompileException;
import reflex.api.API;
import reflex.api.model.RClass;
import reflex.api.model.RField;
import reflex.api.model.iterator.RFieldIterator;
import reflex.api.mop.MOPException;
import reflex.api.mop.SMetaobject;

/**
 * Transforms regular classes into proxy classes, that get their fields'
 * values from an evaluation context.
 * @author gpothier
 */
public class ProxyClassGenerator implements SMetaobject
{
	/**
	 * Field name prefix for the field info field.
	 */
	private static final String PREFIX_FIELD_ID = "_tod_fid_";
	
	/**
	 * Field name prefix for the field initialized flag field.
	 */
	private static final String PREFIX_VALID = "_tod_ok_";

	public void handleClass(RClass aClass) throws MOPException
	{
		try
		{
			RFieldIterator theIterator = aClass.getDeclaredFieldIterator();
			while (theIterator.hasNext())
			{
				RField theField = (RField) theIterator.next();
				processField(aClass, theField);
			}
		}
		catch (CannotCompileException e)
		{
			e.printStackTrace();
		}
	}
	
	private void processField (RClass aClass, RField aField) throws CannotCompileException
	{
		String theName = aField.getName();
		
		// Create FieldInfo field
		RField theIdField = API.getMemberFactory().newField(
				"private static reflex.lib.logging.core.api.collector.FieldInfo "+PREFIX_FIELD_ID+theName, 
				aClass);
		aClass.addField(aField);
		
		// Create the lazy init flag field
		RField theFlagField = API.getMemberFactory().newField("private boolean "+PREFIX_VALID+theName, aClass);
		aClass.addField(aField);
	}

}
