/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.tools.tostring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.config.ClassSelector;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.browser.ICompoundInspector.EntryValue;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.impl.bci.asm.BCIUtils;
import tod.tools.parsers.ParseException;
import tod.tools.parsers.workingset.WorkingSetFactory;
import zz.jinterp.JArray;
import zz.jinterp.JArrayType;
import zz.jinterp.JBehavior;
import zz.jinterp.JClass;
import zz.jinterp.JField;
import zz.jinterp.JInstance;
import zz.jinterp.JInterpreter;
import zz.jinterp.JObject;
import zz.jinterp.JStaticField;
import zz.jinterp.JType;
import zz.jinterp.JPrimitive.JVoid;
import zz.jinterp.SimpleInterp.SimpleStaticField;

/**
 * Permits to compute the result of toString on reconstituted objects.
 * @author gpothier
 */
public class ToStringComputer
{
	private static final JObject NULL = new JVoid();
	
	private final TODConfig itsConfig;
	private final IObjectInspector itsInspector;
	private final JInterpreter itsInterpreter;
	
	private final ClassSelector itsGlobalSelector;
	private final ClassSelector itsTraceSelector;

	
	public ToStringComputer(TODConfig aConfig, IObjectInspector aInspector)
	{
		itsConfig = aConfig;
		itsInspector = aInspector;
		itsGlobalSelector = parseWorkingSet(aConfig.get(TODConfig.SCOPE_GLOBAL_FILTER));
		itsTraceSelector = parseWorkingSet(aConfig.get(TODConfig.SCOPE_TRACE_FILTER));
		
		itsInterpreter = new TODInterpreter();
	}
	
	private ClassSelector parseWorkingSet(String aWorkingSet)
	{
		try
		{
			return WorkingSetFactory.parseWorkingSet(aWorkingSet);
		}
		catch (ParseException e)
		{
			throw new RuntimeException("Cannot parse selector: "+aWorkingSet, e);
		}
	}
	

	
	/**
	 * Computes the toString of the inspected object. 
	 */
	public String compute()
	{
		JObject[] args = {}; 
		JType theType = convertType(itsInspector.getType());
		
		JObject theResult;
		if (theType instanceof JClass)
		{
			JClass theClass = (JClass) theType;
			JBehavior theBehavior = theClass.getVirtualBehavior("toString", "()Ljava/lang/String;");
			TODInstance theInstance = new TODInstance(theClass, itsInspector);
			theResult = theBehavior.invoke(null, theInstance, args);
			
		}
		else throw new RuntimeException("Not handled: "+theType);
		
		if (theResult instanceof JInstance)
		{
			JInstance theInstance = (JInstance) theResult;
			return itsInterpreter.toString(theInstance);
		}
		else throw new RuntimeException("Unexpected result: "+theResult);
	}
	
	public boolean isInScope(JClass aClass)
	{
		String theName = aClass.getName().replace('/', '.');
		if (! BCIUtils.acceptClass(theName, itsGlobalSelector)) return false;
		if (! BCIUtils.acceptClass(theName, itsTraceSelector)) return false;
		return true;
	}

	private JObject convertObject(Object aValue)
	{
		if (aValue instanceof ObjectId)
		{
			ObjectId theId = (ObjectId) aValue;
			Object theRegistered = getLogBrowser().getRegistered(theId);
			if (theRegistered != null) return itsInterpreter.toJObject(theRegistered);
			else 
			{
				IObjectInspector theInspector = getLogBrowser().createObjectInspector(theId);
				theInspector.setReferenceEvent(getRefEvent());
				ITypeInfo theType = theInspector.getType();
				if (theType instanceof IClassInfo)
				{
					IClassInfo theClass = (IClassInfo) theType;
					return new TODInstance(convertClass(theClass), theInspector);
				}
				else if (theType instanceof IArrayTypeInfo)
				{
					IArrayTypeInfo theArrayType = (IArrayTypeInfo) theType;
					return new TODArray(theInspector);
				}
				else throw new RuntimeException("Not handled: "+theType);
				
			}
		}
		else return itsInterpreter.toJObject(aValue);
	}
	
	private JType convertType(ITypeInfo aType)
	{
		if (aType instanceof IClassInfo)
		{
			IClassInfo theClass = (IClassInfo) aType;
			return convertClass(theClass);
		}
		else if (aType instanceof IArrayTypeInfo)
		{
			IArrayTypeInfo theArrayType = (IArrayTypeInfo) aType;
			return convertArray(theArrayType);
		}
		else throw new RuntimeException("Not handled: "+aType);
	}
	
	private JClass convertClass(IClassInfo aClass)
	{
		return itsInterpreter.getClass(aClass.getName().replace('.', '/'));
	}
	
