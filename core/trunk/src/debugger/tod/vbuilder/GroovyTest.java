/*
 * Created on Jul 7, 2005
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
