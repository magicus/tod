/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.vbuilder;

import java.lang.reflect.Constructor;

import zz.csg.CSGUtils;
import zz.csg.GraphicTree;
import zz.csg.api.IGraphicContainer;

public class GroovyTest
{
	public static void main(String[] args) throws Exception 
	{
		new GroovyTest().test();
	}
	
	private void test() throws Exception
	{
		Cell theCell = new Cell();
//		System.out.println(new Circle().getRed());
		
		ICellListener theListener;
		
		IGraphicContainer theGraphicContainer = theCell.getGraphicContainer();
		CSGUtils.showFrame("GroovyTest", theGraphicContainer);
		
		theListener = (ICellListener) instantiate("tod.vbuilder.BaseTreeLayout");
		theCell.addListener(theListener);
		
		Class theClass = loadClass("tod.vbuilder.BaseTreeStructure");
		System.out.println(theClass);

		Class[] theArgTypes = {Cell.class};
		Object[] theArgs = {theCell};
		
		Constructor theConstructor = theClass.getConstructor(theArgTypes);
		Object theInstance = theConstructor.newInstance(theArgs);
		System.out.println(theInstance);
		
		theCell.fireEvents();
		
		GraphicTree.showTree(theGraphicContainer);
	}
	
	private Class loadClass (String aName) throws ClassNotFoundException
	{
		ClassLoader theLoader = new MyLoader();
		return theLoader.loadClass(aName);
	}
	
	private Object instantiate (String aName) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		Class theClass = loadClass(aName);
		return theClass.newInstance();
	}
	
	private static class MyLoader extends ClassLoader
	{
	}
}