	private IClassInfo convertClass(JClass aClass)
	{
		return getLogBrowser().getStructureDatabase().getClass(aClass.getName().replace('/', '.'), true);
	}
	
	private IFieldInfo convertField(JField aField)
	{
		JClass theJClass = aField.getDeclaringClass();
		IStructureDatabase theStructureDatabase = getLogBrowser().getStructureDatabase();
		IClassInfo theTODClass = theStructureDatabase.getClass(theJClass.getName().replace('/', '.'), true);
		return theTODClass.getField(aField.getName());
	}
	
	private JArrayType convertArray(IArrayTypeInfo aArrayType)
	{
		throw new UnsupportedOperationException();
	}
	
	private ILogBrowser getLogBrowser()
	{
		return itsInspector.getLogBrowser();
	}

	private ILogEvent getRefEvent()
	{
		return itsInspector.getReferenceEvent();
	}

	private class TODInterpreter extends JInterpreter
	{
		@Override
		protected byte[] getClassBytecode(String aName)
		{
			IStructureDatabase theStructureDatabase = getLogBrowser().getStructureDatabase();
			IClassInfo theClass = theStructureDatabase.getClass(aName.replace('/', '.'), false);
			return theClass != null ? theClass.getOriginalBytecode() : null;
		}

		@Override
		public JStaticField createStaticField(JClass aClass, String aName, JType aType, int aAccess)
		{
			if (! isInScope(aClass)) return new SimpleStaticField(aClass, aName, aType, aAccess);
			
			IClassInfo theTODClass = convertClass(aClass);
			IObjectInspector theInspector = getLogBrowser().createClassInspector(theTODClass);
			theInspector.setReferenceEvent(getRefEvent());
			IFieldInfo theTODField = theTODClass.getField(aName);
			EntryValue[] theEntryValue = theInspector.getEntryValue(theTODField);
			
			JObject theValue;
			if (theEntryValue.length == 0)
			{
				theValue = aType.getInitialValue();
			}
			else if (theEntryValue.length == 1)
			{
				theValue = convertObject(theEntryValue[0].getValue());
			}
			else
			throw new RuntimeException("Can't retrieve value");

			return new TODStaticField(aClass, aName, aType, aAccess, theValue);
		}
	}

	/**
	 * A {@link JInstance} that obtains field values from reconstituted objects
	 * @author gpothier
	 */
	private class TODInstance extends JInstance
	{
		private final IObjectInspector itsInspector;
		private final Map<JField, JObject> itsValues = new HashMap<JField, JObject>();
		
		public TODInstance(JClass aClass, IObjectInspector aInspector)
		{
			super(aClass);
			itsInspector = aInspector;
		}

		@Override
		public JObject getFieldValue(JField aField)
		{
			JObject theResult = itsValues.get(aField);
			if (theResult == null)
			{
				EntryValue[] theEntryValue = itsInspector.getEntryValue(convertField(aField));
				if (theEntryValue.length != 1) throw new RuntimeException("Can't retrieve value");
				Object theValue = theEntryValue[0].getValue();
				theResult = theValue == null ? NULL : convertObject(theValue);
			}
			
			return theResult == NULL ? null : theResult;
		}
		
		@Override
		public void putFieldValue(JField aField, JObject aValue)
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private class TODArray extends JArray
	{
		private final IObjectInspector itsInspector;
		private final List<JObject> itsValues = new ArrayList<JObject>();

		public TODArray(IObjectInspector aInspector)
		{
			itsInspector = aInspector;
		}

		@Override
		public int getSize()
		{
			return itsInspector.getFieldCount();
		}

		@Override
		public JObject get(int aIndex)
		{
			JObject theResult = itsValues.get(aIndex);
			if (theResult == null)
			{
				IFieldInfo theField = itsInspector.getFields(aIndex, 1).get(0);
				EntryValue[] theEntryValue = itsInspector.getEntryValue(theField);
				if (theEntryValue.length != 1) throw new RuntimeException("Can't retrieve value");
				Object theValue = theEntryValue[0].getValue();
				theResult = theValue == null ? NULL : convertObject(theValue);
			}
			
			return theResult == NULL ? null : theResult;
		}

		@Override
		public void set(int aIndex, JObject aValue)
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private class TODStaticField extends SimpleStaticField
	{
		public TODStaticField(
				JClass aClass,
				String aName,
				JType aType,
				int aAccess,
				JObject aStaticValue)
		{
			super(aClass, aName, aType, aAccess, aStaticValue);
		}

		@Override
		public void putStaticFieldValue(JObject aValue)
		{
			throw new UnsupportedOperationException();
		}
	}
}
