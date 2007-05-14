/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

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
*/
package tod.gui;

import java.awt.Container;

import javax.swing.JFrame;

import net.java.javafx.FXShell;

public class JavaFXBridge
{
	private static Object result;
	
	/**
	 * Returns an object created by a JavaFX script.  
	 * @param aScript Name of the JavaFX script.
	 */
	public static Object get(String aScript)
	{
		try
		{
			result = null;
			FXShell.main(new String[] {aScript});
			return result;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static void put(Object aObject)
	{
		result = aObject;
	}
	
	/** 
	 * Example usage: retrieves a JavaFX component created by the script 
	 * named "File1"
	 */
	public static void main(String[] args) throws Exception
	{
//		Object theResult = get("tod.gui.Home");
		Object theResult = get("File1");
		JFrame theFrame = new JFrame("JavaFXBridge test");
		theFrame.setContentPane((Container) theResult);
		theFrame.pack();
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theFrame.setVisible(true);
	}
}
