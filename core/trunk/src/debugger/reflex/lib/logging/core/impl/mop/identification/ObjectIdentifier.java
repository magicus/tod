/*
 * Created on Oct 13, 2004
 */
package reflex.lib.logging.core.impl.mop.identification;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import reflex.api.API;
import reflex.api.ReflexConfig;
import reflex.api.hookset.ClassSelector;
import reflex.api.model.RClass;
import reflex.api.model.RField;
import reflex.api.model.RMethod;
import reflex.api.mop.MOPException;
import reflex.api.mop.SMetaobject;
import tod.core.config.StaticConfig;

/**
 * This structural metaobject adds a field on each object
 * that permits to uniquely identify it.
 * @author gpothier
 */
public class ObjectIdentifier implements SMetaobject
{
	private static ObjectIdentifier INSTANCE = new ObjectIdentifier();

	public static ObjectIdentifier getInstance()
	{
		return INSTANCE;
	}

	private ObjectIdentifier()
	{
	}
	
	public static final String FIELD_NAME = "__log_uid";
	
	private RClass itsInterfaceClass;

	public void setup(ReflexConfig aConfig)
	{
		ClassSelector theIdentifiedClasses = 
			StaticConfig.getInstance().getIdentificationClassSelector();
		
		try
		{
			itsInterfaceClass = API.getClassPool().get("reflex.lib.logging.core.impl.mop.identification.IIdentifiableObject");
		}
		catch (NotFoundException e)
		{
			e.printStackTrace();
		}
		
        aConfig.addSLink(theIdentifiedClasses, this);
	}
	
	public void handleClass(RClass aClass) throws MOPException
	{
		try
		{
			// Don't modify java.lang.Object
			if ("java.lang.Object".equals(aClass.getName())) return;
			
			// Don't aply to interfaces
			if (aClass.isInterface()) return;
			
			// If we already applied to a superclass, don't apply again.
			if (aClass.isSubtypeOf(itsInterfaceClass)) return;
			
			// Find ancestor to apply to
			aClass = findAncestorToProcess(aClass);
			
			aClass.addInterface(itsInterfaceClass);
			
			RField theField = API.getMemberFactory().newField(
					"long "+FIELD_NAME+";",
					aClass);
			
			aClass.addField(
					theField,
					"reflex.lib.logging.core.impl.mop.identification.ObjectIdentifier.createId()");
			
			
			RMethod theMethod = API.getMemberFactory().newMethod(
					"public final long "+FIELD_NAME+"() " +
					"{ return "+FIELD_NAME+";}", aClass);
			
			aClass.addMethod(theMethod);
			
			System.out.println("Applied ObjectIdentifier to " + aClass.getName());
		}
		catch (CannotCompileException e)
		{
			throw new MOPException(e);
		}
		catch (NotFoundException e)
		{
			throw new MOPException(e);
		}
	}
	
	/**
	 * Searches the furthest ancestor that is accepted by the class selector.
	 */
	private RClass findAncestorToProcess (RClass aClass) throws NotFoundException
	{
		ClassSelector theIdentifiedClasses = 
			StaticConfig.getInstance().getIdentificationClassSelector();
		
		RClass theLatestAncestor = null;
		
		while (aClass != null)
		{
			if (theIdentifiedClasses.accept(aClass)) theLatestAncestor = aClass;
			aClass = aClass.getSuperclass();

			// Don't modify java.lang.Object
			if ("java.lang.Object".equals(aClass.getName())) break;
		}
		
		return theLatestAncestor;
	}
	
}
