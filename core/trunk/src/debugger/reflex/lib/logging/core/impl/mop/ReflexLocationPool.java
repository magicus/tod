/*
 * Created on Oct 9, 2004
 */
package reflex.lib.logging.core.impl.mop;

import javassist.NotFoundException;
import reflex.api.model.RBehavior;
import reflex.api.model.RClass;
import reflex.api.model.RConstructor;
import reflex.api.model.RField;
import reflex.api.model.RLocalVariable;
import reflex.api.model.RMember;
import reflex.api.model.RMethod;
import reflex.api.model.RStructuralElement;
import reflex.std.installer.msgreceive.RMethodWrapper;
import tod.core.BehaviourType;
import tod.core.ILocationRegistrer;

/**
 * Helps the MOP to maintain and generate location ids.
 * When it needs to retrieve the id of a particular location,
 * it can call one of the methods of this class, which will
 * automatically assign an id and register it to the log collector
 * if the location has no id.
 * @author gpothier
 */
public class ReflexLocationPool
{
	/**
	 * Key of the attribute we use to save the location id
	 * in each model object.
	 */
	private static final Object ATTRIBUTE_KEY = new Object();
	
	private static LocationHandler itsClassHandler = new LocationHandler()
	{
		protected void register(int aId, RStructuralElement aElement)
		{
			try
			{
				RClass theClass = (RClass) aElement;

				// Determine superclass id
				RClass theSuperclass = theClass.getSuperclass();
				int theSuperclassId = theSuperclass != null ? getLocationId(theSuperclass) : -1;
				
				// determine interface ids
				RClass[] theInterfaces = theClass.getInterfaces();
				int[] theInterfaceIds = null;
				if (theInterfaces != null)
				{
					theInterfaceIds = new int[theInterfaces.length];
					for (int i = 0; i < theInterfaces.length; i++)
					{
						RClass theInterface = theInterfaces[i];
						theInterfaceIds[i] = getLocationId(theInterface);
					}
				}
				
				// Register current class
				Config.COLLECTOR.registerType(aId, theClass.getName(), theSuperclassId, theInterfaceIds);
			}
			catch (NotFoundException e)
			{
				throw new RuntimeException(e);
			}
		}
	};
	
	private static LocationHandler itsBehaviorHandler = new LocationHandler()
	{
		protected void register(int aId, RStructuralElement aElement)
		{
			RBehavior theBehavior = (RBehavior) aElement;
			BehaviourType theType = null;
			
			if (theBehavior instanceof RMethod) theType = BehaviourType.METHOD;
			else if (theBehavior instanceof RConstructor) theType = BehaviourType.CONSTRUCTOR;
			
			Config.COLLECTOR.registerBehavior(
					theType,
					aId, 
					getDeclaringClassId(theBehavior),
					theBehavior.getName(),
					""); // TODO: signature
		}
	};
	
	private static LocationHandler itsFieldHandler = new LocationHandler()
	{
		protected void register(int aId, RStructuralElement aElement)
		{
			RField theField = (RField) aElement;
			Config.COLLECTOR.registerField(
					aId, 
					getDeclaringClassId(theField),
					theField.getName());
		}
	};
	
	public static int getLocationId (RClass aClass)
	{
		return itsClassHandler.getLocationId(aClass);
	}
	
	public static int getLocationId (RBehavior aBehavior)
	{
		return itsBehaviorHandler.getLocationId(aBehavior);
	}
	
	public static int getLocationId (RField aField)
	{
		return itsFieldHandler.getLocationId(aField);
	}
	
	private static abstract class LocationHandler
	{
		private int itsCurrentId;
		
		public int getLocationId (RStructuralElement aElement)
		{
			Integer theId = (Integer) aElement.getAttribute(ATTRIBUTE_KEY);
            
            if (theId == null) 
            {
                if (aElement instanceof RMethodWrapper)
				{
					RMethodWrapper theWrapper = (RMethodWrapper) aElement;
					theId = (Integer) theWrapper.getMethod().getAttribute(ATTRIBUTE_KEY);
	                if (theId == null) theId = (Integer) theWrapper.getHiddenMethod().getAttribute(ATTRIBUTE_KEY);
				}
            }
            
			if (theId == null)
			{
				synchronized (this)
				{
					int theNumber = itsCurrentId++;
					theId = new Integer (theNumber);
					
					register(theNumber, aElement);
				}
				aElement.setAttribute(ATTRIBUTE_KEY, theId);
			}
			
			return theId.intValue();
		}

		protected int getDeclaringClassId (RMember aMember)
		{
			return ReflexLocationPool.getLocationId(aMember.getDeclaringClass());
		}
		
//		/**
//		 * This method is called before {@link #register(int, RStructuralElement)} so
//		 * that
//		 * @param aElement
//		 */
//		protected abstract void preRegister (RStructuralElement aElement);
		
		/**
		 * Tells the log colector to register the specified model object
		 * @param aId The id assigned to the object.
		 * @param aElement The object to register.
		 */
		protected abstract void register (int aId, RStructuralElement aElement);
		
		
	}
	
}
