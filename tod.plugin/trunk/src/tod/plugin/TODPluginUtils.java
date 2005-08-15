/*
 * Created on Aug 15, 2005
 */
package tod.plugin;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import reflex.lib.logging.core.api.collector.LocationInfo;
import reflex.lib.logging.core.api.collector.LocationRegistrer;
import reflex.lib.logging.core.api.collector.TypeInfo;
import reflex.lib.logging.miner.api.IBrowsableLog;

/**
 * Utilities for the TOD plugin
 * @author gpothier
 */
public class TODPluginUtils
{
	/**
	 * Returns the TOD location info that corresponds to the given element in the
	 * given session.
	 * @param aSession The debugging session in which the element should be looked for
	 * @param aElement The JDT java element to look for.
	 * @return TOD location info, or null if none is found.
	 */
	public static LocationInfo getLocationInfo (TODSession aSession, IJavaElement aElement)
	{
		IBrowsableLog theLog = aSession.getLog();
		LocationRegistrer theLocationRegistrer = theLog.getLocationRegistrer();
		
		if (aElement instanceof IMember)
		{
			IMember theMember = (IMember) aElement;
			
			IType theType = theMember.getDeclaringType();
			if (theType == null) return null;
			
			String theTypeName = theType.getFullyQualifiedName();
			TypeInfo theTypeInfo = theLocationRegistrer.getType(theTypeName);
			System.out.println(theTypeInfo);
			
			if (theMember instanceof IMethod)
			{
				IMethod theMethod = (IMethod) theMember;
				String theMethodName = theMethod.getElementName();
				return theTypeInfo.getBehavior(theMethodName);
			}
			else if (theMember instanceof IInitializer)
			{
				IInitializer theInitializer = (IInitializer) theMember;
				String theInitializerName = theInitializer.getElementName();
				return theTypeInfo.getBehavior(theInitializerName);
			}
			else if (theMember instanceof IField)
			{
				IField theField = (IField) theMember;
				String theFieldName = theField.getElementName();
				return theTypeInfo.getField(theFieldName);
			}
		}
		
		return null;
	}
}
